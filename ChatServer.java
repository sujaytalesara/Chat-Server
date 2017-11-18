
package chatserver_java;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class ChatServer  
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
            //System.err.println("Could not listen on port: " + serverPort);
            System.err.println("ERROR_DESCRIPTION: " + e.getMessage());
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
                // Initiating client call
                ClientThread client = new ClientThread(this, socket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } 
            catch (IOException ex)
            {
                //System.out.println("Accept failed on : " + serverPort);
                System.out.println("ERROR_DESCRIPTION: " + ex.getMessage());
            }
        }
    }
    
    public static void main(String[] args)
    {
        ChatServer server = new ChatServer(portNumber);
        server.startServer();
    }
}

class ServerThread implements Runnable
{
    private Socket socket;
    private String userName;
    private Hashtable <String,Integer> userDetails = new Hashtable <String,Integer>();
    //private boolean isAlived;
    private final LinkedList<String> messagesToSend;
    private boolean hasMessages = false;
    private Integer count = 0;
    Random randomGenerator = new Random();

    public ServerThread(Socket socket, String userName){
        this.socket = socket;
        this.userName = userName;
        count = randomGenerator.nextInt(10000);
        userDetails.put(userName,count);
        messagesToSend = new LinkedList<String>();
    }

    public void addNextMessage(String message){
        synchronized (messagesToSend){
            hasMessages = true;
            messagesToSend.push(message);
        }
    }

    @Override
    public void run()
    {
        //System.out.println("Welcome :" + userName);
        //System.out.println("Local Port :" + socket.getLocalPort());
        //System.out.println("Server = " + socket.getRemoteSocketAddress() + ":" + socket.getPort());
        //System.out.println("Joining ID : " + count);

        try
        {
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), false);
            InputStream serverInStream = socket.getInputStream();
            Scanner serverIn = new Scanner(serverInStream);
            // BufferedReader userBr = new BufferedReader(new InputStreamReader(userInStream));
            // Scanner userIn = new Scanner(userInStream);

            serverOut.println(userName + " Joined the chat");
            serverOut.println("Joining ID : " + count);
            //serverOut.println("Welcome :" + userName);
            serverOut.println("Local Port :" + socket.getLocalPort());
            serverOut.println("Server IP :" + socket.getRemoteSocketAddress());
            serverOut.println("Socket Port No :" + socket.getPort());            
            serverOut.flush();
            while(!socket.isClosed())
            {
                if(serverInStream.available() > 0)
                {
                    if(serverIn.hasNextLine()){
                        System.out.println(serverIn.nextLine());
                    }
                }
                if(hasMessages)
                {
                    String nextSend = "";
                    synchronized(messagesToSend)
                    {
                        nextSend = messagesToSend.pop();
                        hasMessages = !messagesToSend.isEmpty();
                    }
                    if(nextSend.contains("LEAVE_CHATROOM"))
                      { 
                        serverOut.println(userName + "Exited from chat " + this.socket);
                        serverOut.flush();
                        //System.out.println("Client " + this.socket + " requested exit");
                        //System.out.println("Closing this connection.");
                        this.socket.close();
                        System.out.println("Connection closed");
                        
                         break;
                     }
                    serverOut.println(userName + " > " + nextSend);
                    serverOut.flush();
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

    }
}
    
