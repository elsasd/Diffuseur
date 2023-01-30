import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList;

public class MultiCast implements Runnable {
    public static String numMes = "0000";
    private final int port_diff;
    private final String adress_diff;
    private final LinkedList<String> pile;


    /*pile est une variable partagée entre deux threads: celui qui s'occupe de diffuser les messages de la pile,
      et celui qui s'occupe de la communication avec le client et qui transmet les messages du client dans la pile.
      --> Donc l'un lit à partir de la pile et l'autre y écrit.
     */

    MultiCast(int port_diff, String adress_diff, LinkedList<String> pile) {
        this.port_diff = port_diff;
        this.adress_diff = adress_diff;
        this.pile = pile;
    }


    public static synchronized void incNumMes() {
        int intOfStr = Integer.parseInt(numMes) + 1;
        if (intOfStr > 9999) intOfStr = 0;
        StringBuilder ret = new StringBuilder(Integer.toString(intOfStr));
        int nbZero = 4 - ret.length();
        for (int j = 0; j < nbZero; j++)
            ret.insert(0, "0");
        numMes = ret.toString();
    }

    @Override
    public void run() {
        take();
    }

    private void take() {
        try {
            DatagramSocket dso = new DatagramSocket();
            InetSocketAddress ia = new InetSocketAddress(adress_diff, port_diff);
            while (!pile.isEmpty()) {
                byte[] data;
                Thread.sleep(1000);
                synchronized (pile) {
                    synchronized (MultiCast.numMes) {
                        String msg = pile.get(Integer.parseInt(MultiCast.numMes));
                        String msg2 = "DIFF " + numMes + " " + msg.substring(10);
                        pile.add(msg2);
                        incNumMes();
                        data = msg2.getBytes();
                    }
                }
                DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
                dso.send(paquet);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
