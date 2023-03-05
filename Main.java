import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args)  {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to Bubble! Would you like to create a chatroom or simply connect to a chatroom? Type 1 to create, type 2 to connect, then press enter!");
        String in = sc.nextLine();
        if(Integer.parseInt(in) == 1){
            System.out.println("Understood! For simplicity's sake, we'll use the IP address on this device. Before creating, I need the port number you want to use:");
            String in2 = sc.nextLine(); // for now, just use local host;
            try{
                new Thread(new Server(Integer.parseInt(in2))).start();
                System.out.println("If you're seeing this that means the server was created! Now, we'll create your client!");
                Thread.sleep(1000);
                new Client("localhost", Integer.parseInt(in2)).run();
                System.out.println("If you're seeing this your client was created!");
            } catch(Exception e){
                System.out.println("Oops, something went wrong! Bye!");
                e.printStackTrace();
            }
        }
        if(Integer.parseInt(in) == 2){
            System.out.println("Understood! What's the port number of the chatroom?\n");
            String in3 = sc.nextLine();
            new Client("127.0.0.1", Integer.parseInt(in3));
        }
    }
}
