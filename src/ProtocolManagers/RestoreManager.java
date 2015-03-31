package ProtocolManagers;

import Main.Peer;
import PeerProtocol.PeerChunkRestore;

public class RestoreManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.getchunk_messages.isEmpty()){
				@SuppressWarnings("unused")
				PeerChunkRestore pcr = new PeerChunkRestore(Peer.getchunk_messages.removeFirst());
			} else
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}	
}
