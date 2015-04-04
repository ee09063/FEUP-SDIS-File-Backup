package Files;

public class Chunk {
	public static final int CHUNK_MAX_SIZE = 64000;
	
	public final int chunkNo;
	public final Integer replicationDeg;
	public final FileID fileID;
	public final byte[] data;
	
	public Chunk(int cn, int rd, FileID fileID, byte[] data){
		if(cn < 0 || cn > 999999){
			System.err.println("CHUNK NUMBER OUTSIDE LIMITS -> " + cn);
			System.exit(0);
		}
		this.chunkNo = cn;
		if(rd < 1 || rd > 9){
			System.err.println("CHUNK REPLICATION DEGREE OUTSIDE LIMITS -> " + rd);
			System.exit(0);
		}
		this.replicationDeg = rd;
		this.fileID = fileID;
		if(data.length > CHUNK_MAX_SIZE){
			System.err.println("CHUNK SIZE OUTSIDE LIMITS -> " + data.length);
		}
		this.data = data;
	}
	
	public Chunk(int cn, FileID fileID, byte[] data){
		if(cn < 0 || cn > 999999){
			System.err.println("CHUNK NUMBER OUTSIDE LIMITS -> " + cn);
			System.exit(0);
		}
        chunkNo = cn;
        replicationDeg = null;    
        this.fileID = fileID;
        if(data.length > CHUNK_MAX_SIZE){
			System.err.println("CHUNK SIZE OUTSIDE LIMITS -> " + data.length);
		}
        this.data = data;
	}
}
