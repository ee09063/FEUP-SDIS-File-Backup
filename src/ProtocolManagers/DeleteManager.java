package ProtocolManagers;

import Main.Peer;
import Message.Message;
import PeerProtocol.PeerFileDeletion;

public class DeleteManager implements Runnable{
	@Override
	public void run() {
		while(true){
			if(!Peer.delete_messages.isEmpty()){
				System.out.println("RECEIVED DELETE REQUEST...");
				Message message = Peer.delete_messages.firstElement();
				Peer.delete_messages.removeElementAt(0);
				@SuppressWarnings("unused")
				PeerFileDeletion pfd = new PeerFileDeletion(message);
			}	
		}
	}	
}
