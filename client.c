#include <stdio.h>
#include <time.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#define CYAN "\x1B[36m"
#define BLUE "\x1B[34m"
#define NRM "\x1B[0m"
 main(int argc,char *argv[]){
        
	 
	if(argc != 4){
                fprintf(stderr,"Usage: %s <server_ip> <server_port> <username>\n",argv[0]);
                return -1;
        }
        char *ip = argv[1];
        int port = atoi(argv[2]);
	char *name = argv[3];

        int sfd = socket(AF_INET,SOCK_STREAM,0);
        if(sfd == -1){
                perror("socket");
                return -1;
        }
        struct sockaddr_in addr;
        addr.sin_family = AF_INET;
        addr.sin_port = htons(port);
        addr.sin_addr.s_addr = inet_addr(ip);
        socklen_t addrlen = sizeof(addr);

        int ret = connect(sfd,(const struct sockaddr*)(&addr),addrlen);
        if(ret == -1){
                perror("connect");
                return -1;
        }
	printf("---------Welcome to the Chat Room---------\n");
	printf("Enter !q to quit the chat room\n");
        printf("Enter !Q to close the server\n");
	printf("Successfully connect the server!\n");
        printf("Your username is ");
	printf(CYAN "%s",name);
	printf(NRM "\n");
        
	write(sfd,name,strlen(name)+1);
        pid_t pid = fork();
        if(pid == -1){
                perror("fork");
                return -1;
        }
        if(pid == 0){
                while(1){
                        char buf[1024]={};
                        scanf("%s",buf);
                        if(write(sfd,buf,strlen(buf)+1)<=0){
                                break;
                        }
                }
        }else{
                while(1){
                        char buf[1024]={};
                        if(read(sfd,buf,1024)<=0){
                                //close(sfd);
				break;
                        }
			if(NULL != strstr(buf,"server close")){
				strcpy(buf,"!Q");
				write(sfd,buf,strlen(buf)+1);
			}
                        printf(BLUE "%s",buf);
			printf(NRM "\n");
                }
        }
        close(sfd);
        return 0;
}


                                         
