package com.tools.tvguide.uninstall;


interface IUninstallReport {
    

    int sendRequest(int type, in Bundle data);
    
}