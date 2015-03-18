import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class ChunkBackup {
	private int count = 0;
	private int timeInterval = 500;
	final Message msg;
	final DatagramPacket msgPacket;
	
	public ChunkBackup(Chunk chunk) throws IOException{
		msg = Message.makePutChunk(chunk);
		
		byte[] temp = msg.toByteArray();
		
		msgPacket = new DatagramPacket(temp,
										  temp.length,
										  Peer.mdb_saddr.getAddress(),
										  Peer.mdb_saddr.getPort());
		
		//System.out.println("SENT MESSAGE");
		/*TaskManager task = new TaskManager();
		task.startTask(msg, chunk);*/
		
		SendDelay sd = new SendDelay();
		sd.startTask(msgPacket);
	}
	
	public class SendDelay{
		private Timer timer = new Timer();
		DatagramPacket p;
		
		public void startTask(DatagramPacket p){
			this.p = p;
			Random rand = new Random();
			timer.schedule(new PeriodicTask(), rand.nextInt(2000));
		}
		
		private class PeriodicTask extends TimerTask{
			@Override
			public void run() {
				try {
					System.out.println("SENDING MESSAGE");
					Peer.mdb_socket.send(p);
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
	        		/**/
	        	}else{
	        		int numStored = 0; //GET THE STORED MESSAGES HERE
	        		if(numStored >= chunk.replicationDeg){
	        			
	        		}else{
	        			timeInterval*=2;
	        			timer.schedule(new PeriodicTask(), timeInterval);
	        		}
	        	}
	        }
	    }
	}
}
