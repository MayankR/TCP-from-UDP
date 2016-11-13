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
        boolean[] gotData = new boolean[100005];
        byte[] sendData;
        String sendString;
        int rPacketSize = 0;
        int nextSeq = 0;
        int rSeq = 0;
        int ack = 0;

        for(int i=0;i<100005;i++) {
            gotData[i]=false;
        }

        int count = 0;
        while(true) {
            count++;
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String rec = new String(receivePacket.getData());
            // System.out.print(rec.substring(0, 24) + "  ");
            // System.out.print("count: "  + count + " ");

            System.out.println("Received data: " + rec);

            rSeq = Integer.parseInt(rec.substring(0,12));           //Packet seq number -> start bit
            rPacketSize = Integer.parseInt(rec.substring(12,24));       //Packet size

            for(int i=rSeq;i<rSeq+rPacketSize;i++) {
                gotData[i]=true;
            }

            if(rSeq==nextSeq){
                nextSeq += rPacketSize;
                while(gotData[nextSeq]) {
                    nextSeq++;
                }
                ack = nextSeq;
            }

            System.out.println("sending ACK: " + ack+"");

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            
            sendString = ack + "";
            sendData = sendString.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);

        }

    }

}
