package ProtocolManagers;

import Main.Peer;
import PeerProtocol.PeerFileDeletion;

public class DeleteManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.delete_messages.isEmpty()){
				System.out.println("RECEIVED DELETE REQUEST...");
				@SuppressWarnings("unused")
				PeerFileDeletion pfd = new PeerFileDeletion(Peer.delete_messages.removeFirst());
			} else
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}	
}
