#ifndef FileDeleteObserver_h
#define FileDeleteObserver_h

#include "FileObserver.h"

class FileDeleteObserver : public FileObserver::Delegate
{
public:
    FileDeleteObserver(const std::string& path);
    bool startWatching();
    void stopWatching();
    
private:
    virtual void onEvent(FileObserver::Event event, const std::string& path);
    
    std::string mPath;
    FileObserver* mFileObserver;
};


#endif
