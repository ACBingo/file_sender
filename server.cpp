#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#define BUF_SIZE 1024
int main(){

    char filename[]="test.txt";
    FILE *fp = fopen(filename,"rb");
    if (fp == NULL){
        printf("Cannot open file, press any key to exit!\n");
        getchar();
        exit(0);
    }
    //创建套接字
    int serv_sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

    //将套接字和IP、端口绑定
    struct sockaddr_in serv_addr;
    memset(&serv_addr, 0, sizeof(serv_addr));  //每个字节都用0填充
    serv_addr.sin_family = AF_INET;  //使用IPv4地址
    //serv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");  //具体的IP地址
    serv_addr.sin_port = htons(1234);  //端口
    bind(serv_sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr));

    printf("正在监听端口1234...\n");
    //进入监听状态，等待用户发起请求
    listen(serv_sock, 20);

    


    //接收客户端请求
    struct sockaddr_in clnt_addr;
    socklen_t clnt_addr_size = sizeof(clnt_addr);
    int clnt_sock = accept(serv_sock, (struct sockaddr*)&clnt_addr, &clnt_addr_size);

    printf("收到呼入请求,正在建立连接\n");
    printf("正在传输文件\n");

    char buffer[BUF_SIZE];
    int nCount;
    while ((nCount=fread(buffer,1,BUF_SIZE,fp))>0){
        //printf("%d\n",nCount);
        //puts(buffer);
        write(clnt_sock,buffer,nCount);
    }


    shutdown(clnt_sock,SHUT_WR);
    read(clnt_sock,buffer,sizeof(buffer));

    fclose(fp);

    //关闭套接字
    close(clnt_sock);
    close(serv_sock);

    printf("文件传输成功,结束程序!");

    return 0;
}
