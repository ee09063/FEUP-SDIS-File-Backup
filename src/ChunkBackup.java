import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ChunkBackup {
	private int count = 0;
	private int time = 500;
	final Message msg;
	//final DatagramPacket msgPacket;
	
	public ChunkBackup(Chunk chunk) throws IOException{
		msg = Message.makePutChunk(chunk);
		
		//System.out.println(this.msg.type + " " + new String(this.msg.getBody()));
		System.out.println(new String(this.msg.toByteArray()));
		
		
		/*SINCRO?*/
		/*msgPacket = new DatagramPacket(msg.toByteArray(),
										  msg.toByteArray().length,
										  Peer.mc_saddr.getAddress(),
										  Peer.mc_saddr.getPort());
		Peer.mc_socket.send(msgPacket);*/
		
		/*SCHEDULE TO VERIFY THE STORED MESSAGES*/
		
		
	}
	
	/*public void checkStoredMsg(Chunk chunk){
		ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
		exec.schedule(new Runnable(){
			public void run(){
				int numStoredMsg = 0; //HOW TO GET THEM?
				count++;
				if(numStoredMsg < chunk.replicationDeg){
					if(count == 5)
						System.out.println("ERROR: Replication degree not reached");
					else {
						time = time * 2;
						try {
							Peer.mc_socket.send(msgPacket);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, time, TimeUnit.MILLISECONDS);
	}
	*/
}
