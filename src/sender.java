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

		int w_send = 0;
	
		int last_ACK = 0;
	
        while(true){
			int w_send_copy = W;
			if(Seq <= flow)
			{
				w_send += W;
				w_send = Math.min(w_send, flow - Seq + 1);
				w_send_copy = w_send;
				while(w_send > 0)
				{
					sequ = zero12.substring(0,12-(Seq+"").length())+Seq;
					
					packetSize = Math.min(MSS, w_send);
					
					PS = zero12.substring(0,12-(packetSize+"").length())+packetSize;
					sentence = sequ+PS+by1000.substring(0,1000);
				
					sendData = sentence.getBytes();
					int sendSizeByte = sendData.length;

					DatagramPacket sendPacket = new DatagramPacket(sendData, sendSizeByte, ipAddress, Integer.parseInt(port));
					clientSocket.send(sendPacket);
					
					
					
					w_send -= packetSize ;	//todo
					Seq += packetSize;		//todo
				}
			}
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			w_send = 0;

            try {
                clientSocket.receive(receivePacket);
                String rec = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println(rec);
                ACK = Integer.parseInt(rec);
                
                if(ACK == flow)
					break;
                
                
				last_ACK = ACK;
                
                if(ACK <= Seq - w_send_copy)
                {
					Seq = ACK;
					W = MSS;
				}
				else //if(ACK > Seq - W)
                {
					w_send = ACK - Seq;
					tmp = W;
					W+= (MSS*MSS)/tmp;
				}
            } catch (SocketTimeoutException e) {
                // time expired
                Seq = last_ACK;
                
                W = MSS;
            }

        }

        clientSocket.close();

    }
}
