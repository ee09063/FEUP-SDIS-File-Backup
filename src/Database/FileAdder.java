package Database;

import Files.FileID;
import Utilities.Pair;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class FileAdder extends SQLiteJob<Object> {
	
	private String filePath;
    private FileID fileID;
    private Integer numberOfChunks;
	    
	    
    public FileAdder(String filePath, FileID fileId, Integer numberOfChunks) {
        this.filePath = filePath;
        this.fileID = fileId;
        this.numberOfChunks = numberOfChunks;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, filePath, fileID, numberOfChunks);
        return null;
    }

    public static void exec(SQLiteConnection connection, String filePath, FileID fileId, Integer numberOfChunks) throws SQLiteException {
        String fileID = fileId.toString();

        SQLiteStatement st = connection.prepare("INSERT INTO OwnFile (filePath, fileId, numberChunks) values(?, ?, ?)");
        try {
            st.bind(1, filePath);
            st.bind(2, fileID);
            st.bind(3, numberOfChunks);
            st.stepThrough();
        } finally {
            st.dispose();
        }
    }

   
}