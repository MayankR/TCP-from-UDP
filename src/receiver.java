import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ashleyjain on 06/11/16.
 */
public class receiver {

    public static void main(String args[]) throws IOException {

        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData;
        String send;
        int ACK = 0;
        int seq = 0;
        int rseq =0;

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String rec = new String(receivePacket.getData());
            System.out.println(rec);

            rseq = Integer.parseInt(rec.substring(0,12));

            if(rseq==seq){
                ACK = Integer.parseInt(rec.substring(12,24));
                seq+= ACK;
                System.out.println(ACK+"");
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                send = ACK+"";
                sendData = send.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }


        }

    }

}
