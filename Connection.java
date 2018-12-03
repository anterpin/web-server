
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.StringTokenizer;

import java.util.Date;
import java.io.*;
import javax.net.ssl.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;


public class Connection implements Runnable 
{
    //connection stuff
    private Socket connection = null;
    private int connectionId = 0;


    private Map<String,String> headers = null;

    Connection(int connId,Socket conn)
    {
        headers = new HashMap<String,String>();
        connection = conn;
        connectionId = connId;
    }
    private void parseHeader(InputStream inputStream)throws IOException
    {
        int charRead;
        StringBuffer sb = new StringBuffer();
        while (true) {
            sb.append((char) (charRead = inputStream.read()));
            if ((char) charRead == '\r') {            // if we've got a '\r'
                sb.append((char) inputStream.read()); // then write '\n'
                charRead = inputStream.read();        // read the next char;
                if (charRead == '\r') {                  // if it's another '\r'
                    sb.append((char) inputStream.read());// write the '\n'
                    break;
                } else {
                    sb.append((char) charRead);
                }
            }
        }
    
        String[] headersArray = sb.toString().split("\r\n");
        
        headers.put(headersArray[0].split(" ")[0],headersArray[0].split(" ")[1]);
        for (int i = 1; i < headersArray.length - 1; i++) 
        {
            headers.put(headersArray[i].split(": ")[0],
                    headersArray[i].split(": ")[1]);
        }
    }

    @Override
    public void run()
    {
        try {
            // Start handling application content
            InputStream inputStream = connection.getInputStream();
            OutputStream outputStream = connection.getOutputStream();
            parseHeader(inputStream);
            
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
             

            handleRequest(printWriter);
            connection.close();
        } 
        catch (Exception e) 
        {
            if(Server.VERBOSE)
                e.printStackTrace();
        }
        Server.getInstance().connectionClosed(connectionId);
    }

    private StringBuffer responseHeader()
    {
        // HTTP/1.1 200 OK
        // Date: Sun, 11 Mar 2007 15:38:49 GMT
        // Server: Myserver
        // Cache-Control: no-store, no-cache, must-revalidate
        // Expires: Fri, 15 Aug 1976 18:15:00 GMT
        // X-Powered-By: PHP/4.3.10-18
        // Pragma: no-cache
        // Last-Modified: Sun, 11 Mar 2007 15:38:49 GMT
        // Cache-Control: post-check=0, pre-check=0
        // Content-Encoding: gzip
        // Connection: close
        // Content-Type: text/html; charset=iso-8859-1 
        Date date = new Date();
        StringBuffer ss = new StringBuffer();
        ss.append("HTTP/1.1 200 OK\n");
        ss.append("Date: " + date);
        ss.append("\nServer: Myserver\n");
        ss.append("Cache-Control: no-store, no-cache, must-revalidate\n");
        ss.append("Expires: " + new Date(date.getTime() + 24*60*60));
        ss.append("Content-Encoding: gzip\nConnection: close\nContent-Type: text/html; charset=iso-8859-1\n");

        return ss;
    }
    private String readFileContent(String fileName)
    {
        
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

			StringBuffer content = new StringBuffer("");
            String line;
            while ((line = br.readLine()) != null) 
            {
				content.append(line + "\n");
            }
            br.close(); 
            return content.toString();   
        } 
        catch (IOException e) 
        {
            if(Server.VERBOSE)
			    e.printStackTrace();
        }
        return new String();
    }

    private String handlePython(String fileName)
    {
        try
        {
            ProcessBuilder builder = new ProcessBuilder("python",fileName);
            builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = builder.start();
            StringBuilder content = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                reader.lines().forEach(line -> content.append(line + "\n"));
            }
            return content.toString();
        }
        catch(IOException e)
        {
            if(Server.VERBOSE)
                e.printStackTrace();
        }
        return new String();
        
    }

    private void handleRequest(PrintWriter writer)
    {
        String tokens = headers.get("GET");
        String fileName = Server.DEFAULT_FILE;
        if(tokens != null)
        {
            fileName = tokens.split(" ")[0];
        }
        File f = new File(Server.WEB_ROOT + fileName);
        if(!f.exists() || f.isDirectory()) 
        { 
            fileName = Server.FILE_NOT_FOUND;
        }
        
        String content=null;
        if(fileName.endsWith(".py"))
        {
           content = handlePython(Server.WEB_ROOT + fileName);
        }
        else
        {
            content = readFileContent(Server.WEB_ROOT + fileName);
        }
        String response = responseHeader().append("\n\n\n"+content).toString();

        writer.write(response);
        writer.flush();
    }
};
