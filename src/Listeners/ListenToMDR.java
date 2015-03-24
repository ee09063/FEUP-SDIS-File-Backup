package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;

import Main.Chunk;
import Main.Peer;
import Message.Message;

public class ListenToMDR implements Runnable{
	@Override
	public void run() {
		try {
			Peer.mdr_socket.joinGroup(Peer.mdr_saddr.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
			DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
			try {
				Peer.mdr_socket.receive(rp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message message = null;
			try {
				message = Message.fromByteArray(rp.getData());
				Peer.mutex_chunk_messages.lock();
				if(message.type == Message.Type.CHUNK){
					if(!Peer.chunk_messages.contains(message))
						Peer.chunk_messages.add(message);
				}
				Peer.mutex_chunk_messages.unlock();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}