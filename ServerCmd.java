import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;

public class ServerCmd implements Runnable
{
    ServerCmd()
    {
    }

    @Override
    public void run()
    {
        boolean running = true;
        while(running)
        {
            String command = null;
            String line = null;
            System.out.flush();
            System.out.print("server-console > ");
            try
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                line = br.readLine();
            }
            catch(IOException e)
            {
                if(Server.VERBOSE)
                    e.printStackTrace();
            }

            List<String> list = new ArrayList<String>();
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
            while (m.find())
                list.add(m.group(1).replace("\"", ""));//replace the double quotes

            
            if(list.isEmpty())
                continue;
            
            command = list.get(0);
            list.remove(0);
            

            if(command.equals("quit"))
                running = false;
    
            String[] args = new String[list.size()];
            args = list.toArray(args);
            
            Server.getInstance().command(command,args);
        }
    }   
};