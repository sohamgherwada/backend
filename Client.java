import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    
    final String host;
    final int port;


    public static void main(String[] args) throws IOException{
        new Client("127.0.0.1", 5000).run();
    }

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void run() throws IOException{

        Socket client = new Socket(host, port);
        System.out.println("Connected to server!");
        
        PrintStream out = new PrintStream(client.getOutputStream());
        
        Scanner sc = new Scanner(System.in);

        System.out.println("Nickname please: ");
        String nick = sc.nextLine();

        out.println(nick);

        new Thread(new MHandler(client.getInputStream())).start();

        System.out.println("Messages: \n");

        while(sc.hasNextLine()){
            out.println(sc.nextLine());
        }

        out.close();
        sc.close();
        client.close();

    }
}
class MHandler implements Runnable{

    private InputStream server;

    public MHandler(InputStream server){
        this.server = server;
    }

    public void run(){
        Scanner sc = new Scanner(server);
        String tmp = "";
        while(sc.hasNextLine()){
            tmp = sc.nextLine();
            if(tmp.charAt(0) == '['){
                tmp = tmp.substring(1, tmp.length()-1);
                System.out.println("\nUser list: " + new ArrayList<String>(Arrays.asList(tmp.split(","))) +"\n");
            }
            else{
                try{
                    System.out.println("\n" + tagVal(tmp));
                }catch(Exception e){};
            }
        }
        sc.close();
    }
    public static String tagVal(String xml){
        return  xml.split(">")[2].split("<")[0] + xml.split("<span>")[1].split("</span>")[0];
    }
}


