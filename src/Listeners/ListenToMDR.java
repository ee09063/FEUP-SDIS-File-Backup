package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
			byte[] receiveData = new byte[64*1024];
			DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
			byte[] finalArray = null;
			try {
				Peer.mdr_socket.receive(rp);
				finalArray = new byte[rp.getLength()];
				System.arraycopy(rp.getData(), 0, finalArray, 0, rp.getLength());	
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message message = null;
			try {
				if(!rp.getAddress().equals(InetAddress.getLocalHost())){
					message = Message.fromByteArray(finalArray);
					if(message.type == Message.Type.CHUNK){
						Peer.mutex_chunk_messages.lock();
						//if(!Peer.chunk_messages.contains(message)){
						Peer.chunk_messages.add(message);
						Peer.mutex_chunk_messages.unlock();
					}	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}