package ProtocolManagers;

import Main.Peer;
import Message.Message;
import PeerProtocol.PeerChunkRestore;

public class RestoreManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.getchunk_messages.isEmpty()){
				Message message = Peer.getchunk_messages.firstElement();
				Peer.getchunk_messages.removeElementAt(0);
				@SuppressWarnings("unused")
				PeerChunkRestore pcr = new PeerChunkRestore(message);
			}	
		}
	}	
}
