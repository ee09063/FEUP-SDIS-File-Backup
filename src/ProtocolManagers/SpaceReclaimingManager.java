package ProtocolManagers;

import InitiatorProtocol.SpaceReclaiming;
import Main.Peer;
import Message.Message;

public class SpaceReclaimingManager implements Runnable{
	@Override
	public void run(){
		while(true){
			if(!Peer.removed_messages.isEmpty()){
				System.out.println("RECEIVED REMOVED MESSAGE...");
				Message message = Peer.removed_messages.firstElement();
				Peer.removed_messages.removeElementAt(0);
				@SuppressWarnings("unused")
				SpaceReclaiming sr = new SpaceReclaiming(message);
			}
		}
	}
}