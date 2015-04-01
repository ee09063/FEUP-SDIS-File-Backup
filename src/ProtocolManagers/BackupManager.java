package ProtocolManagers;

import Main.Peer;
import PeerProtocol.PeerChunkBackup;

public class BackupManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.putchunk_messages.isEmpty()){
				try {
					@SuppressWarnings("unused")
					PeerChunkBackup pcb = new PeerChunkBackup(Peer.putchunk_messages.removeFirst());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
		}
	}	
}