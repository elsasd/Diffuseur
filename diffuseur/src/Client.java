import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    static private String id_client;
    
    public static void main(String[] args) {
        if(args.length < 1){
            System.err.println("La ligne de commande doit être de la forme \"java Client1 id_client\".");
        }else {
            id_client= Util.diese(args[0],8);
            Window listening = new Window();
	    
	    //On commence la diffusion
            Thread th_diffusion = new Thread(listening);
            th_diffusion.start();

	    while (true){
		Scanner sc = new Scanner(System.in);
		String msg = sc.nextLine();

		if(msg.equals("LIST")){
		    list();
		}
		else if (msg.equals("MESS") || msg.equals("LAST")){
			try{
			Socket socket = new Socket("172.20.10.11", 5252);
		    if (msg.charAt(0)=='M')
				msg(socket);
			else 
				last(socket);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		else{
		    System.err.println("Les seules commandes reconnues sont :\n\"MESS\" - envoyer un message à diffuser\n\"LIST\" - demander au gestionnaire la liste des diffuseurs\n\"LAST\" - demander à voir les derniers messages"); 
		}
	    }
	    /*
            CliCommunication messaging = new CliCommunication(id_client);
            Thread th_tcp = new Thread(messaging);
            GestCommunication gestCommunication = new GestCommunication();
            Thread th_gest = new Thread(gestCommunication);
            th_gest.start();
            th_tcp.start();*/
        }
    }

    static void list(){
	try{
	    Socket socket = new Socket("localhost", 5050);
	    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	    pr.println("LIST\r\n");
	    pr.flush();
	    //affichage de la réponse du diffuseur
	    String msg = br.readLine();
	    System.err.println(msg);
	    int num = Integer.parseInt(msg.split(" ")[1]);
	    //VERIFIER LE FORMAT DE NUM
	    for (int i = 0; i < num; i++) {
		System.err.println(br.readLine());
	    }
	    pr.close();
	    br.close();
	    socket.close();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    static void msg(Socket socket){
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	    Scanner sc = new Scanner(System.in);
	    //System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

	    System.err.println("Entrez le message à envoyer au diffuseur.");
	    String input= sc.nextLine();
	    while(input.length()>140){
		System.err.println("Votre message doit avoir une taille maximum de 140 caractères.");
		input=sc.nextLine();
	    }
	    int nbDiese = 140 - (input.length());
	    pw.println("MESS " + id_client + " " + input + "#".repeat(nbDiese)+"\r\n");
	    pw.flush();
	    String msg = br.readLine();
	    if (msg.equals("ACKM")) {
		System.err.println(msg);
		pw.close();
		br.close();
		socket.close();
	    }else{
		System.err.println("Oups, il semblerait qu'il y ait une erreur, réessayez.");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    static void last(Socket socket){
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	    Scanner sc = new Scanner(System.in);
	    //System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

	    System.err.println("Combien de messages souhaitez-vous voir ?");
	    try{
		int nb_mess= sc.nextInt();
		while (nb_mess <= 0 || nb_mess >= 1000){
		    System.err.println("Votre nombre doit être compris entre 1 et 999 inclus.");
		    nb_mess= sc.nextInt();
		}
		pw.print("LAST "+Util.zero(Integer.toString(nb_mess),3)+"\r\n");
		pw.flush();
		String msg=br.readLine();
		System.err.println(msg);
		while(!msg.equals("ENDM\r\n")){
		    msg=br.readLine();
		    if (msg==null) break;
		    System.err.println(msg);
		}
		pw.close();
		br.close();
		socket.close();
	    
	    }catch (InputMismatchException e){
		System.err.println("Commande avortée, vous devez insérer un nombre (entre 1 et 999).");
		last(socket);
		return;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    static class Window implements Runnable {
        @Override
        public void run() {
            MulticastSocket mso;
            try {
                mso = new MulticastSocket(5151);
                mso.joinGroup(InetAddress.getByName("225.010.020.030"));

                JFrame window = new JFrame();
                window.setTitle("Bienvenue sur notre NetRadio!");
                //window.setSize(700, 500);
                window.setLocationRelativeTo(null);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //window.setResizable(true);
                JPanel pan = new JPanel();
                pan.setLayout(new BorderLayout());
                JTextArea textArea = new JTextArea(50, 60);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                pan.add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
                RedirectOutputStream redirectOutputStream = new RedirectOutputStream(textArea, "NetRadio");
                System.setOut(new PrintStream(redirectOutputStream));
                window.getContentPane().add(pan);
                window.setPreferredSize(new Dimension(1250, 600));
                window.pack();
                window.setVisible(true);
                //window.setPreferredSize(new Dimension(400, 300));

                while (true) {
                    byte[] data = new byte[500];
                    DatagramPacket paquet = new DatagramPacket(data, data.length);
                    mso.receive(paquet);
                    String msg = new String(paquet.getData(), 0, data.length);
                    System.out.print(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}