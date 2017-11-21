
package chatserver_java;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
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

    public List<ClientThread> getClients()
    {
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
    private String chatRoomName;
    private Integer room_Ref;
    
    // Username -- Join ID
    private Hashtable <String,Integer> userDetails = new Hashtable <String,Integer>();
    // Username -- ChatRoom
    private Hashtable <Integer,String> userRoom = new Hashtable <Integer,String>();
    // ChatRoom -- ChatRoom Ref ID
    private Hashtable <String,Integer> chatRoomRef = new Hashtable <String,Integer>();

    //private boolean isAlived;
    private final LinkedList<String> messagesToSend;
    private boolean hasMessages = false;
    private Integer joinID = 100;
    private String studentID = "17306775"; 
    
    

    public ServerThread(Socket socket, String userName,String chatRoom)
    {
        this.socket = socket;
        this.userName = userName;
        this.chatRoomName = chatRoom;
        // Reference number generation
        Random randomGenerator = new Random();
        this.joinID = randomGenerator.nextInt(joinID);
        //this.room_Ref = randomGenerator.nextInt(count);
        //chatRoomRef.put(chatRoom, room_Ref);
        messagesToSend = new LinkedList<>();
        
        // Dictionary Objects
        userDetails.put(userName,joinID);
        userRoom.put(joinID, chatRoom);
        
    /*    if(chatRoomRef.isEmpty())
        {
            System.out.println("ROOM IS EMPTY");
            this.room_Ref = randomGenerator.nextInt(joinID);
            chatRoomRef.put(chatRoom, room_Ref);
        }
        else */
    
        if ((chatRoomRef.containsKey(chatRoom)) == false)
        {
            System.out.println("False...");
            this.room_Ref = randomGenerator.nextInt(joinID);
            chatRoomRef.put(chatRoom, room_Ref);
            
        }
        else if ((chatRoomRef.containsKey(chatRoom)) == true)
        {
            System.out.println(chatRoomRef.containsKey(chatRoom)+ "3True....3" + chatRoomRef.keySet().contains(chatRoom));
            
        }
    }

    public void addNextMessage(String message)
    {
        synchronized (messagesToSend){
            hasMessages = true;
            messagesToSend.push(message);
        }
    }

    @Override
    public void run()
    {
        
        try
        {
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), false);
            InputStream serverInStream = socket.getInputStream();
            Scanner serverIn = new Scanner(serverInStream);
            // BufferedReader userBr = new BufferedReader(new InputStreamReader(userInStream));
            // Scanner userIn = new Scanner(userInStream);
           
            System.out.println("JOINED_CHATROOM :" + userRoom.get(joinID));
            System.out.println("SERVER_IP :" + socket.getRemoteSocketAddress());
            System.out.println("PORT :" + socket.getPort());
            System.out.println("ROOM_REF : " + chatRoomRef.get((userRoom.get(joinID))));
            System.out.println("JOIN_ID : " + joinID);
            //serverOut.println(userName + " Joined the chat");
            //serverOut.println("Welcome :" + userName);
            //serverOut.println("Socket Port No :" + socket.getPort());            
            serverOut.flush();
            
          
            while(!socket.isClosed())
            {
                if(serverInStream.available() > 0)
                {
                    if(serverIn.hasNextLine())
                    {
                        System.out.println(serverIn.nextLine());
                    }
                }
                if(hasMessages)
                {
                    String nextSend = "";
                    String[] Helo = new String[5];
                    synchronized(messagesToSend)
                    {
                        nextSend = messagesToSend.pop();
                        hasMessages = !messagesToSend.isEmpty();
                        Helo = nextSend.split(" ", 0);
                    }
                    if(nextSend.contains("LEAVE_CHATROOM"))
                      { 
                        //serverOut.println(userName + "Exited from chat " + this.socket);
                        //System.out.println("You have succesfully Exited from chat");
                        serverOut.println("LEFT_CHATROOM :" + chatRoomRef.get(userName));
                        serverOut.println("JOIN_ID:" + userDetails.get(userName));
                        serverOut.flush();
                        //System.out.println("Client " + this.socket + " requested exit");
                        //System.out.println("Closing this connection.");
                        this.socket.close();
                        //System.out.println(userName + "LEFT_CHATROOM");
                        //System.out.println("JOIN_ID:" + userDetails.get(userName));
                        
                         break;
                     }
                    else if(Helo[0].contains("HELO"))
                    {
                        System.out.println(nextSend);
                        System.out.println("SERVER_IP:" + socket.getRemoteSocketAddress());
                        System.out.println("PORT:" + socket.getPort());
                        System.out.println("STUDENT_ID:" + studentID);
                    }
                    else
                    {
                        serverOut.println(userName + " >> " + nextSend);
                    }
                    serverOut.flush();
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

    }
}
    
class ClientThread implements Runnable 
{
    private Socket socket;
    private PrintWriter clientOut;
    private ChatServer server;

    public ClientThread(ChatServer server, Socket socket){
        this.server = server;
        this.socket = socket;
    }

    private PrintWriter getWriter(){
        return clientOut;
    }

    @Override
    public void run() {
        try{
            // setup
            this.clientOut = new PrintWriter(socket.getOutputStream(), false);
            Scanner in = new Scanner(socket.getInputStream());

            // start communicating
            while(!socket.isClosed())
            {
                if(in.hasNextLine())
                {
                    String input = in.nextLine();
                    System.out.println(input + "************");
                     
                    for(ClientThread thatClient : server.getClients())
                    {
                        PrintWriter thatClientOut = thatClient.getWriter();
                        if(thatClientOut != null)
                        {
                            thatClientOut.write(input + "\r\n");
                           // System.out.println(input + "**");
                            thatClientOut.flush();
                        }
                    }
                }
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}
