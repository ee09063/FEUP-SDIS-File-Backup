package InitiatorProtocol;

import java.io.IOException;

import Main.Chunk;
import Main.Peer;
import Message.Message;

public class SpaceReclaiming {
	public SpaceReclaiming(Message message) throws IOException{
		int dif = Peer.updateActualRepDegree(message, -1);
		if(dif < 0){ //DRD > ARD
			try {
				byte[] chunkArray = Peer.getOriginalChunk(message.getFileID(), message.getChunkNo());
				int rd = Peer.getRDOfChunk(message);
				final Chunk chunk = new Chunk(message.getChunkNo(), rd, message.getFileID(), chunkArray);
				@SuppressWarnings("unused")
				ChunkBackup cb = new ChunkBackup(chunk);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else System.out.println("CHUNK BACKUP NOT NECESSARY");
	}
}
