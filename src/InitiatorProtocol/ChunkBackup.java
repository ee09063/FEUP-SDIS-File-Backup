package InitiatorProtocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Files.Chunk;
import Main.Peer;
import Message.Message;


public class ChunkBackup {
	private int count = 0;
	private int timeInterval = 500;
	final Message msg;
	final DatagramPacket msgPacket;
	
	public ChunkBackup(Chunk chunk) throws IOException{
		msg = Message.makePutChunk(chunk);
	
		Peer.addChunk(msg);
		
		byte[] temp = msg.toByteArray();
		msgPacket = new DatagramPacket(temp,
										  temp.length,
										  Peer.mdb_saddr.getAddress(),
										  Peer.mdb_saddr.getPort());
		SendDelay sd = new SendDelay();
		sd.startTask(msgPacket, chunk, msg);
	}
	
	public class SendDelay{
		private Timer timer = new Timer();
		DatagramPacket p;
		Message message;
		Chunk chunk;
		
		public void startTask(DatagramPacket p, Chunk chunk, Message msg){
			this.p = p;
			this.chunk = chunk;
			this.message = msg;
			Random rand = new Random();
			timer.schedule(new PeriodicTask(), rand.nextInt(401));
		}
		
		private class PeriodicTask extends TimerTask{
			@Override
			public void run() {
				try {
					Peer.mdb_socket.send(p);
					TaskManager task = new TaskManager();
					task.startTask(message, chunk);
					timer.cancel();
					timer.purge();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	public class TaskManager {
	    private Timer timer = new Timer();
	    Message msg;
	    Chunk chunk;
	    
	    public void startTask(Message msg, Chunk chunk) {
	    	this.msg = msg;
	    	this.chunk = chunk;
	        timer.schedule(new PeriodicTask(), timeInterval);
	    }

	    private class PeriodicTask extends TimerTask {
	        @Override
	        public void run(){
	        	count++;
	        	if(count == 5){
	        		System.err.println("BACKUP ERROR : CHUNK " + chunk.chunkNo + " " + Peer.getARDOfChunk(msg) + " OUT OF " + chunk.replicationDeg);
	        		timer.cancel();
	        		timer.purge();
	        	}else{
	        		int numStored = Peer.getARDOfChunk(msg);
	        		if(numStored >= chunk.replicationDeg){
	        			timer.cancel();
		        		timer.purge();
	        		}else{
	        			try {
	        				System.out.println("MISSING CHUNK NO " + chunk.chunkNo);
							Peer.mdb_socket.send(msgPacket);
						} catch (IOException e) {
							e.printStackTrace();
						}
	        			timeInterval*=2;
	        			timer.schedule(new PeriodicTask(), timeInterval);
	        		}
	        	}
	        }
	    }
	}
}
