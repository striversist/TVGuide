#include "EchoTcpServer.h"

#include <string>
#include <stdio.h>
#include <strings.h>
#include <unistd.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <android/log.h>
#include "MyLog.h"
#include "SimpleTcpClient.h"

using namespace std;

const int kSelectTimeout = 6;      // Seconds
const int kMaxRetryTimes = 5;

EchoTcpServer::EchoTcpServer(int port)
{
    mListenPort = port;
    mLoop = true;
    
    bzero(&mServerAddr, sizeof(mServerAddr));
    mServerAddr.sin_family = AF_INET;
    mServerAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    mServerAddr.sin_port = htons(mListenPort);

    createSocket();
    setupSocket();
    bindSocket();
}

bool EchoTcpServer::isServerAlive(int port)
{
    SimpleTcpClient client;
    if (client.connect("127.0.0.1", port) < 0)
        return false;

    const char* msg = "hello";
    client.write(msg, strlen(msg));
    
    const int BUFFER_SIZE = 100;
    char buffer[BUFFER_SIZE] = {0};
    int length = 0;
    length = client.read(buffer, BUFFER_SIZE);
    XLOG("EchoTcpServer::isServerAlive recv %s\n", buffer);
    
    if (length > 0 && string(msg) == string(buffer))
        return true;
    
    return false;
}

void EchoTcpServer::start()
{
    XLOG("EchoTcpServer::start begin");
    if (listen(mServerSocket, 60) < 0)
    {
        XLOG("listen failed");
        return;
    }

    int communicateSocket;
    struct sockaddr_in client_addr;
    bzero(&client_addr, sizeof(client_addr));
    fd_set readFds;
    struct timeval timeout;
    int failTimes = 0;
    
    while (mLoop && (failTimes < kMaxRetryTimes))
    {
        //XLOG("EchoTcpServer::start enter while loop\n");
        FD_ZERO(&readFds);
        FD_SET(mServerSocket, &readFds);
        int maxFd = mServerSocket + 1;
        timeout.tv_sec = kSelectTimeout;
        timeout.tv_usec = 0;
        
        switch (select(maxFd, &readFds, NULL, NULL, &timeout))
        {
            case -1:    // Error
                failTimes++;
                break;
            case 0:
                XLOG("EchoTcpServer::start timeout, continue");
                break;
            default:
                if (FD_ISSET(mServerSocket, &readFds))
                {
                    socklen_t client_addr_len = sizeof(client_addr);
                    XLOG("EchoTcpServer::start server begin accept\n");
                    communicateSocket = accept(mServerSocket, (sockaddr*)&client_addr, &client_addr_len);
                    if (communicateSocket < 0)
                    {
                        XLOG("accept failed");
                        break;
                    }
                    else
                    {
                        XLOG("Client(IP: %s) connected.\n", inet_ntoa(client_addr.sin_addr));
                    }
        
                    const int BUFFERSIZE = 1024;
                    char buffer[BUFFERSIZE] = {0};
                    int recvMsgSize = 0;
        
                    XLOG("EchoTcpServer::start server begin recv\n");
                    recvMsgSize = recv(communicateSocket, buffer, BUFFERSIZE, 0);
                    if (recvMsgSize < 0)
                    {
                        XLOG("server recv msg failed");
                        break;
                    }
                    else if (recvMsgSize == 0)
                    {
                        XLOG("server recv finished\n");
                        break;
                    }
                    else
                    {
                        XLOG("EchoTcpServer::start server recv msg success: %s\n", buffer);
                        if (send(communicateSocket, buffer, recvMsgSize, 0) != recvMsgSize)
                        {
                            XLOG("server send msg failed");
                            break;
                        }
                    }
                    close(communicateSocket);
                }
        }
    }
    
    close(mServerSocket);
    XLOG("EchoTcpServer::start end");
}

void EchoTcpServer::stop()
{
    mLoop = false;
}

void EchoTcpServer::createSocket()
{
    mServerSocket= socket(PF_INET, SOCK_STREAM, 0);
    if (mServerSocket < 0)
    {
        XLOG("create socket failed");
        return;
    }
}

void EchoTcpServer::setupSocket()
{
    int opt = 1;
    setsockopt(mServerSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
}

void EchoTcpServer::bindSocket()
{
    if (bind(mServerSocket, (sockaddr*)&mServerAddr, sizeof(mServerAddr)) < 0)
    {
        XLOG("bind socket failed");
    }
}

