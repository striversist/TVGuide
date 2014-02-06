#include "FileDeleteObserver.h"
#include <string>
#include "MyLog.h"
#include "SimpleTcpClient.h"

extern bool gKeepAliveDaemonProcess;
using namespace std;

FileDeleteObserver::FileDeleteObserver(const std::string& path)
{
    mPath = path;
    mFileObserver = new FileObserver(path, this);
}

bool FileDeleteObserver::startWatching()
{
    return mFileObserver->startWatching();
}

void FileDeleteObserver::stopWatching()
{
    mFileObserver->stopWatching();
}

void FileDeleteObserver::onEvent(FileObserver::Event event, const std::string& path)
{
    if (event == FileObserver::Delete)
    {
        XLOG("FileDeleteObserver::onEvent delete path=%s", path.c_str());
        SimpleTcpClient client;
        if (client.connect("www.baidu.com", 80) < 0)
        {
            XLOG("FileDeleteObserver::onEvent connect error");
        }

        gKeepAliveDaemonProcess = false;
    }
}

