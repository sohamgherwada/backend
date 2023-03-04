import java.util.Scanner;

public class test {
    public static void main(String[]args){
        System.out.println("What is your name?");
        Encription en ;
        Decription de ;
        Scanner sc = new Scanner(System.in);
        String a =  sc.nextLine();
        en = new Encription(a);
        String Encripted = en.security();
        System.out.println(Encripted);
        de = new Decription(Encripted);
        String Decripted = de.uncode();
        System.out.println(Decripted);
    }
}
