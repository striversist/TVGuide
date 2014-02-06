#ifndef SimpleTcpClient_h
#define SimpleTcpClient_h

#include <sys/socket.h>
#include <arpa/inet.h>

class SimpleTcpClient
{
public:
    SimpleTcpClient();
    int connect(const char* ip, int port);
    int write(const char* buffer, int length);
    int read(char* buffer, int length);

private:
    int mClientSocket;
    sockaddr_in mClientAddr;

};


#endif
