package PeerProtocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Random;

import Files.ChunkInfo;
import Files.FileID;
import Files.FileSystem;
import Main.Peer;
import Message.Message;

public class PeerSpaceReclaiming {
	public PeerSpaceReclaiming(){
		
	}
	
	public void reclaim(){
		Peer.reclaimInProgress = true;
		
		ArrayList<ChunkInfo> list = Peer.getChunksWithHighRD();
		
		if(list != null){
			System.out.println("DETECTED " + list.size() + " CHUKS WITH HIGH REPLICATION DEGREE");
			
			for(int i = 0; i < list.size(); i++){
				ChunkInfo chunk = list.get(i);
				File chunkToDelete = new File(Peer.getBackupDir() + File.separator + chunk.getFileId() + File.separator + chunk.getChunkNo());
				
				if(FileSystem.deleteFile(chunkToDelete, true))
					Peer.chunks.remove(chunk);
				
				Message rc = Message.makeRemoved(new FileID(chunk.getFileId()), chunk.getChunkNo());
				DatagramPacket packet = new DatagramPacket(rc.toByteArray(), rc.toByteArray().length, Peer.mc_saddr.getAddress(), Peer.mc_saddr.getPort());
				Random rand = new Random();
				
				try {
					Thread.sleep(rand.nextInt(101));
					Peer.mc_socket.send(packet);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else{
			System.err.println("THERE ARE NO CHUNKS WITH HIGH REPLICATION DEGREE");
		}
		Peer.reclaimInProgress = false;
	}
}
