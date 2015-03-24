package ProtocolManagers;

import Main.Peer;
import Message.Message;
import PeerProtocol.PeerChunkBackup;

public class BackupManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.putchunk_messages.isEmpty()){
				Message message = Peer.putchunk_messages.firstElement();
				Peer.putchunk_messages.removeElementAt(0);
				@SuppressWarnings("unused")
				PeerChunkBackup pcb = new PeerChunkBackup(message);
			}	
		}
	}	
}