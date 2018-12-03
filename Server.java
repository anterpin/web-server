
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.StringTokenizer;

import java.util.Date;
import java.util.*;
import java.io.*;
import javax.net.ssl.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;
 
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


//Each client connection will be managed to a dedicated thread
class Server 
{
    static final String WEB_ROOT = "/web/root/path/";
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final boolean HTTPS = true;
    //port to listen to
    static final int PORT = 8000;
    //verbose mode
    static boolean VERBOSE = false;

    
    //connection via socket class
    private boolean running = true;
    
    //socket objects
    private ServerSocket serverSocket = null;
    private SSLServerSocketFactory ssf = null;

    //managing connections
    private Map<Integer,Thread> connections = new HashMap<Integer,Thread>();
    private List<Integer> stoppedConnections = new ArrayList<Integer>();

    private Thread consoleThread = null;

    //instance
    static Server instance = null;

    private Server()
    {
        SSLManager.trust();
    }
    
    public static Server getInstance()
    {
        if(instance == null)
        {
            instance = new Server();
        }

        return instance;
    }

    public void connectionClosed(int connectionId)
    {
        stoppedConnections.add(connectionId);
        connections.remove(connectionId);
    }

    public void generateConnection(Socket socket)
    {
        int connectionId = 0;
        if(stoppedConnections.isEmpty())
        {
            connectionId = connections.size();
        }
        else
        {
            connectionId = stoppedConnections.get(0);
            stoppedConnections.remove(0);
        }
        Connection connection = new Connection(connectionId,(SSLSocket) socket);

        if(VERBOSE)
            System.out.println("New connection opened " + new Date());

        Thread thread = new Thread(connection);

        connections.put(connectionId,thread);

        thread.start();
    }

    public static void main(String[] args)
    {
        Server server = Server.getInstance();
        server.run();
    }

    private void shutDownServer()
    {
        running = false;
        
        try
        {
            if(!serverSocket.isClosed())
                serverSocket.close();

            //close all opened thrads
            for(Thread conn : connections.values())
            {
                conn.interrupt();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        
    }
    private void verbose(String[] args)
    {
        switch(args[0])
        {
            case "true":
                VERBOSE = true;
                break;
            case "false":
                VERBOSE = false;
                break;
            default:
                System.out.println("Unkown "+args[0]);
                break;
        }
    }

    public void command(String command,String[] args)
    { 
        switch(command)
        {
            case "":
                break;
            case "quit":
                shutDownServer();
                break;
            case "echo":
                Arrays.asList(args).stream().forEach(s -> System.out.print(s + " "));
                System.out.println();
                break;
            case "verbose":
                verbose(args);
                break;
            default:
                System.err.println(command + " is not recognized");
                break;
        }
    }


    public void run()
    {

        try
        {
            if(HTTPS)
            {
                ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
                serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT);
            }
            else
                serverSocket = new ServerSocket(PORT);
            

            consoleThread = new Thread(new ServerCmd());
            consoleThread.start();

            while(running)
            {
                
                generateConnection(serverSocket.accept());
                
            }
            if(!serverSocket.isClosed())
                serverSocket.close();
    
        }
        catch(IOException e)
        {
            if(VERBOSE)
                e.printStackTrace();
        }
    }

    

};
