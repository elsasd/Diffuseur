import java.io.*;
import java.net.Socket;

public class Registration implements Runnable{
    private final String id_diff;
    private final String address;
    private final int port1;
    private final int port2;


    Registration(String id_diff, String address, int port1, int port2){
        this.id_diff= id_diff;
        this.address= address;
        this.port1= port1;
        this.port2= port2;
    }

    @Override
    public void run() {
        try{
            Socket socket= new Socket("localhost", 5050);
            BufferedReader br= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw= new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.println("REGI "+ id_diff+ " "+ address+ " "+ port1+ " "+ port2+"\r\n");
            pw.flush();
            String msg= br.readLine();
            if(msg.equals("RENO")){
                System.err.println("Not registered.");
                socket.close();
                pw.close();
                br.close();
            }else{
                System.out.println(msg);
            }
            /* TODO else le diffuseur est sensé recevoir des messages du gestionnaire pour vérifier qu'il est
            tjr vivant
             */

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
