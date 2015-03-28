package Listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Main.Chunk;
import Main.Peer;
import Message.Message;

public class ListenToMDB implements Runnable{
	@Override
	public void run() {
		try {
			Peer.mdb_socket.joinGroup(Peer.mdb_saddr.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			byte[] receiveData = new byte[64*1024];
			DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
			byte[] finalArray = null;
			try {
				Peer.mdb_socket.receive(rp);
				finalArray = new byte[rp.getLength()];
				System.arraycopy(rp.getData(), 0, finalArray, 0, rp.getLength());	
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message message = null;
			try {
				if(!rp.getAddress().equals(InetAddress.getLocalHost())){
					try {
						message = Message.fromByteArray(finalArray);
						if(message.type == Message.Type.PUTCHUNK){
							System.out.println("RECEIVED A PUTCHUNK MESSAGE");
							Peer.mutex_putchunk_messages.lock();
							Peer.putchunk_messages.add(message);
							Peer.mutex_putchunk_messages.unlock();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
}