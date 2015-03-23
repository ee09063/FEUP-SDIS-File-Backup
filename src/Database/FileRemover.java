package Database;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteStatement;

class OwnFileRemover extends SQLiteJob<Object> {
   
	private String filePath;
	
	public OwnFileRemover(String filePath) {
        this.filePath = filePath;
    }

    @Override
    protected Object job(SQLiteConnection connection) throws Throwable {
        exec(connection, filePath);
        return null;
    }

    public static void exec(SQLiteConnection connection, String filePath) throws SQLiteException {
        SQLiteStatement st = connection.prepare("DELETE FROM OwnFile WHERE filePath = ?");
        try {
            st.bind(1, filePath);
            st.stepThrough();
        } finally {
            st.dispose();
        }
    }
}