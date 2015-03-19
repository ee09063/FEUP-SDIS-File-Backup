import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Peer {
	/*ARGUMENTS -> MC_IP, MC_PORT, MDB_IP, MDB_PORT, MBD_IP, MDB_PORT*/
	
	static Vector<Message> stored_messages;
	static Vector<Message> putchunk_messages;
	public static Lock mutex;
	
	public static void main(String args[]) throws IOException{
		/*if(args.length == 6){
			setUpSockets(args);
		}*/
		setUpSocketsDefault();
		mutex = new ReentrantLock(true);
		stored_messages = new Vector<Message>();
		putchunk_messages = new Vector<Message>();
		
		ListenToMC ltmc = new ListenToMC();
		Thread ltmcThread = new Thread(ltmc);
		ltmcThread.start();
		
		ListenToMDB ltmdb = new ListenToMDB();
		Thread ltmdbThread = new Thread(ltmdb);
	
		BackUpManager bum = new BackUpManager();
		Thread bumThread = new Thread(bum);
		
		
		while(true){
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			String command = inFromUser.readLine();
			String parts[] = command.split(" ");
			if(parts.length == 2 && parts[0].equals("BACKUP")){
				String filename = parts[1];
				MyFile test = new MyFile(filename);
				FileBackup fb = new FileBackup(test, 1);
				fb.Send();
			} else if(command.equals("PEER")){
				break;
			} else{
				System.out.println("INVALID INPUT");
			}
		}
		/*
		 * 
		 */
		System.out.println("ACTING AS PEER - JOINING GROUP...");
		ltmdbThread.start();
		bumThread.start();
	}
	
	public static void writeChunk(Message msg){
		//System.out.println("WRITING CHUNK " + msg.chunkNo.toString());
		String path = null;
		if(msg.type == Message.Type.PUTCHUNK){
			path = backupPath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		}
		long writtenSize = FileSystem.writeByteArray(path, msg.getBody());
	}
	
	
	public static class BackUpManager implements Runnable{
		@Override
		public void run() {
			while(true){
				if(!putchunk_messages.isEmpty()){
					Message message = putchunk_messages.firstElement();
					putchunk_messages.removeElementAt(0);
					PeerChunkBackup pcb = new PeerChunkBackup(message);
				}	
			}
		}	
	}
	
	public static class ListenToMC implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mc_socket.joinGroup(mc_saddr.getAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
			while(true){
				byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
				DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
				try {
					mc_socket.receive(rp);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message message = null;
				try {
					message = Message.fromByteArray(rp.getData());
					mutex.lock();
					System.out.println("ADDLOCKED");
					System.out.println("ADDING");
					if(message.type == Message.Type.STORED){
						stored_messages.add(message);
					}
					mutex.unlock();
					System.out.println("ADDUNLOCKED");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class ListenToMDB implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mdb_socket.joinGroup(mdb_saddr.getAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
			while(true){
				byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
				DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
				try {
					mdb_socket.receive(rp);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message message = null;
				try {
					message = Message.fromByteArray(rp.getData());
					if(message.type == Message.Type.PUTCHUNK){
						putchunk_messages.add(message);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static int getStoredMessages(Chunk chunk){
		int count = 0;
		mutex.lock();
		for(Message m : stored_messages){
			if(m.getFileID().toString().equals(chunk.fileID._hexFileID)
					&& m.chunkNo == chunk.chunkNo){
				count++;
			}
		}
		mutex.unlock();
		return count;
	}
	
	public static void removeStoredMessages(Chunk chunk){
		System.out.println("REMOVING");
		for(int i = stored_messages.size()-1; i >=0; i--){
			Message m = stored_messages.elementAt(i);
			if(m.getFileID().toString().equals(chunk.fileID._hexFileID)
					&& m.chunkNo == chunk.chunkNo){
				stored_messages.remove(m);
			}
		}
	}
	
	static void setUpSockets(String args[]) throws IOException{
		/*MULTICAST CONTROL SETUP*/
		mc_saddr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		mc_port = mc_saddr.getPort();
		mc_socket = new MulticastSocket(mc_saddr.getPort());
		mc_socket.setTimeToLive(1);
		/*MULTICAST DATA BACKUP CONTROL*/
		mdb_saddr = new InetSocketAddress(args[2], Integer.parseInt(args[3]));
		mdb_port = mdb_saddr.getPort();
		mdb_socket = new MulticastSocket(mdb_saddr.getPort());
		mdb_socket.setTimeToLive(1);
		/*MULTICAST DATA RESTORE CONTROL*/
		mdr_saddr = new InetSocketAddress(args[4], Integer.parseInt(args[5]));
		mdr_port = mdr_saddr.getPort();
		mdr_socket = new MulticastSocket(mdr_saddr.getPort());
		mdr_socket.setTimeToLive(1);	
	}
	
	static void setUpSocketsDefault() throws IOException{
		/*MULTICAST CONTROL SETUP*/
		mc_saddr = new InetSocketAddress("239.0.0.4", 4444);
		mc_port = mc_saddr.getPort();
		mc_socket = new MulticastSocket(mc_saddr.getPort());
		mc_socket.setTimeToLive(1);
		/*MULTICAST DATA BACKUP CONTROL*/
		mdb_saddr = new InetSocketAddress("239.0.0.3", 3333);
		mdb_port = mdb_saddr.getPort();
		mdb_socket = new MulticastSocket(mdb_saddr.getPort());
		mdb_socket.setTimeToLive(1);
	}
	
	/*MULTICAST CONTROL SOCKET*/
	public static MulticastSocket mc_socket;
	public static int mc_port;
	public static String mc_addr;
	public static InetSocketAddress mc_saddr;
	/*MULTICAST DATA BACKUP SOCKET*/
	public static MulticastSocket mdb_socket;
	public static int mdb_port;
	public static String mdb_addr;
	public static InetSocketAddress mdb_saddr;
	/*MULTICAST DATA RESTORE SOCKET*/
	public static MulticastSocket mdr_socket;
	public static int mdr_port;
	public static String mdr_addr;
	public static InetSocketAddress mdr_saddr;
	
	private final static String backupPath = "backup";
	
}
