package InitiatorProtocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Files.Chunk;
import Files.FileID;
import Files.MyFile;
import Main.Peer;
import Utilities.Pair;


public class FileBackup {
	
	private int numChunks;
	int replicationDegree;
	MyFile file;
	
	public FileBackup(final MyFile file, int replicationDegree){
		if(replicationDegree < 1 || replicationDegree > 9)
			throw new IllegalArgumentException("Replication Degree must be between 1 and 9");
		
		this.replicationDegree = replicationDegree;
		this.file = file;	
	}
	
	public void backup() throws IOException, InterruptedException{
		/*
		 * IF FILE WAS ALREADY BACKED UP DELETE THE EXISTING VERSION
		 */
		if(null != Peer.fileList.get(file.getPath())){
			System.out.println("OLDER FILE VERSION DETECTED. DELETING BEFORE BACKUP...");
			FileDeletion fd = new FileDeletion(file.getPath());
			try {
				fd.sendDeleteRequest();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(500);
		}
		/*
		 * 
		 */
		try {
			System.out.println("PC " + InetAddress.getLocalHost() + " IS INITIATING BACKUP OF FILE " + file.getPath());
			Pair<FileID, Integer> pair = new Pair<FileID, Integer>(file.getFileID(), file.getNumberofChunks());
			if(!Peer.fileList.containsKey(file.getPath())){
				Peer.fileList.put(file.getPath(), pair);
			}
			this.Send();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Send() throws IOException{
		this.numChunks = file.getNumberofChunks();
		
		System.out.println("Number of chunks: " + this.numChunks + "  Size: " + this.file.getFileSize());
		
		for(int i = 0; i < numChunks; i++){
			byte[] chunkArray = this.file.getChunk(i);
			Chunk chunk = new Chunk(i+1, this.replicationDegree, this.file.getFileID(), chunkArray);
			new ChunkBackup(chunk);
		}
	}
}
