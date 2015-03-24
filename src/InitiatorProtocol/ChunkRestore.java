package InitiatorProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Files.FileID;
import Main.Peer;
import Message.Message;

public class ChunkRestore{

	final DatagramPacket packet;
	private int timeInterval = 500;
	private int count = 0;
	
	public ChunkRestore(final FileID fileId, final int chunkNo) throws IOException{
		final Message msg = Message.makeGetChunk(fileId, chunkNo);
		packet = new DatagramPacket(msg.toByteArray(),
				  msg.toByteArray().length,
				  Peer.mc_saddr.getAddress(),
				  Peer.mc_saddr.getPort());
		
		SendDelay sd = new SendDelay();
		sd.startTask(packet, msg);
	}
	
	public class SendDelay{
		private Timer timer = new Timer();
		DatagramPacket p;
		Message message;
		
		public void startTask(DatagramPacket p, Message msg){
			this.p = p;
			this.message = msg;
			Random rand = new Random();
			timer.schedule(new PeriodicTask(), rand.nextInt(100));
		}
		
		private class PeriodicTask extends TimerTask{
			@Override
			public void run() {
				try {
					Peer.mc_socket.send(p);
					TaskManager task = new TaskManager();
					task.startTask(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	public class TaskManager {
	    private Timer timer = new Timer();
	    Message msg;
	    
	    public void startTask(Message msg) {
	    	this.msg = msg;
	        timer.schedule(new PeriodicTask(), timeInterval);
	    }

	    private class PeriodicTask extends TimerTask {
	        @Override
	        public void run(){
	        	count++;
	        	if(count == 3){
	        		System.out.println("GAME OVER MAN, GAME OVER! -> ChunkRestore");
	        		timer.cancel();
	        		timer.purge();
	        	}else{
	        		Peer.mutex_chunk_messages.lock();
	        		Message m = Peer.chunkMessageExists(msg);
	        		Peer.mutex_chunk_messages.unlock();
	        		if(m != null){
	        			Peer.writeChunk(m);
	        			timer.cancel();
	        			timer.purge();
	        		}else{
	        			try {
							Peer.mc_socket.send(packet);
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
