package InitiatorProtocol;

import java.io.IOException;

import Files.Chunk;
import Main.Peer;
import Message.Message;
import Utilities.Pair;

public class SpaceReclaiming {
	public SpaceReclaiming(Pair<String, Message> pair) throws IOException{
		Message message = pair.getsecond();
		String ip = pair.getfirst();
		int dif = Peer.updateActualRepDegree(message, -1);
		if(dif < 0){ //DRD > ARD
			try {
				byte[] chunkArray = Peer.getOriginalChunk(message.getFileID(), message.getChunkNo());
				int rd = Peer.getRDOfChunk(message);
				final Chunk chunk = new Chunk(message.getChunkNo(), rd, message.getFileID(), chunkArray);
				/*
				 * REMOVE PEER BECAUSE OF DUPLICATED MESSAGES
				 */
				Peer.removePeer(ip, message);
			
				@SuppressWarnings("unused")
				ChunkBackup cb = new ChunkBackup(chunk);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else System.out.println("CHUNK BACKUP NOT NECESSARY");
	}
}
