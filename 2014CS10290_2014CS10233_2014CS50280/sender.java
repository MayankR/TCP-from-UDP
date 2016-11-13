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

	long start_time = System.currentTimeMillis();

        int Seq = 0;String sequ = "";String zero12 = "000000000000";
        int MSS = 1000;
        int flow = 100000;
        int packetSize = 1000;String PS = "";
        int W = MSS;
        int tmp;
        int ACK=0;
        byte[] sendData;
        byte[] receiveData = new byte[1024];

	long time_out = 3000;

        String sentence;
        String by1000="";
        for(int i=0;i<1000;i++)
            by1000+="a";

		InetAddress ipAddress;
		String port = "9876";
		
		boolean loss = false;

		if(args.length>1)
		{
			if(args[1].equals("1"))
			{
				loss = true;
			}
			ipAddress = InetAddress.getByName(args[2]);
			//port = args[3];
		}
		else
		{
			ipAddress = InetAddress.getByName(args[0]);
			//port = args[1];
		}
		int w_send = 0;
	
		int last_ACK = 0;
	long first_start_time = -1;
        while(true){
			int w_send_copy = W;
			if(Seq < flow)
			{
				w_send += W;
				w_send = Math.min(w_send, flow - Seq);
				w_send_copy = w_send;
                //System.out.println("W: " + W + "; Seq: " + Seq + "; Total senging: " + w_send);
				while(w_send/MSS != 0)
				{
					sequ = zero12.substring(0,12-(Seq+"").length())+Seq;
					
					packetSize = Math.min(MSS, w_send);
					
					PS = zero12.substring(0,12-(packetSize+"").length())+packetSize;
					sentence = sequ+PS+by1000.substring(0,1000);
				
					sendData = sentence.getBytes();
					int sendSizeByte = sendData.length;

					System.out.println("W: " + W + "\t Time Expired:" + (System.currentTimeMillis() - start_time) + "ms\t Sequence Number: "+Seq);

                    //System.out.println("Sending in while: " + Seq + " size: " + packetSize);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendSizeByte, ipAddress, Integer.parseInt(port));
					if(Seq == 0)
					{
						first_start_time = System.currentTimeMillis();
					}
					if(!loss || Math.random()>0.05)
					{
						clientSocket.send(sendPacket);
					}
					
					w_send -= packetSize ;	
					Seq += packetSize;	
				}
			}
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			w_send = 0;

			
			clientSocket.setSoTimeout((int)time_out);//1s timeout
			
			try {
				clientSocket.receive(receivePacket);
				String rec = new String(receivePacket.getData(), 0, receivePacket.getLength());
				//System.out.println("Recvd ACK: " + rec);
				if(first_start_time != 0)
				{
					time_out = System.currentTimeMillis() - first_start_time;
					//System.out.println(time_out);
					time_out = Math.max(3*time_out, 100);
					first_start_time = 0;
				}
					
				ACK = Integer.parseInt(rec);
				
				System.out.println("W: " + W + "\t Time Expired:" + (System.currentTimeMillis() - start_time) + "ms\t Cumulative ACK No.: "+ACK);
				
				if(ACK == flow)
				{
					break;
				}
				
				
				if(ACK == last_ACK)
				{
					Seq = ACK;
					W = MSS;
					w_send = 0;
				}
				else //if(ACK > Seq - W)
				{
					Seq = Math.max(Seq, ACK);
					w_send = ACK - Seq;
					//System.out.println(w_send + " " + ACK + " " + Seq + " " + w_send_copy);
					tmp = W;
					W+= (MSS*MSS)/tmp;
				}
				last_ACK = ACK;
				
			} catch (SocketTimeoutException e) {
				// time expired
				Seq = last_ACK;
				W = MSS;
				w_send = 0;
				//System.out.println("Catch out");
			}
			
			
			boolean break_outer = false;

			clientSocket.setSoTimeout(1);//1s timeout

			while(true)
			{
				try {
					clientSocket.receive(receivePacket);
					String rec = new String(receivePacket.getData(), 0, receivePacket.getLength());
					//System.out.println("Recvd ACK _ in: " + rec);
					
					ACK = Integer.parseInt(rec);
					
					System.out.println("W: " + W + "\t Time Expired:" + (System.currentTimeMillis() - start_time) + "ms\t Cumulative ACK No.: "+ACK);
					
					if(ACK == flow)
					{
						//System.out.println("Break outer");
						break_outer = true;
						break;
					}
					
					
					if(ACK == last_ACK)
					{
						Seq = ACK;
						W = MSS;
						w_send = 0;
					}
					else //if(ACK > Seq - W)
					{
						Seq = Math.max(Seq, ACK);
						w_send = ACK - Seq;
						//System.out.println(w_send + " " + ACK + " " + Seq + " " + w_send_copy + " fefwfewfwefwwfwef");
						tmp = W;
						W+= (MSS*MSS)/tmp;
					}
					last_ACK = ACK;
					
				} catch (SocketTimeoutException e) {
					// time expired
					
					//System.out.println("Catch in");
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
	//System.out.println("  FRFRFE " + time_out);
    }
}
