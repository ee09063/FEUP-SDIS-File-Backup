package Database;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

import Files.FileID;

public class ChunkAdder extends SQLiteJob<Object> {
	
	private String fileID;
	private Integer chunkNo;
	private Integer replicationDegree;
	private Integer actualReplicationDegree;
	
	public ChunkAdder(String fileID, Integer chunkNo, Integer replicationDegree, Integer actualReplicationDegree){
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.replicationDegree = replicationDegree;
		this.actualReplicationDegree = actualReplicationDegree;
	}
	
	public static void exec(SQLiteConnection connection, String fileID, Integer chunkNo, Integer replicationDegree, Integer actualRepDegree){
		SQLiteStatement st1 = null;
		
		try{
			st1 = connection.prepare("INSERT INTO Chunk (fileId, chunkNo, replicationDegree, actualReplicationDegree) VALUES (?, ?, ?, ?)");
			st1.bind(1, fileID.toString());
			st1.bind(2, chunkNo);
			st1.bind(3, replicationDegree);
			st1.bind(4, actualRepDegree);
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			st1.dispose();
		}
	}

	@Override
	protected Object job(SQLiteConnection connection) throws Throwable {
		exec(connection, this.fileID, this.chunkNo, this.replicationDegree, this.actualReplicationDegree);
        return null;
	}
}
