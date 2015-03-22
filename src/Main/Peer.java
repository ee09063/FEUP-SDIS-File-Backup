package Main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Files.FileID;
import Files.FileSystem;
import Files.MyFile;
import InitiatorProtocol.FileBackup;
import InitiatorProtocol.FileDeletion;
import InitiatorProtocol.FileRestore;
import Message.Message;
import PeerProtocol.PeerChunkBackup;
import PeerProtocol.PeerChunkRestore;
import PeerProtocol.PeerFileDeletion;
import Utilities.Pair;


public class Peer {
	/*ARGUMENTS -> MC_IP, MC_PORT, MDB_IP, MDB_PORT, MBD_IP, MDB_PORT*/
	
	static Vector<Message> stored_messages;
	static Vector<Message> putchunk_messages;
	static Vector<Message> getchunk_messages;
	static Vector<Message> chunk_messages;
	static Vector<Message> delete_messages;
	public static Lock mutex_stored_messages;
	public static Lock mutex_chunk_messages;
	public static ConcurrentHashMap<String, Pair> fileList;
	
	public static void main(String args[]) throws IOException{
		/*if(args.length == 6){
			setUpSockets(args);
		}*/
		setUpSocketsDefault();
		
		mutex_stored_messages = new ReentrantLock(true);
		mutex_chunk_messages = new ReentrantLock(true);
		stored_messages = new Vector<Message>();
		putchunk_messages = new Vector<Message>();
		getchunk_messages = new Vector<Message>();
		chunk_messages = new Vector<Message>();
		delete_messages = new Vector<Message>();
		
		fileList = new ConcurrentHashMap<String, Pair>();
		
		ListenToMC ltmc = new ListenToMC();
		Thread ltmcThread = new Thread(ltmc);
		ltmcThread.start();
		
		ListenToMDB ltmdb = new ListenToMDB();
		Thread ltmdbThread = new Thread(ltmdb);
	
		ListenToMDR ltmdr = new ListenToMDR();
		Thread ltmdrThread = new Thread(ltmdr);
		ltmdrThread.start();
		
		BackUpManager bum = new BackUpManager();
		Thread bumThread = new Thread(bum);
		
		RestoreManager rm = new RestoreManager();
		Thread rmThread = new Thread(rm);
		rmThread.start();
		
		DeleteManager dm = new DeleteManager();
		Thread dmThread = new Thread(dm);
		
		while(true){
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			String command = inFromUser.readLine();
			String parts[] = command.split(" ");
			if(parts.length == 2 && parts[0].equals("BACKUP")){
				String filename = parts[1];
				MyFile test = new MyFile(filename);
				FileBackup fb = new FileBackup(test, 1);
				fb.Send();
			} else if(parts.length == 2 && parts[0].equals("RESTORE")){
				String filename = parts[1];
				FileRestore fr = new FileRestore(filename, "restoredFiles" + File.separator + filename);
			} else if(parts.length == 2 && parts[0].equals("DELETE")){
				String filename = parts[1];
				FileDeletion fd = new FileDeletion(filename);
			}
			else if(command.equals("PEER")){
				break;
			} else {
				System.out.println("INVALID INPUT");
			}
		}
		/*
		 * 
		 */
		System.out.println("ACTING AS PEER - JOINING GROUP...");
		ltmdbThread.start();
		bumThread.start();
		dmThread.start();
	}
	
	public static void writeChunk(Message msg){
		String path = null;
		if(msg.type == Message.Type.PUTCHUNK){
			path = backupPath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		} else {
			path = restorePath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
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
					@SuppressWarnings("unused")
					PeerChunkBackup pcb = new PeerChunkBackup(message);
				}	
			}
		}	
	}
	
	public static class RestoreManager implements Runnable{
		@Override
		public void run() {
			while(true){
				if(!getchunk_messages.isEmpty()){
					Message message = getchunk_messages.firstElement();
					getchunk_messages.removeElementAt(0);
					@SuppressWarnings("unused")
					PeerChunkRestore pcr = new PeerChunkRestore(message);
				}	
			}
		}	
	}
	
	public static class DeleteManager implements Runnable{
		@Override
		public void run() {
			while(true){
				if(!delete_messages.isEmpty()){
					System.out.println("RECEIVED DELETE REQUEST...");
					Message message = delete_messages.firstElement();
					delete_messages.removeElementAt(0);
					@SuppressWarnings("unused")
					PeerFileDeletion pfd = new PeerFileDeletion(message);
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
					mutex_stored_messages.lock();
					if(message.type == Message.Type.STORED){
						stored_messages.add(message);
					} else if(message.type == Message.Type.GETCHUNK){
						getchunk_messages.add(message);
					}
					else if(message.type == Message.Type.DELETE){
						delete_messages.add(message);
					}
					mutex_stored_messages.unlock();
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
	
	public static class ListenToMDR implements Runnable{
		@Override
		public void run() {
			try {
				mdr_socket.joinGroup(mdr_saddr.getAddress());
			} catch (IOException e) {
				e.printStackTrace();
			}
			while(true){
				byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
				DatagramPacket rp = new DatagramPacket(receiveData, receiveData.length);
				try {
					mdr_socket.receive(rp);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message message = null;
				try {
					message = Message.fromByteArray(rp.getData());
					if(message.type == Message.Type.CHUNK){
						if(!chunk_messages.contains(message))
							chunk_messages.add(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static int getStoredMessages(Chunk chunk){
		int count = 0;
		mutex_stored_messages.lock();
		for(Message m : stored_messages){
			if(m.getFileID().toString().equals(chunk.fileID._hexFileID)
					&& m.chunkNo == chunk.chunkNo){
				count++;
			}
		}
		mutex_stored_messages.unlock();
		return count;
	}
	
	public static void removeStoredMessages(Chunk chunk){
		for(int i = stored_messages.size()-1; i >=0; i--){
			Message m = stored_messages.elementAt(i);
			if(m.getFileID().toString().equals(chunk.fileID._hexFileID)
					&& m.chunkNo == chunk.chunkNo){
				stored_messages.remove(m);
			}
		}
	}
	
	public static Message chunkMessageExists(Message msg){
		for(Message m : chunk_messages){
			if(m.getFileID().toString().equals(msg.getFileID().toString()) && m.getChunkNo() == msg.getChunkNo()){
				return m;
			}
		}
		return null;
	}
	
	public static boolean allChunks(FileID fileId, int nofChunks){
		int count = 0;
		for(Message m : chunk_messages){
			if(m.getFileID().toString().equals(fileId.toString())){
				count++;
			}
		}
		if(count == nofChunks){
			return true;
		}
		System.out.println(count);
		return false;
	}
	
	public static byte[] readChunk(FileID fileId, Integer chunkNo) throws IOException {
		//System.out.println(getBackupDir() + File.separator + fileId.toString() + File.separator + chunkNo.toString());
        File f = new File(getBackupDir() + File.separator + fileId.toString() + File.separator + chunkNo.toString());
        if (!f.exists()) 
            throw new FileNotFoundException();
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        byte[] chunk = new byte[(int)f.length()];
        
        bis.read(chunk);        
        
        bis.close();
        return chunk;
    }
	
	public static void removeOwnFile(String path){
		File file = new File(path);
		if(file.delete())
			System.out.println("DELETED " + path);
		else System.out.println("FAILED TO DELETE FILE " + path);
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
		/*MULTICAST DATA RESTORE CONTROL*/
		mdr_saddr = new InetSocketAddress("239.0.0.5", 5555);
		mdr_port = mdr_saddr.getPort();
		mdr_socket = new MulticastSocket(mdr_saddr.getPort());
		mdr_socket.setTimeToLive(1);
	}
	
	public static String getBackupDir(){return backupPath;}
	public static String getRestoreDir(){return restorePath;}
	
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
	private final static String restorePath = "restore";
	
}
