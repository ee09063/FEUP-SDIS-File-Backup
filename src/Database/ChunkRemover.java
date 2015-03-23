package Database;

import Files.FileID;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;


public class ChunkRemover extends SQLiteJob<Object> {
	
    private FileID fileID;
    private Integer chunkNo;
	
    public ChunkRemover(FileID fileID, Integer chunkNo) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, this.fileID, this.chunkNo);
        return null;
    }

    public static void exec(SQLiteConnection connection, FileID fileID, Integer chunkNo) throws SQLiteException {

        SQLiteStatement st = connection.prepare("DELETE FROM Chunk WHERE fileID = ? AND chunkNo = ?");
        try {
            st.bind(1, fileID.toString());
            st.bind(2, chunkNo);
            st.stepThrough();
        } finally {
            st.dispose();
        }
    }
}
