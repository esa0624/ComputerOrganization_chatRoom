# 2022 Chat System
This repository contains C code for a text based chat system.

Team members:
  - Esa Chen
  - Junhao Qu

Extended Features:
  - Change the font color to highlight messages in C program
      - messages sent by other clients: Blue color
      - \<username\> in the message printed at first "Your username is \<username\>": Cyan color
  - Implement the client/server model with threads and GUI in Java
    - have all the same base functionality as C program
    - A button "Close Server" in the GUI window of server to close the server and send message to all clients
    - A button "Send message" in the GUI window of client to send message. (client can also press enter to send text)
    - Closing the window of client makes the client leave and the server will broadcast to all other clients
    - Closing the window of server closes the server 
    
