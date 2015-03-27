package PeerProtocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import InitiatorProtocol.SpaceReclaiming;
import Main.Peer;
import Message.Message;
import Message.Message.Type;


public class PeerChunkBackup {
	
	private Message msg;
	private Timer timer;
	
	public PeerChunkBackup(Message msg){
		this.msg = msg;
		this.timer = new Timer();
		Random rand = new Random();
		/*
		 * 
		 */
		if(msg.getBody().length > Peer.availableSpace){/*NEED TO RECLAIM SPACE*/
			System.out.println("LIMIT REACHED. RECLAIMING SPACE...");
			System.exit(0);
			PeerSpaceReclaiming psr = new PeerSpaceReclaiming();
		}
		Peer.writeChunk(msg);
		/*
		 * 
		 */
		this.timer.schedule(new Task(), rand.nextInt(401));
	}
	
	private class Task extends TimerTask{
		@Override
		public void run(){
			Message storedMsg = new Message(Message.Type.STORED);
			storedMsg.setFileID(msg.getFileID());
			storedMsg.setVersion(1, 0);
			storedMsg.setChunkNo(msg.getChunkNo());
			DatagramPacket storedPacket = new DatagramPacket(storedMsg.toByteArray(),
												storedMsg.toByteArray().length,
												Peer.mc_saddr.getAddress(),
												Peer.mc_saddr.getPort());
			try {
				Peer.mc_socket.send(storedPacket);
				timer.cancel();
				timer.purge();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
