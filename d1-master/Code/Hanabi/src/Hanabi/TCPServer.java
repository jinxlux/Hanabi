package Hanabi;

import java.io.*;
import java.net.*;

class TCPServer {

  public static void main(String argv[]) throws Exception {
    String clientSentence;
    String capitalizedSentence;
    ServerSocket welcomeSocket = new ServerSocket(8080);

    while (true) {
      Socket connectionSocket = welcomeSocket.accept();
      System.out.println("Connected");

      BufferedReader inFromClient =
          new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

      DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

      clientSentence = inFromClient.readLine();

      System.out.println("Received: " + clientSentence);
      capitalizedSentence = clientSentence + '\n';
      outToClient.writeBytes(capitalizedSentence);
    }
  }
}