import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by ashleyjain on 06/11/16.
 * Contributors: Ashley Jain, Prakhar Gupta, Mayank Rajoria
 */
public class receiver {

    public static void main(String args[]) throws IOException {

        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData;
        String sendString;
        int rPacketSize = 0;
        int nextSeq = 0;
        int rSeq = 0;
        int ack = 0;

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String rec = new String(receivePacket.getData());
            System.out.println(rec);

            rSeq = Integer.parseInt(rec.substring(0,12));           //Packet seq number -> start bit

            if(rSeq==nextSeq){
                rPacketSize = Integer.parseInt(rec.substring(12,24));       //Packet size
                nextSeq += rPacketSize;
                ack = nextSeq;
            }

            System.out.println(ack+"");

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            sendString = ack + "";
            sendData = sendString.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);

        }

    }

}
