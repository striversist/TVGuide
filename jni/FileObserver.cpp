#include "FileObserver.h"

#include <string>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <assert.h>
#include <stddef.h>
#include <sys/queue.h>
#include <sys/inotify.h>
#include "MyLog.h"
#include "event_queue.h"

using namespace std;
const int kFailedRetryTimes = 10;

struct ThreadParams
{
    FileObserver::Delegate* delegate;
    const char* path;
};


bool isFileExist(const char* path)
{
    if (access(path, F_OK) == -1)
        return false;
    return true;
}

int setNonBlocking(int fd)
{
    int opts;
    opts = fcntl(fd, F_GETFL);
    if (opts<0)
    {
        perror("fcntl(sock,GETFL)");
        return -1;
    }
    opts = opts|O_NONBLOCK;
    if (fcntl(fd, F_SETFL, opts) < 0)
    {
        perror("fcntl(sock,SETFL,opts)");
        return -1;
    }
}

void* ThreadFunc(void* param)
{
    ThreadParams* thread_params = static_cast<ThreadParams*>(param);
    FileObserver::Delegate* delegate = thread_params->delegate;
    const char* path = thread_params->path;
    bool loop = true;
    
    int fd = inotify_init();
    if (fd == -1)
    {
        XLOG("inotify_init failed");
        return NULL;
    }

    // Will cause read error
    //if (setNonBlocking(fd) < 0)
    //{
    //    XLOG("Warning: set nonblocking failed");
    //}

    char watchPath[256] = {0};
    sprintf(watchPath, path, strlen(path));
    int wd = inotify_add_watch(fd, watchPath, IN_ALL_EVENTS);
    if (wd == -1)
    {
        XLOG("inotify_add_watch failed");
        return NULL;
    }

    int failedTimes = 0;
    XLOG("start watching %s\n", path);
    while (true && loop && failedTimes < kFailedRetryTimes)
    {
        XLOG("Watching loop begin");
        const int bufferSize = 16384;
        char buffer[bufferSize];
        XLOG("before read");
        ssize_t recvSize = read(fd, buffer, bufferSize);
        XLOG("after read");
        if (recvSize < 0)
        {
            XLOG("inotify read error");
            failedTimes++;
            usleep(1000 * 1000);
            continue;
        }

        queue_t q = queue_create();
        struct inotify_event *pevent;
        size_t event_size = 0;
        size_t q_event_size = 0;
        size_t buffer_i = 0;
        queue_entry_t event;
        int count = 0;
        
        while (buffer_i < recvSize)
        {
            /* Parse events and queue them. */
            pevent = (struct inotify_event*) &buffer[buffer_i];
            event_size =  offsetof(struct inotify_event, name) + pevent->len;
            q_event_size = offsetof(struct queue_entry, inot_ev.name) + pevent->len;
            event = (queue_entry_t)malloc(q_event_size);
            memmove(&(event->inot_ev), pevent, event_size);
            queue_enqueue(event, q);
            buffer_i += event_size;
            count++;
        }

        usleep(1000 * 500);     // sleep for file operation (eg. delete) complete
        char* cur_event_filename = NULL;
        char* cur_event_file_or_dir = NULL;
        int cur_event_wd;
        int cur_event_cookie;
        FileObserver::Event notifyEvent;
        while (event = queue_dequeue(q))
        {
            if (event->inot_ev.len)
                cur_event_filename = event->inot_ev.name;

            if ( event->inot_ev.mask & IN_ISDIR )
                cur_event_file_or_dir = "Dir";
            else
                cur_event_file_or_dir = "File";

            cur_event_wd = event->inot_ev.wd;
            cur_event_cookie = event->inot_ev.cookie;

            notifyEvent = FileObserver::None;
            switch (event->inot_ev.mask & 
                   (IN_ALL_EVENTS | IN_UNMOUNT | IN_Q_OVERFLOW | IN_IGNORED))
            {
                /* File was accessed */
                case IN_ACCESS:
                    //XLOG("ACCESS: %s \"%s\" on WD #%i\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::Access;
                    break;
  
                /* File was modified */
                case IN_MODIFY:
                    //XLOG("MODIFY: %s \"%s\" on WD #%i\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::Modify;
                    break;
  
                /* File changed attributes */
                case IN_ATTRIB:
                    //XLOG("ATTRIB: %s \"%s\" on WD #%i\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::AttribChanged;
                    break;
  
                /* File open for writing was closed */
                case IN_CLOSE_WRITE:
                    //XLOG("CLOSE_WRITE: %s \"%s\" on WD #%i\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::CloseWrite;
                    break;
  
                /* File open read-only was closed */
                case IN_CLOSE_NOWRITE:
                    XLOG("CLOSE_NOWRITE: %s \"%s\" on WD #%i\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::CloseNoWrite;
              	    if (!isFileExist(path))
              	    {
              	        XLOG("CLOSE_NOWRITE: file is deleted");
              	        notifyEvent = FileObserver::Delete;
          	        }
                    break;
  
                /* File was opened */
                case IN_OPEN:
                    //XLOG("OPEN: %s \"%s\" on WD #%i\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::Open;
                    break;
  
                /* File was moved from X */
                case IN_MOVED_FROM:
                    //XLOG("MOVED_FROM: %s \"%s\" on WD #%i. Cookie=%d\n", cur_event_file_or_dir, cur_event_filename, cur_event_wd, cur_event_cookie);
                    notifyEvent = FileObserver::MovedFrom;
                    break;

                /* File was deleted */
                case IN_DELETE_SELF:
                    XLOG("DELETE_SELF: %s \"%s\" deleted", cur_event_file_or_dir, cur_event_filename);
                    notifyEvent = FileObserver::Delete;
                    break;
  
                /* Watch was removed explicitly by inotify_rm_watch or automatically
                              because file was deleted, or file system was unmounted.  */
                case IN_IGNORED:
                    //XLOG("IGNORED: WD #%d\n", cur_event_wd);
                    notifyEvent = FileObserver::None;
                    break;
  
                /* Some unknown message received */
                default:
                    //XLOG("UNKNOWN EVENT \"%X\" OCCURRED for file \"%s\" on WD #%i\n", event->inot_ev.mask, cur_event_filename, cur_event_wd);
              	    notifyEvent = FileObserver::None;
                    break;
            }

            if (notifyEvent != FileObserver::None)
                delegate->onEvent(notifyEvent, path);

            if (notifyEvent == FileObserver::Delete)
            {
                loop = false;
                break;
            }
        }
        queue_dequeue(q);
        XLOG("Watching loop end");
    }

    if (failedTimes == kFailedRetryTimes)
    {
        delegate->onEvent(FileObserver::Error, path);
    }

    XLOG("inotify_rm_watch");
    inotify_rm_watch(fd, IN_ALL_EVENTS);
}

FileObserver::FileObserver(const std::string& path, Delegate* delegate)
{
    assert (delegate);
    mPath = path;
    mDelegate = delegate;
}

bool FileObserver::startWatching()
{
    bool success = false;

    pthread_t threadId;
    pthread_attr_t attributes;
    pthread_attr_init(&attributes);

    ThreadParams* params = new ThreadParams();
    params->delegate = mDelegate;
    params->path = mPath.c_str();
    success = !pthread_create(&threadId, &attributes, ThreadFunc, (void*)params);

    pthread_attr_destroy(&attributes);
    
    return success;
}

void FileObserver::stopWatching()
{
}

