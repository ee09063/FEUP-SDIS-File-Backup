package Main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Files.ChunkInfo;
import Files.FileID;
import Files.FileSystem;
import Files.MyFile;
import InitiatorProtocol.FileBackup;
import InitiatorProtocol.FileDeletion;
import InitiatorProtocol.FileRestore;
import Listeners.ListenToMC;
import Listeners.ListenToMDB;
import Listeners.ListenToMDR;
import Message.Message;
import ProtocolManagers.BackupManager;
import ProtocolManagers.DeleteManager;
import ProtocolManagers.RestoreManager;
import ProtocolManagers.SpaceReclaimingManager;
import Utilities.Pair;


public class Peer {
	/*ARGUMENTS -> MC_IP, MC_PORT, MDB_IP, MDB_PORT, MBD_IP, MDB_PORT*/
	public static Vector<Message> stored_messages;
	public static Vector<Message> putchunk_messages;
	public static Vector<Message> getchunk_messages;
	public static Vector<Message> chunk_messages;
	public static Vector<Message> delete_messages;
	public static Vector<Message> removed_messages;
	/*
	 * STRING -> PATH ; PAIR -> <FILEID, NOFCHUNKS>
	 */
	public static ConcurrentHashMap<String, Pair> fileList;
	
	public static Vector<Pair<String, ChunkInfo>> peers;
	public static Vector<ChunkInfo> chunks;
	/*
	 * MUTEXES
	 */
	public static Lock mutex_space;
	public static Lock mutex_stored_messages;
	public static Lock mutex_chunk_messages;
	public static Lock mutex_chunks;
	public static Lock mutex_putchunk_messages;
	/*
	 * SPACE RECLAIMING
	 */
	public static long usedSpace;
	public static long totalSpace = 200000000;
	public static long availableSpace;
	public static boolean reclaimInProgress;
	/*
	 * THREADS
	 */
	private static Thread ltmcThread;
	private static Thread ltmdbThread;
	private static Thread ltmdrThread;
	private static Thread bumThread;
	private static Thread rmThread;
	private static Thread dmThread;
	private static Thread srmThread;
	
	public static void main(String args[]) throws IOException, InterruptedException{
		/*if(args.length == 6){
			setUpSockets(args);
		}*/
		setUpSocketsDefault();
		
		System.out.println(InetAddress.getLocalHost());
		/*
		 * USED SPACE STARTS AT 0, CHANGE LATER DUE TO DATABASE 
		 */
		usedSpace = 0;
		availableSpace = totalSpace;
		reclaimInProgress = false;
		
		mutex_stored_messages = new ReentrantLock(true);
		mutex_chunk_messages = new ReentrantLock(true);
		mutex_space = new ReentrantLock(true);
		mutex_chunks = new ReentrantLock(true);
		mutex_putchunk_messages = new ReentrantLock(true);
		
		stored_messages = new Vector<Message>();
		putchunk_messages = new Vector<Message>();
		getchunk_messages = new Vector<Message>();
		chunk_messages = new Vector<Message>();
		delete_messages = new Vector<Message>();
		removed_messages = new Vector<Message>();
		
		fileList = new ConcurrentHashMap<String, Pair>();
		
		peers = new Vector<Pair<String, ChunkInfo>>();
		chunks = new Vector<ChunkInfo>();
		
		ListenToMC ltmc = new ListenToMC();
		ltmcThread = new Thread(ltmc);
		ltmcThread.start();
		
		ListenToMDB ltmdb = new ListenToMDB();
		ltmdbThread = new Thread(ltmdb);
		ltmdbThread.start();
		
		ListenToMDR ltmdr = new ListenToMDR();
		ltmdrThread = new Thread(ltmdr);
		ltmdrThread.start();
		
		BackupManager bum = new BackupManager();
		bumThread = new Thread(bum);
		bumThread.start();
		
		RestoreManager rm = new RestoreManager();
		rmThread = new Thread(rm);
		rmThread.start();
		
		DeleteManager dm = new DeleteManager();
		dmThread = new Thread(dm);
		dmThread.start();
		
		SpaceReclaimingManager srm = new SpaceReclaimingManager();
		srmThread = new Thread(srm);
		srmThread.start();
		
		while(true){
			System.out.println("THREAD COUNT : " + java.lang.Thread.activeCount());
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			String command = inFromUser.readLine();
			String parts[] = command.split(" ");
			if(parts.length == 2 && parts[0].equals("BACKUP")){
				String filename = parts[1];
				MyFile file = new MyFile(filename);
				FileBackup fb = new FileBackup(file, 1);
			} else if(parts.length == 2 && parts[0].equals("RESTORE")){
				String filename = parts[1];
				FileRestore fr = new FileRestore(filename, "restoredFiles" + File.separator + filename);
			} else if(parts.length == 2 && parts[0].equals("DELETE")){
				String filename = parts[1];
				FileDeletion fd = new FileDeletion(filename);
				fd.deleteOwnFile();
				fd.sendDeleteRequest();
			} else if(command.equals("QUIT")){
				quit();
			} else if(command.equals("PEER")){
				break;
			} else {
				System.out.println("INVALID INPUT");
			}
		}
		/*
		 * 
		 */
		System.out.println("PEER MODE - JOINING GROUP...");
	}
	
	public static void writeChunk(Message msg){
		String path = null;
		if(msg.type == Message.Type.PUTCHUNK){
			path = backupPath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		} else {
			path = restorePath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		}
		long writtenSize = FileSystem.writeByteArray(path, msg.getBody());
		mutex_space.lock();
			usedSpace+=writtenSize;
		mutex_space.unlock();
	}
	
	public static long getAvailableSpace(){
		return totalSpace - usedSpace;
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
		for(int i = 0; i < chunk_messages.size(); i++){
			Message m = chunk_messages.elementAt(i);
			if(m.getFileID().toString().equals(msg.getFileID().toString()) && m.getChunkNo() == msg.getChunkNo()){
				chunk_messages.removeElementAt(i);
				return m;
			}
		}
		return null;
	}
	
	public static byte[] readChunk(FileID fileId, Integer chunkNo) throws IOException {
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
		long fileSize = file.length();
		if(file.delete()){
			usedSpace-=fileSize;
			System.out.println("DELETED " + path);
		}
		else System.out.println("FAILED TO DELETE FILE " + path);
	}
	
	public static void updateActualRepDegree(Message message, int value){
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.elementAt(i);
			
			String fileID = chunk.getFileId().toString();
			Integer chunkNo = chunk.getChunkNo();
			Integer actualRD = chunk.getActualRD();
			
			if(message.getFileID().toString().equals(fileID) && message.chunkNo == chunkNo){
				chunk.setActualRD(actualRD+value);
				System.out.println("UPDATED ARD OF " + fileID + " " + chunkNo + " ARD: " + chunk.getActualRD());
			}
		}
	}
	
	public static void addChunk(Message message){
		ChunkInfo newChunk = new ChunkInfo(message.getFileID().toString(), message.chunkNo, message.getReplicationDeg(), 0);	
		mutex_chunks.lock();
		chunks.addElement(newChunk);
		mutex_chunks.unlock();
	}
	
	public static ArrayList<ChunkInfo> getChunksWithHighRD(){
		ArrayList<ChunkInfo> list = new ArrayList<ChunkInfo>();
		if(chunks.size() == 0)
			return null;
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.elementAt(i);
			
			if(chunk.getExcessDegree() > 0){/*CANDIDATE FOR REMOVAL*/
				list.add(chunk);
			}
		}
		return list;
	}
	
	public static ArrayList<ChunkInfo> getChunksWithLowRD(){
		ArrayList<ChunkInfo> list = new ArrayList<ChunkInfo>();
		if(chunks.size() == 0)
			return null;
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.elementAt(i);
			
			if(chunk.getExcessDegree() < 0){/*CANDIDATE FOR NEW BACKUP*/
				list.add(chunk);
			}
		}
		return list;
	}
	
	private static void quit(){
		if(ltmcThread != null)
			ltmcThread.interrupt();
		if(ltmdbThread != null)
			ltmdbThread.interrupt();
		if(ltmdrThread != null)
			ltmdrThread.interrupt();
		if(bumThread != null)
			bumThread.interrupt();
		if(rmThread != null)
			rmThread.interrupt();
		if(dmThread != null)
			dmThread.interrupt();
		mc_socket.close();
		mdb_socket.close();	
		mdr_socket.close();
		System.exit(0);
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
		mc_saddr = new InetSocketAddress("239.255.0.1", 1234);
		mc_port = mc_saddr.getPort();
		mc_socket = new MulticastSocket(mc_saddr.getPort());
		mc_socket.setTimeToLive(1);
		/*MULTICAST DATA BACKUP CONTROL*/
		mdb_saddr = new InetSocketAddress("239.255.0.2", 5678);
		mdb_port = mdb_saddr.getPort();
		mdb_socket = new MulticastSocket(mdb_saddr.getPort());
		mdb_socket.setTimeToLive(1);
		/*MULTICAST DATA RESTORE CONTROL*/
		mdr_saddr = new InetSocketAddress("239.255.0.3", 9123);
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
