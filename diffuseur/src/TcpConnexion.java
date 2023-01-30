import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class TcpConnexion implements Runnable{
    private final int port_recep;
    final LinkedList<String> pile;

    TcpConnexion(int port_recep, LinkedList<String> pile){
        this.port_recep= port_recep;
        this.pile= pile;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port_recep);
            while (true) {
                Socket socket = server.accept();

                ServiceMess serv= new ServiceMess(socket);
                Thread th= new Thread(serv);
                th.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    class ServiceMess implements Runnable{
        private final Socket socket;
        ServiceMess(Socket socket){
            this.socket= socket;
        }
        @Override
        public void run() {
            put();
        }
        private void put() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                String mess = br.readLine();
		/*
                try {
                    String[] splited = mess.split(" ");
                    if (splited[0].equals("MESS")) {
                        String id_client = splited[1];
                        String message_client = splited[2];
                        String msg = id_client + " " + message_client + "\r\n";
                        pile.push(msg);
                        pw.println("ACKM\r\n");
                        pw.flush();
                        socket.close();
                    }
                    if(splited[0].equals("LAST")){
                        int nb_Mess= Integer.parseInt(splited[1]);
                        Iterator<String> te= pile.descendingIterator();
                        int stop= (Math.min(nb_Mess, pile.size()));
                        for(int i= 0; i< stop; i++){
                            String msg= te.next();
                            pw.println(msg.replace("DIFF", "OLDM"));
*/
                    synchronized (pile) {
                        try {
                            String[] splited = mess.split(" ");
                            synchronized (MultiCast.numMes){
                                if (splited[0].equals("MESS")) {
                                    String id_client = splited[1];
                                    String message_client = mess.substring(14, 154);
                                    MultiCast.incNumMes();
                                    String msg = "DIFF "+ MultiCast.numMes+" " + id_client +" "+ message_client +"\r\n";
                                    pile.add(Integer.parseInt(MultiCast.numMes), msg);
                                    pw.print("ACKM\r\n");
                                    pw.flush();
                                    pw.close();
                                    socket.close();
                                }
                                if (splited[0].equals("LAST")) {
                                    int nb_Mess = Integer.parseInt(splited[1]);
                                    Iterator<String> te = pile.descendingIterator();
                                    int stop = (Math.min(nb_Mess, Integer.parseInt(MultiCast.numMes)));
                                    for (int i = 0; i < stop; i++) {
                                        String msg = te.next();
                                        String old= msg.replace("DIFF", "OLDM");
                                        pw.print(old);
                                        pw.flush();
                                    }
                                    pw.print("ENDM\r\n");
                                    pw.flush();
                                    pw.close();
                                    socket.close();
                                }
                            }
                        } catch (NullPointerException | IOException ignored) {
                            pw.close();
                            br.close();
                            socket.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

