package ProtocolManagers;

import java.io.IOException;

import InitiatorProtocol.SpaceReclaiming;
import Main.Peer;

public class SpaceReclaimingManager implements Runnable{
	@Override
	public void run(){
		while(true){
			if(!Peer.removed_messages.isEmpty()){
				System.out.println("RECEIVED REMOVED MESSAGE...");
				try {
					@SuppressWarnings("unused")
					SpaceReclaiming sr = new SpaceReclaiming(Peer.removed_messages.removeFirst());
				} catch (IOException e) {
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