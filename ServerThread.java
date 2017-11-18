
package chatserver_java;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.*;
/**
public class ServerThread implements Runnable
{
    private Socket socket;
    private String userName;
    private Hashtable <String,Integer> userDetails = new Hashtable <String,Integer>();
    private boolean isAlived;
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
                    if(nextSend.equals("Exit"))
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
**/