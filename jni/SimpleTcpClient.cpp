#include "SimpleTcpClient.h"
#include <string>
#include <strings.h>
#include <stdio.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>
#include "MyLog.h"

using namespace std;

int ResolveHost(const std::string& host, std::string& ip)
{
    std::string result;
    struct addrinfo *answer, hint, *curr;
    char ipstr[16] = {0};
    bzero(&hint, sizeof(hint));
    hint.ai_family = AF_INET;
    hint.ai_socktype = SOCK_STREAM;

    int ret = getaddrinfo(host.c_str(), NULL, &hint, &answer);
    if (ret != 0) 
    {
        XLOG("ResolveHost getaddrinfo: %s\n", gai_strerror(ret));
        return ret;
    }

    for (curr = answer; curr != NULL; curr = curr->ai_next) 
    {
        inet_ntop(AF_INET, &(((struct sockaddr_in*)(curr->ai_addr))->sin_addr), ipstr, 16);
        XLOG("ResolveHost %s\n", ipstr);
        ip = std::string(ipstr);
        break;
    }

    freeaddrinfo(answer);
    return 0;
}

SimpleTcpClient::SimpleTcpClient()
{
    mClientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (mClientSocket < 0)
        XLOG("create client socket failed");

    bzero(&mClientAddr, sizeof(mClientAddr));
    mClientAddr.sin_family = AF_INET;
    mClientAddr.sin_addr.s_addr = htons(INADDR_ANY);
    mClientAddr.sin_port = htons(0);
}

int SimpleTcpClient::connect(const char* ip, int port)
{
    std::string resolvedIp;
    if (ResolveHost(ip, resolvedIp) < 0)
    {
        XLOG("ResolveHost error");
        return -1;
    }
    
    struct sockaddr_in server_addr;
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = inet_addr(resolvedIp.c_str());
    server_addr.sin_port = htons(port);
    socklen_t server_addr_len = sizeof(server_addr);
    if (::connect(mClientSocket, (sockaddr*)&server_addr, server_addr_len) < 0)
    {
        XLOG("connect failed");
        return -1;
    }
    return 0;
}

int SimpleTcpClient::write(const char* buffer, int length)
{
    return send(mClientSocket, buffer, length, 0);
}

int SimpleTcpClient::read(char* buffer, int length)
{
    return recv(mClientSocket, buffer, length, 0);
}

