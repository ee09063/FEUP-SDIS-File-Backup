package Main;
import Files.FileID;


public class Chunk {
	public static final int CHUNK_MAX_SIZE = 64000;
	
	public final int chunkNo;
	public final Integer replicationDeg;
	public final FileID fileID;
	public final byte[] data;
	
	public Chunk(int cn, int rd, FileID fileID, byte[] data){
		if(cn < 0 || cn > 999999) throw new IllegalArgumentException("ERROR: Chunk number outside allowed values");
		this.chunkNo = cn;
		if(rd < 1 || rd > 9) throw new IllegalArgumentException("ERROR: Replication Degree of Chunk outside allowed Values");
		this.replicationDeg = rd;
		this.fileID = fileID;
		if(data.length > CHUNK_MAX_SIZE) throw new IllegalArgumentException("ERROR: Data larger than CHUNK_MAX_SIZE -> " + data.length);
		this.data = data;
	}
	
	public Chunk(int cn, FileID fid, byte[] data){
		if (cn < 0 || cn > 999999) throw new IllegalArgumentException();
        chunkNo = cn;
        replicationDeg = null;    
        fileID = fid;
        if (data.length > 64000) throw new IllegalArgumentException();
        this.data = data;
	}
}
