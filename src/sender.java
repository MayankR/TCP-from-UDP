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
        clientSocket.setSoTimeout(1);//1s timeout

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
		
		boolean loss = false;

		if(args.length>2)
		{
			if(args[1].equal("1"))
			{
				loss = true;
			}
			InetAddress ipAddress = InetAddress.getByName(args[2]);
			String port = args[3];
		}
		int w_send = 0;
	
		int last_ACK = 0;
	
        while(true){
			int w_send_copy = W;
			if(Seq < flow)
			{
				w_send += W;
				w_send = Math.min(w_send, flow - Seq);
				w_send_copy = w_send;
                System.out.println("W: " + W + "; Seq: " + Seq + "; Total senging: " + w_send);
				while(w_send > 0)
				{
					sequ = zero12.substring(0,12-(Seq+"").length())+Seq;
					
					packetSize = Math.min(MSS, w_send);
					
					PS = zero12.substring(0,12-(packetSize+"").length())+packetSize;
					sentence = sequ+PS+by1000.substring(0,1000);
				
					sendData = sentence.getBytes();
					int sendSizeByte = sendData.length;

                    System.out.println("Sending in while: " + Seq + " size: " + packetSize);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendSizeByte, ipAddress, Integer.parseInt(port));
					
					if(loss && Math.random()>0.05)
					{
						clientSocket.send(sendPacket);
					}
					
					w_send -= packetSize ;	//todo
					Seq += packetSize;		//todo
				}
			}
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			w_send = 0;

			
			clientSocket.setSoTimeout(1000);//1s timeout
			
			try {
				clientSocket.receive(receivePacket);
				String rec = new String(receivePacket.getData(), 0, receivePacket.getLength());
				System.out.println("Recvd ACK: " + rec);
				ACK = Integer.parseInt(rec);
				
				if(ACK == flow)
				{
					break;
				}
				
				
				if(ACK == last_ACK)
				{
					Seq = ACK;
					W = MSS;
				}
				else //if(ACK > Seq - W)
				{
					w_send = ACK - Seq;
					System.out.println(w_send + " " + ACK + " " + Seq + " " + w_send_copy);
					tmp = W;
					W+= (MSS*MSS)/tmp;
				}
				last_ACK = ACK;
				
			} catch (SocketTimeoutException e) {
				// time expired
				Seq = last_ACK;
				W = MSS;
				System.out.println("Catch out");
			}
			
			
			boolean break_outer = false;

			clientSocket.setSoTimeout(1);//1s timeout

			while(true)
			{
				try {
					clientSocket.receive(receivePacket);
					String rec = new String(receivePacket.getData(), 0, receivePacket.getLength());
					System.out.println("Recvd ACK _ in: " + rec);
					ACK = Integer.parseInt(rec);
					
					if(ACK == flow)
					{
						System.out.println("Break outer");
						break_outer = true;
						break;
					}
					
					
					if(ACK == last_ACK)
					{
						Seq = ACK;
						W = MSS;
					}
					else //if(ACK > Seq - W)
					{
						w_send = ACK - Seq;
						System.out.println(w_send + " " + ACK + " " + Seq + " " + w_send_copy + " fefwfewfwefwwfwef");
						tmp = W;
						W+= (MSS*MSS)/tmp;
					}
					last_ACK = ACK;
					
				} catch (SocketTimeoutException e) {
					// time expired
					
					System.out.println("Catch in");
					//Seq = last_ACK;
					//W = MSS;
					break;
				}
			}
			if(break_outer)
			{
				break;
			}
        }

        clientSocket.close();

    }
}
