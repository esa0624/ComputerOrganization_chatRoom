#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>

#define MAX 100
typedef struct Client{
        int clientfd;
        char name[40];
}Client;
int serverfd;
pthread_t id;
Client client[MAX] = {};
size_t csize = 0;
int n =0;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

void quit();

void msg_broadcast(char *msg,Client c){
        size_t i;
        printf("%s\n",msg);
        pthread_mutex_lock(&mutex);
        for(i=0;i<csize;i++){
                if(client[i].clientfd != c.clientfd){
                        if(write(client[i].clientfd,msg,strlen(msg))<=0){
                                break;
                        }
                }
        }
        pthread_mutex_unlock(&mutex);
}

void clean(){
pthread_detach(pthread_self());
}
void *server(void *arg){
        Client cl = *(Client*)(arg);
	 while(1){
                char buf[1024]={};
                strcpy(buf,cl.name);
                strcat(buf," :");
                int ret = read(cl.clientfd,buf+strlen(buf),sizeof(buf));
                if(ret <= 0){
                        size_t i;
                        for(i=0;i<csize;i++){
                                if(client[i].clientfd == cl.clientfd){
                                        client[i] = client[csize-1];
                                        csize --;
                                        strcpy(buf,"");
                                        strcat(buf,cl.name);
                                        strcat(buf," leave the channel");
                                        break;
                                }
                        }
                        msg_broadcast(buf,cl);
                        close(cl.clientfd);
                        pthread_exit(0);
                }if(NULL != strstr(buf,"!q")){
                                size_t i;
                        for(i=0;i<csize;i++){
                                if(client[i].clientfd == cl.clientfd){
                                        client[i] = client[csize-1];
                                        csize --;
                                        strcpy(buf,"");
                                        strcat(buf,cl.name);
                                        strcat(buf," leave the channel");
                                        break;
                                }
                        }
			msg_broadcast(buf,cl);
                        close(cl.clientfd);
                        pthread_exit(0);

                                }
		if(NULL != strstr(buf,"!Q")){
		 	strcpy(buf,"server close");
			msg_broadcast(buf,cl);	
			shutdown(serverfd,2);	
                        pthread_exit(0);
		}
                else{
                        msg_broadcast(buf,cl);
		}
        }
	  pthread_exit(0);
}	

int main(int argc,char *argv[]){
        int a =0;
	if(argc != 2){
                printf("Usage: <server_port>\n");
                return -1;
        }
        char *ip = "127.0.0.1";
        int port = atoi(argv[1]);
        

        printf("---------------Server---------------\n");
        printf("Enter !Q to close the server\n");

        serverfd = socket(AF_INET,SOCK_STREAM,0);
        if(serverfd == -1){
                perror("socket");
                return -1;
        }

        struct sockaddr_in addr;
        addr.sin_family = AF_INET;
        addr.sin_port = htons(port);
        addr.sin_addr.s_addr = inet_addr(ip);
        socklen_t addrlen = sizeof(addr);

        int ret = bind(serverfd,(struct sockaddr*)(&addr),addrlen);
        if(ret == -1){
                perror("bind");
                return -1;
        }
        if(listen(serverfd,10)==-1){
                perror("listen");
                return -1;
        }
	//thread for server to control itself
        pthread_create(&id, NULL, (void *)(quit), NULL);

        while(1){
                struct sockaddr_in clientaddr;
                socklen_t len = sizeof(clientaddr);
                printf("waiting for connection....\n");
                int clientfd = accept(serverfd,(struct sockaddr*)(&clientaddr),&len);
                if(clientfd == -1){
                        //perror("accept");
                        //return -1;

			pthread_detach(id);

			close(serverfd);
			break;
                }
                char buf[100]={};
                read(clientfd,&client[csize].name,40);
                client[csize].clientfd = clientfd;
                strcpy(buf,"");
                strcat(buf,client[csize].name);
                strcat(buf," join the channel");
                msg_broadcast(buf,client[csize]);
                ret = pthread_create(&id,NULL,server,&client[csize]);
		csize++;
		if(ret != 0){
                        printf("pthread_create:%s\n",strerror(ret));
                        continue;
                }
                 printf("one client connect:ip <%s> port [%hu]\n",inet_ntoa(clientaddr.sin_addr),ntohs(clientaddr.sin_port));
                }

        return 0;
}
void quit(){

        char text[10];
        while(1){
                scanf("%s",text);
                if(strcmp(text, "!Q") == 0){
                        printf("Server close...\n");
                        size_t i;
                        pthread_mutex_lock(&mutex);
                        for(i=0;i<csize;i++){

                                if(write(client[i].clientfd,"Server close...",strlen("Server close..."))<=0){
                                        break;
                                }


                        }
			for (i=0;i<csize;i++){
			     close(client[i].clientfd);
			     pthread_detach(pthread_self());
			}
                        pthread_mutex_unlock(&mutex);
                        shutdown(serverfd,2);
		
                        pthread_exit(0);
                        exit(0);

                }

        }

}

