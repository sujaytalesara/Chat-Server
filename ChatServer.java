
package chatserver_java;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class ChatServer 
{

    private int serverPort;
    private static final int portNumber = 4243;
    private List<ClientThread> clients;

     public ChatServer(int portNumber)
    {
        this.serverPort = portNumber;
    }

    public List<ClientThread> getClients(){
        return clients;
    }

    private void startServer()
    {
        clients = new ArrayList<ClientThread>();
        ServerSocket serverSocket = null;
        try 
        {
            serverSocket = new ServerSocket(serverPort);
            acceptClients(serverSocket);
        } 
        catch (IOException e){
            System.err.println("Could not listen on port: "+serverPort);
            System.exit(1);
        }
    }

    private void acceptClients(ServerSocket serverSocket)
    {

        System.out.println("server starts port = " + serverSocket.getLocalSocketAddress());
        while(true)
        {
            try
            {
                Socket socket = serverSocket.accept();
                System.out.println("accepts : " + socket.getLocalSocketAddress());
                ClientThread client = new ClientThread(this, socket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } 
            catch (IOException ex)
            {
                System.out.println("Accept failed on : " + serverPort);
            }
        }
    }
    
    public static void main(String[] args)
    {
        ChatServer server = new ChatServer(portNumber);
        server.startServer();
    }
}
    
