#ifndef FileObserver_h
#define FileObserver_h
#pragma once

#include <string>

class FileObserver
{
public:
    enum Event {
        None,
        Access,
        Modify,
        AttribChanged,
        CloseWrite,
        CloseNoWrite,
        Open,
        MovedFrom,
        Delete,
    };

    class Delegate
    {
    public:
        virtual void onEvent(Event event, const std::string& path) = 0;
    };
    
    FileObserver(const std::string& path, Delegate* delegate);
    bool startWatching();
    void stopWatching();

private:
    std::string mPath;
    Delegate* mDelegate;
};


#endif
