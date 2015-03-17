import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;


public class ChunkBackup {
	private int count = 0;
	private int timeInterval = 500;
	final Message msg;
	final DatagramPacket msgPacket;
	
	public ChunkBackup(Chunk chunk) throws IOException{
		msg = Message.makePutChunk(chunk);
		
		
		msgPacket = new DatagramPacket(msg.toByteArray(),
										  msg.toByteArray().length,
										  Peer.mdb_saddr.getAddress(),
										  Peer.mdb_saddr.getPort());
		Peer.mdb_socket.send(msgPacket);
		System.out.println("SENT MESSAGE");
		/*TaskManager task = new TaskManager();
		task.startTask(msg, chunk);*/
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
