package Main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Database.Database;
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
import PeerProtocol.PeerSpaceReclaiming;
import ProtocolManagers.BackupManager;
import ProtocolManagers.DeleteManager;
import ProtocolManagers.RestoreManager;
import ProtocolManagers.SpaceReclaimingManager;
import Utilities.LoadProperties;
import Utilities.Pair;


public class Peer {
	/*ARGUMENTS -> MC_IP, MC_PORT, MDB_IP, MDB_PORT, MBD_IP, MDB_PORT*/
	public static LinkedList<Message> putchunk_messages;
	public static LinkedList<Message> getchunk_messages;
	public static LinkedList<Message> chunk_messages;
	public static LinkedList<Message> delete_messages;
	public static LinkedList<Pair<String, Message>> removed_messages;
	/*
	 * STRING -> PATH ; PAIR -> <FILEID, NOFCHUNKS>
	 */
	public static ConcurrentHashMap<String, Pair<FileID, Integer>> fileList;
	public static LinkedList<Pair<String, ChunkInfo>> peers;
	public static LinkedList<ChunkInfo> chunks;
	/*
	 * MUTEXES
	 */
	public static Lock mutex_space;
	public static Lock mutex_chunk_messages;
	public static Lock mutex_chunks;
	public static Lock mutex_putchunk_messages;
	/*
	 * SPACE
	 */
	public static long usedSpace;
	public static long totalSpace;
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
	/*
	 * PATHS
	 */
	private static String backupPath;
	private static String restorePath;
	private static String restoredFilesPath;
	/*
	 * PROPERTIES
	 */
	private static Properties properties;
	
	public static void main(String args[]) throws IOException, InterruptedException{
		
		LoadProperties lp = new LoadProperties();
		loadProperties(lp.prop);
		
		if(args.length == 6){
			setUpSockets(args);
		} else {
			setUpSockets(lp.prop);
		}
		
		System.out.println(InetAddress.getLocalHost());
		System.out.println("TOTAL SPACE: " + totalSpace + " | " + "AVAILABLE SPACE: " + getAvailableSpace());
		
		mutex_chunk_messages = new ReentrantLock(true);
		mutex_space = new ReentrantLock(true);
		mutex_chunks = new ReentrantLock(true);
		mutex_putchunk_messages = new ReentrantLock(true);
		
		putchunk_messages = new LinkedList<Message>();
		getchunk_messages = new LinkedList<Message>();
		chunk_messages = new LinkedList<Message>();
		delete_messages = new LinkedList<Message>();
		removed_messages = new LinkedList<Pair<String,Message>>();
		
		fileList = new ConcurrentHashMap<String, Pair<FileID, Integer>>();
		
		peers = new LinkedList<Pair<String, ChunkInfo>>();
		chunks = new LinkedList<ChunkInfo>();
		
		Database.loadDatabase();
		
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
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			String command = inFromUser.readLine();
			String parts[] = command.split(" ");
			
			if(parts.length == 3 && parts[0].equals("backup")){
				String filename = parts[1];
				MyFile file = new MyFile(filename);
				FileBackup fb = new FileBackup(file, Integer.parseInt(parts[2]));
				fb.backup();
			} else if(parts.length == 2 && parts[0].equals("restore")){
				String filename = parts[1];
				@SuppressWarnings("unused")
				FileRestore fr = new FileRestore(filename, restoredFilesPath + File.separator + filename);
			} else if(parts.length == 2 && parts[0].equals("delete")){
				String filename = parts[1];
				FileDeletion fd = new FileDeletion(filename);
				fd.deleteOwnFile();
				fd.sendDeleteRequest();
			} else if(parts.length == 1 && parts[0].equals("reclaim")){
				PeerSpaceReclaiming psr = new PeerSpaceReclaiming();
				psr.reclaim();
			} else if(command.equals("quit")){
				quit();
			} else {
				System.out.println("INVALID INPUT");
			}
		}		
	}
	
	public static long writeChunk(Message msg){
		String path = null;
		if(msg.type == Message.Type.PUTCHUNK){
			path = backupPath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		} else {
			path = restorePath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		}
		long writtenSize = FileSystem.writeByteArray(path, msg.getBody());
		return writtenSize;
	}
	
	public static long getAvailableSpace(){
		return totalSpace - usedSpace;
	}
	
	public static Message chunkMessageExists(Message msg){
		@SuppressWarnings("unchecked")
		LinkedList<Message> new_chunk_messages = (LinkedList<Message>) chunk_messages.clone();
		for(int i = 0; i < new_chunk_messages.size(); i++){
			Message m = new_chunk_messages.get(i);
			if(m.getFileID().toString().equals(msg.getFileID().toString()) && m.getChunkNo() == msg.getChunkNo()){
				chunk_messages.remove(i);
				return m;
			}
		}
		return null;
	}
	
	public static byte[] readChunk(FileID fileId, Integer chunkNo) throws IOException {
        File f = new File(getBackupDir() + File.separator + fileId.toString() + File.separator + chunkNo.toString());
        if (!f.exists()){
        	System.err.println("BACKUP OF THE REQUESTED CHUNK NOT FOUND.");
        	return null;
        }
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        byte[] chunk = new byte[(int)f.length()];
        
        bis.read(chunk);        
        bis.close();
        return chunk;
    }
	
	public static byte[] getOriginalChunk(FileID fileID, Integer chunkNo) throws IOException {
		byte[] chunk;
		for(Entry<String, Pair<FileID, Integer>> entry : Peer.fileList.entrySet()){
			String path = entry.getKey();
			Pair<FileID, Integer> pair = entry.getValue();
			if(fileID.toString().equals(pair.getFirst().toString())){
				MyFile file = new MyFile(path);
				chunk = file.getChunk(chunkNo-1);
				return chunk;
			}
		}
		return null;
	}
	
	public static void removePeer(String peerIP, Message message){
		@SuppressWarnings("unchecked")
		LinkedList<Pair<String, ChunkInfo>> new_peers = (LinkedList<Pair<String, ChunkInfo>>) peers.clone();
		for(Pair<String, ChunkInfo> pair : new_peers){
			if(pair.getFirst().equals(peerIP) && pair.getSecond().getFileId().equals(message.getFileID().toString()) && pair.getSecond().getChunkNo() == message.getChunkNo()){
				peers.remove(pair);
			}
		}
	}
	
	public static void removeOwnFile(String path){
		File file = new File(path);
		if(file.delete()){
			System.out.println("DELETED " + path);
		}
		else System.err.println("FAILED TO DELETE FILE " + path);
	}
	
	public static int updateActualRepDegree(Message message, int value){
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.get(i);
			
			String fileID = chunk.getFileId().toString();
			Integer chunkNo = chunk.getChunkNo();
			Integer actualRD = chunk.getActualRD();
			
			if(message.getFileID().toString().equals(fileID) && message.chunkNo == chunkNo){
				chunk.setActualRD(actualRD+value);
				System.out.println("UPDATED ARD OF " + fileID + " " + chunkNo + " ARD: " + chunk.getActualRD());
				return chunk.getActualRD() - chunk.getDesiredRD();
			}
		}
		return 0;
	}
	
	public static void addChunk(Message message){
		ChunkInfo newChunk = new ChunkInfo(message.getFileID().toString(), message.chunkNo, message.getReplicationDeg(), 0);	
		mutex_chunks.lock();
		if(!chunks.contains(newChunk))
			chunks.add(newChunk);
		mutex_chunks.unlock();
	}
	
	public static ArrayList<ChunkInfo> getChunksWithHighRD(){
		ArrayList<ChunkInfo> list = new ArrayList<ChunkInfo>();
		if(chunks.size() == 0)
			return null;
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.get(i);
			
			if(chunk.getExtraDegree() > 0){/*CANDIDATE FOR REMOVAL*/
				list.add(chunk);
			}
		}
		return list;
	}
	
	public static int getRDOfChunk(Message message) {
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.get(i);	
			if(chunk.getFileId().equals(message.getFileID().toString()) && chunk.getChunkNo() == message.getChunkNo()){/*CANDIDATE FOR REMOVAL*/
				return chunk.getDesiredRD();
			}
		}
		return 0;
	}
	
	public static int getARDOfChunk(Message message) {
		for(int i = 0; i < chunks.size(); i++){
			ChunkInfo chunk = chunks.get(i);	
			if(chunk.getFileId().equals(message.getFileID().toString()) && chunk.getChunkNo() == message.getChunkNo()){/*CANDIDATE FOR REMOVAL*/
				return chunk.getActualRD();
			}
		}
		return 0;
	}
	
	public static void deleteChunks(String fileID) {
		@SuppressWarnings("unchecked")
		LinkedList<ChunkInfo> new_chunks = (LinkedList<ChunkInfo>) chunks.clone();
		mutex_chunks.lock();
		for(ChunkInfo ci : new_chunks){
			if(ci.getFileId().equals(fileID))
				chunks.remove(ci);
		}
		mutex_chunks.unlock();
	}
	
	private static void quit() throws IOException{
		FileOutputStream fos = new FileOutputStream("config.properties");
		
		properties.setProperty("usedSpace", Long.toString(usedSpace));
		properties.store(fos, null);
		fos.close();
		
		Database.updateDatabase();
		System.exit(0);
	}
	
	public static void loadProperties(Properties prop) throws IOException{
		properties = prop;
		
		backupPath = prop.getProperty("backup_path");
		File backupDir = new File(backupPath);
		if(!backupDir.exists()){
			boolean sucess_backup = new File(backupPath).mkdir();
			if(!sucess_backup){
				System.err.println("FAILED TO CREATE BACKUP DIRECTORY");
			}
		}
		
		restorePath = prop.getProperty("restore_path");
		File restoreDir = new File(restorePath);
		if(!restoreDir.exists()){
			boolean sucess_restore = new File(restorePath).mkdir();
			if(!sucess_restore){
				System.err.println("FAILED TO CREATE RESTORE DIRECTORY");
			}
		}
		
		restoredFilesPath = prop.getProperty("fileRestore_path");
		File restoredFilesDir = new File(restoredFilesPath);
		if(!restoredFilesDir.exists()){
			boolean sucess_restoredFiles = new File(restoredFilesPath).mkdir();
			if(!sucess_restoredFiles){
				System.err.println("FAILED TO CREATE RESTORED FILES DIRECTORY");
			}
		}
		
		Database.filePath = prop.getProperty("file_path");
		File fileList = new File(Database.filePath);
		if(!fileList.exists()){
			boolean sucess = fileList.createNewFile();
			if(!sucess){
				System.err.println("FAILED TO CREATE fileList.txt");
			}
		}
		
		Database.chunkPath = prop.getProperty("chunk_path");
		File chunkList = new File(Database.chunkPath);
		if(!chunkList.exists()){
			boolean sucess = chunkList.createNewFile();
			if(!sucess){
				System.err.println("FAILED TO CREATE chunkList.txt");
			}
		}
		
		totalSpace = Integer.parseInt(prop.getProperty("totalSpace"));
		usedSpace = Integer.parseInt(prop.getProperty("usedSpace"));
	}
	
	static void setUpSockets(Properties prop) throws IOException{
		/*MULTICAST CONTROL SETUP*/
		mc_saddr = new InetSocketAddress(prop.getProperty("mc_ip"), Integer.parseInt(prop.getProperty("mc_port")));
		mc_port = mc_saddr.getPort();
		mc_socket = new MulticastSocket(mc_saddr.getPort());
		mc_socket.setTimeToLive(1);
		/*MULTICAST DATA BACKUP CONTROL*/
		mdb_saddr = new InetSocketAddress(prop.getProperty("mdb_ip"), Integer.parseInt(prop.getProperty("mdb_port")));
		mdb_port = mdb_saddr.getPort();
		mdb_socket = new MulticastSocket(mdb_saddr.getPort());
		mdb_socket.setTimeToLive(1);
		/*MULTICAST DATA RESTORE CONTROL*/
		mdr_saddr = new InetSocketAddress(prop.getProperty("mdr_ip"), Integer.parseInt(prop.getProperty("mdr_port")));
		mdr_port = mdr_saddr.getPort();
		mdr_socket = new MulticastSocket(mdr_saddr.getPort());
		mdr_socket.setTimeToLive(1);
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
}
