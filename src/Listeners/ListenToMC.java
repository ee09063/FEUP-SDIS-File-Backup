package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;

import Main.Chunk;
import Main.Peer;
import Message.Message;
import Utilities.Triple;

public class ListenToMC implements Runnable{
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Peer.mc_socket.joinGroup(Peer.mc_saddr.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
			DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
			try {
				Peer.mc_socket.receive(rp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message message = null;
			try {
				message = Message.fromByteArray(rp.getData());
				Peer.mutex_stored_messages.lock();
				if(message.type == Message.Type.STORED){
					this.filterStoredMessage(rp, message);
				} else if(message.type == Message.Type.GETCHUNK){
					Peer.getchunk_messages.add(message);
				} else if(message.type == Message.Type.DELETE){
					Peer.delete_messages.add(message);
				} else if(message.type == Message.Type.REMOVED){
					Peer.removed_messages.add(message);
				}
				Peer.mutex_stored_messages.unlock();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void filterStoredMessage(DatagramPacket rp, Message message){
		/*
		 * IP, FILEID, CHUNKNO
		 */
		Triple<String, String, Integer> tr = new Triple<String, String, Integer>(rp.getAddress().toString(), message.getFileID().toString(), message.chunkNo);
		if(Peer.peers.contains(tr)){
			System.out.println("RECEIVED A DUPLICATE STORED MESSAGE FROM " + tr.getFirst() + " " + tr.getSecond() + " " + tr.getThird());
		} else {
			System.out.println("RECEIVED STORED MESSAGE " + tr.getFirst() + " " + tr.getSecond() + " " + tr.getThird());
			Peer.peers.addElement(tr);
			Peer.stored_messages.add(message);
			/*
			 * UPDATE ACTUAL REPLICATION DEGREE OF THE CHUNK
			 */
			Peer.updateActualRepDegree(message);
		}
	}
	
}