package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import Files.ChunkInfo;
import Main.Chunk;
import Main.Peer;
import Message.Message;
import Utilities.Pair;

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
				if(message.type == Message.Type.STORED){
					Peer.mutex_stored_messages.lock();
					this.filterStoredMessage(rp, message);
					Peer.mutex_stored_messages.unlock();
				} else if(message.type == Message.Type.GETCHUNK){
					Peer.getchunk_messages.add(message);
				} else if(message.type == Message.Type.DELETE){
					Peer.delete_messages.add(message);
				} else if(message.type == Message.Type.REMOVED){
					Peer.removed_messages.add(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void filterStoredMessage(DatagramPacket rp, Message message){
		Pair<String, ChunkInfo> peer = new Pair<String, ChunkInfo>(rp.getAddress().toString(), new ChunkInfo(message.getFileID().toString(), message.chunkNo, 0, 0));
		if(Peer.peers.contains(peer)){
			System.out.println("RECEIVED A DUPLICATE STORED MESSAGE FROM " + peer.getfirst() + " " + peer.getsecond().getFileId().toString() + " " + peer.getsecond().getChunkNo());
		} else {
			System.out.println("RECEIVED STORED MESSAGE " + peer.getfirst() + " " + peer.getsecond().getFileId().toString() + " " + peer.getsecond().getChunkNo());
			Peer.peers.addElement(peer);
			Peer.stored_messages.add(message);
			/*
			 * UPDATE ACTUAL REPLICATION DEGREE OF THE CHUNK
			 */
			Peer.updateActualRepDegree(message);
		}
	}
	
}