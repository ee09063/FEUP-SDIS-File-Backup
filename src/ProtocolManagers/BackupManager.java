package ProtocolManagers;

import Main.Peer;
import Message.Message;
import PeerProtocol.PeerChunkBackup;

public class BackupManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.putchunk_messages.isEmpty()){
				Peer.mutex_putchunk_messages.lock();
				Message message = Peer.putchunk_messages.firstElement();
				Peer.putchunk_messages.removeElementAt(0);
				Peer.mutex_putchunk_messages.unlock();
				try {
					@SuppressWarnings("unused")
					PeerChunkBackup pcb = new PeerChunkBackup(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		}
	}	
}