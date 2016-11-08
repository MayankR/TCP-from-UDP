import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Created by ashleyjain on 06/11/16.
 */
public class sender {

    private static DatagramSocket clientSocket;

    public static void main(String args[]) throws Exception {

        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(1000);//1s timeout

        int Seq = 0;String sequ = "";String zero12 = "000000000000";
        int MSS = 1000;
        int flow = 100000;
        int packetSize = 1000;String PS = "";
        int W = MSS;
        int tmp;
        int ACK=0;
        byte[] sendData;
        byte[] receiveData = new byte[1024];

        String sentence;
        String by1000="";
        for(int i=0;i<1000;i++)
            by1000+="a";

        InetAddress ipAddress = InetAddress.getByName(args[0]);
        String port = args[1];

        while(flow>0){

            sequ = zero12.substring(0,12-(Seq+"").length())+Seq;
            //System.out.println("seq: "+sequ);

            PS = zero12.substring(0,12-(packetSize+"").length())+packetSize;
            sentence = sequ+PS+by1000.substring(0,1000);
            //System.out.println(sentence.substring(0,24)+"");

            sendData = sentence.getBytes();
            int sendSizeByte = sendData.length;

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendSizeByte, ipAddress, Integer.parseInt(port));
            clientSocket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                clientSocket.receive(receivePacket);
                String rec = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println(rec);
                ACK = Integer.parseInt(rec);
                Seq+= ACK;
                flow-=ACK;
                tmp = W;
                W+= (MSS*MSS)/tmp;
            } catch (SocketTimeoutException e) {
                // time expired
                W=MSS;
                continue;
            }

        }

        clientSocket.close();

    }
}
