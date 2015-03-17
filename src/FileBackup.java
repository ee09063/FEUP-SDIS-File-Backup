import java.io.IOException;


public class FileBackup {
	
	private int numChunks;
	int replicationDegree;
	MyFile file;
	
	public FileBackup(final MyFile file, int replicationDegree){
		System.out.println("DB2");
		if(replicationDegree < 1 || replicationDegree > 9)
			throw new IllegalArgumentException("Replication Degree must be between 1 and 9");
		
		this.replicationDegree = replicationDegree;
		this.file = file;		
	}
	
	public void Send() throws IOException{
		this.numChunks = file.getNumberofChunks();
		this.file.open();
		
		System.out.println("Number of chunks: " + this.numChunks + "  Size: " + this.file.getFileSize());
		
		for(int i = 0; i < numChunks; i++){
			byte[] chunkArray = this.file.getChunk(i);
			Chunk chunk = new Chunk(i, this.replicationDegree, this.file.getFileID(), chunkArray);
			new ChunkBackup(chunk);
		}
	}
}
