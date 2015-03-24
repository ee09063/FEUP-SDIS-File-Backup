package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import Main.Chunk;
import Main.Peer;
import Message.Message;

public class ListenToMDB implements Runnable{
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			Peer.mdb_socket.joinGroup(Peer.mdb_saddr.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
			DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
			try {
				Peer.mdb_socket.receive(rp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message message = null;
			try {
				message = Message.fromByteArray(rp.getData());
				if(message.type == Message.Type.PUTCHUNK){
					System.out.println("RECEIVED PUTCHUNK REQUEST...");
					Peer.mutex_putchunk_messages.lock();
					Peer.putchunk_messages.add(message);
					Peer.mutex_putchunk_messages.unlock();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}