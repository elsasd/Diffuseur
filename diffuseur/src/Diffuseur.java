import java.util.*;
import java.io.*;
import java.net.*;


public class Diffuseur {
    private final int port_recep;
    private final int port_diff;
    private final String id_diff;
    private final String adress_diff;
    /*créer ensuite la liste des messages*/
    /*une pile pour gérer l'ordre de diffusion des messages des clients*/
    private final LinkedList<String> list = new LinkedList<>();
    /*une liste pour gérer les messages du diffuseur, d'après un fichier texte ou peut importe*/
    public LinkedList<String> pile = new LinkedList<>();


    Diffuseur(int port_diff, int port_recep, String id_diff, String adress_diff) throws UnknownHostException {
        this.adress_diff = Util.ip(adress_diff);
        String adress_local = InetAddress.getLocalHost().getHostAddress();
        System.out.println(adress_local);
        id_diff = Util.diese(id_diff, 8);
        this.id_diff = Util.diese(id_diff, 8);
        this.port_diff = port_diff;
        this.port_recep = port_recep;
    }

    public void diffuse() {
        MultiCast multi = new MultiCast(port_diff, adress_diff, pile);
        Thread th = new Thread(multi);
        th.start();
    }

    public void connect() {
        TcpConnexion connect = new TcpConnexion(port_recep, pile);
        Thread th = new Thread(connect);
        th.start();
    }

    public void register() {
        Registration registration = new Registration(id_diff, adress_diff, port_diff, port_recep);
        Thread th = new Thread(registration);
        th.start();
    }

    public void parseFile(String fileName) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(fileName));
        while (sc.hasNext()) {
            list.add(sc.nextLine());
        }
    }

    public void setPile(String fileName) throws FileNotFoundException {
        parseFile(fileName);
        //pile.addAll(list);
        //diffusion du message de la forme :
        //DIFF 000n id(len : 8) message(len : 140)
        for (String ad : list) {
            int len = (ad.length() % 140 == 0) ?
                    ad.length() / 140 :
                    ad.length() / 140 + 1;
            if (len == 0) continue;
            for (int j = 0; j < len - 1; j++) {
                pile.add("DIFF 0000 " + id_diff + " " + ad.substring(j * 140, (j + 1) * 140) + "\r\n");
            }

            //On ajoute les dieses necessaires (ne devrait pas etre ici)
            StringBuilder ad1 = new StringBuilder(ad.substring((len - 1) * 140));
            int nbDiese = 140 - ad1.length();
            ad1.append("#".repeat(Math.max(0, nbDiese)));
            pile.add("DIFF 0000 " + id_diff + " " + ad1 + "\r\n");
            //incNumMes();
        }
    }

    public void list(String add, int port) {
        try {
	        add = Util.ip(add);
            //On connecte la socket au port du gestionnaire
            Socket socket = new Socket(add, port);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.print("LIST\r\n");
            pw.flush();

            String mess = br.readLine();
            System.out.println(mess);
            String[] splited = mess.split(" ");

            int len = Integer.parseInt(splited[1]);
            for (int i = 0; i < len; i++) {
                mess = br.readLine();
                System.out.println(mess);
            }

            pw.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
