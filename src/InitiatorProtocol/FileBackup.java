package InitiatorProtocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Files.MyFile;
import Main.Chunk;


public class FileBackup {
	
	private int numChunks;
	int replicationDegree;
	MyFile file;
	
	public FileBackup(final MyFile file, int replicationDegree){
		if(replicationDegree < 1 || replicationDegree > 9)
			throw new IllegalArgumentException("Replication Degree must be between 1 and 9");
		
		this.replicationDegree = replicationDegree;
		this.file = file;	
		
		try {
			System.out.println("PC " + InetAddress.getLocalHost() + " IS INITIATING BACKUP OF FILE " + file.getPath());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void Send() throws IOException{
		this.numChunks = file.getNumberofChunks();
		//this.file.open();
		
		System.out.println("Number of chunks: " + this.numChunks + "  Size: " + this.file.getFileSize());
		
		for(int i = 0; i < numChunks; i++){
			byte[] chunkArray = this.file.getChunk(i);
			Chunk chunk = new Chunk(i+1, this.replicationDegree, this.file.getFileID(), chunkArray);
			new ChunkBackup(chunk);
		}
	}
}
