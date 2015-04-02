package PeerProtocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Main.Peer;
import Message.Message;


public class PeerChunkBackup {
	
	private Message msg;
	private Timer timer;
	//private boolean reclaimed = true;
	
	public PeerChunkBackup(Message msg) throws InterruptedException{
		this.msg = msg;
		this.timer = new Timer();
		Random rand = new Random();
		/*
		if(msg.getBody().length > Peer.getAvailableSpace() && !Peer.reclaimInProgress){
			System.out.println("LIMIT REACHED. RECLAIMING SPACE...");
			PeerSpaceReclaiming psr = new PeerSpaceReclaiming();
			reclaimed = psr.reclaim();
		}
		
		while(Peer.reclaimInProgress) Thread.sleep(10);
		*/
		if(!(msg.getBody().length > Peer.getAvailableSpace())){
			long writtenSize = Peer.writeChunk(msg); 
			Peer.usedSpace+=writtenSize;
			if(writtenSize > 0){
				Peer.addChunk(msg);
				//Peer.updateActualRepDegree(msg, 1);
			}
			this.timer.schedule(new Task(), rand.nextInt(401));
		} else 
			System.err.println("NO SPACE AVAILABLE");
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
				System.out.println(InetAddress.getLocalHost() + " SENDING A STORED MESSAGE");
				Peer.mc_socket.send(storedPacket);
				timer.cancel();
				timer.purge();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
