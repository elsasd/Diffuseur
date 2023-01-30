import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class DiffuseurLauncher {
    public static void main(String[] args) throws FileNotFoundException, UnknownHostException {
        /*TODO: il faut lire les informations concernant le diffuseur à partir d'un fichier */
        Diffuseur diffuseur= new Diffuseur(5151, 5252, "DIF_ONE", "225.010.020.030");
        diffuseur.setPile("crash2.txt");
        diffuseur.diffuse();
        diffuseur.connect();
        Scanner sc= new Scanner(System.in);
        String msg; 
        while(true){
            msg = sc.nextLine();
            if(msg.equals("REGI")) {
                /*pour s'enregistrer auprès du gestionnaire*/
                diffuseur.register();
            }else if(msg.equals("LIST")){
                diffuseur.list("127.0.0.1", 5050);
            }
        }
    }
}
