package PeerProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Main.Chunk;
import Main.Peer;
import Message.Message;

public class PeerChunkRestore {
	
	private Message chunkMessage;
	private Timer timer;
	
	public PeerChunkRestore(Message msg){
		byte[] chunkArray = null;
		
		try {
			chunkArray = Peer.readChunk(msg.getFileID(), msg.getChunkNo());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(chunkArray == null) return;
		
		final Chunk chunk = new Chunk(msg.getChunkNo(), msg.getFileID(), chunkArray);
		this.chunkMessage = Message.makeChunk(chunk);
		this.timer = new Timer();
		Random rand = new Random();
		this.timer.schedule(new Task(), rand.nextInt(401));
		
	}
	
	private class Task extends TimerTask{
		@Override
		public void run(){
			DatagramPacket packet = new DatagramPacket(chunkMessage.toByteArray(),
												chunkMessage.toByteArray().length,
												Peer.mdr_saddr.getAddress(),
												Peer.mdr_saddr.getPort());
			try {
				Peer.mdr_socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
