package InitiatorProtocol;

import Files.ChunkInfo;
import Main.Peer;
import Message.Message;

public class SpaceReclaiming {
	public SpaceReclaiming(Message message){
		Peer.updateActualRepDegree(message, -1);
		//ChunkInfo chunk = Peer.chunks.get(arg0)
	}
}
