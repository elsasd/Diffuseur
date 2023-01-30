import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.*;

public class Gestionnaire {
    //port de communication, nombre maximal de diffuseurs
    //et liste de diffuseurs
    private final int port;
    private final ArrayList<String> annuaire;

    Gestionnaire(int port) {
        annuaire = new ArrayList<>();
        this.port = port;
    }

    /*rajouter une fonction qui permet de vérifier si les diffuseurs sont toujours actifs*/

    void gestionne() {
        try {
            //On connecte la socket au port du gestionnaire
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                int maxDiff = 10;
                Communication communication = new Communication(socket, annuaire, maxDiff);
                Thread th = new Thread(communication);
                th.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Communication implements Runnable {
        private final Socket socket;
        private final ArrayList<String> annuaire;
        private final int maxDiff;

        public Communication(Socket socket, ArrayList<String> annuaire, int maxDiff) {
            this.socket = socket;
            this.annuaire = annuaire;
            this.maxDiff = maxDiff;
        }

        @Override
        public void run() {
            synchronized (annuaire) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    String mess = br.readLine();
                    String[] splited = mess.split(" ");
                    //Si on cherche à enregistrer un diffuseur :
                    if (splited[0].equals("REGI")) {
                        splited[2] = Util.ip(splited[2]);
                        //Si la taille maximale est atteinte
                        //On envoie RENO et on ferme la connexion
                        if (annuaire.size() == maxDiff) {
                            pw.println("RENO\r\n");
                            pw.flush();
                            pw.close();
                            br.close();
                            socket.close();
                        } else {
                            //Si la taille maximale n'est pas atteinte
                            //On verifie le format des donnees :
                            // REGI id###### ip1(len : 12) port1(len:4) ip2 port2
                            boolean check = (splited.length == 6
                                    && splited[1].length() == 8
                                    && splited[3].matches("\\d\\d\\d\\d")
                                    && splited[5].matches("\\d\\d\\d\\d"));
                            if (!check) {
                                pw.println("Erreur d'identifiant ou de port.");
                                pw.flush();
                                pw.close();
                                br.close();
                                socket.close();
                            }
                            String msg = mess.substring(5);
                            if (annuaire.contains(msg)) {
                                pw.println("RENO\r\n");
                                pw.flush();
                            } else {
                                annuaire.add(msg);
                                pw.println("REOK\r\n");
                                pw.flush();
                            }
                            pw.flush();
                            /*TODO: NE PAS FERMER LA CONNEXION ET BERIFIER SI LE DIFF EST TJR VIVANT*/
                            /* On maintient la connexiona avec le diffuseur pour lui envoyer des messages
                             * avec un certain interval pour vérifier qu'il est tjr vivant
                             * s'il est mort, on fermera la conenxion à ce moment-là. */
                        }
                    }
                    //Si on cherche a lister les diffuseurs :
                    else if (splited[0].equals("LIST") && splited.length == 1) {
                        int sizeAn = annuaire.size();
                        String nbDiff = (sizeAn > 9) ? Integer.toString(sizeAn) : "0" + sizeAn;
                        pw.print("LINB " + nbDiff + "\r\n");
                        pw.flush();
                        for (String s : annuaire) {
                            pw.print("ITEM " + s + "\r\n");
                            pw.flush();
                        }
                        pw.close();
                        br.close();
                        socket.close();
                    } else {
                        pw.println("Les seules commandes acceptées sont LIST et REGI.");
                        pw.flush();
                        pw.close();
                        socket.close();
                        br.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        Gestionnaire gestionnaire = new Gestionnaire(5050);
        gestionnaire.gestionne();
    }
}
