package Database;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

public class Database {
	private static final String _databaseCreationString  = "BEGIN TRANSACTION;                                        "
            + "PRAGMA foreign_keys = ON;                                                                              "
            + "                                                                                                       "
            + "DROP TABLE IF EXISTS FileBackup;                                                                       "
            + "DROP TABLE IF EXISTS File;                                                                             "
            + "DROP TABLE IF EXISTS Chunk;                                                                            "
            + "DROP TABLE IF EXISTS Ip;                                                                               "
            + "                                                                                                       "
            + "CREATE TABLE FileBackup (                                                                              "
            + "    id INTEGER NOT NULL,                                                                               "
            + "    filePath TEXT NOT NULL,                                                                            "
            + "    fileId TEXT NOT NULL,                                                                              "
            + "    numberChunks INTEGER NOT NULL,                                                                     "
            + "                                                                                                       "
            + "    CONSTRAINT FileBackup_PK PRIMARY KEY (id),                                                         "
            + "    CONSTRAINT FileBackupId_Unique UNIQUE(fileId),                                                     "
            + "    CONSTRAINT FileBackupPath_Unique UNIQUE(filePath),                                                 "
            + "    CONSTRAINT FileBackupId_Size64 CHECK(length(fileId) = 64)                                          "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE File (                                                                                    "
            + "    id INTEGER NOT NULL,                                                                               "
            + "    fileId TEXT NOT NULL,                                                                              "
            + "                                                                                                       "
            + "    CONSTRAINT file_PK PRIMARY KEY (id),                                                               "
            + "    CONSTRAINT fileId_Unique UNIQUE(fileId),                                                           "
            + "    CONSTRAINT fileId_Size64 CHECK(length(fileId) = 64)                                                "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE Chunk (                                                                                   "
            + "    id INTEGER NOT NULL,                                                                               "
            + "    fileId INTEGER NOT NULL,                                                                           "
            + "    chunkNo INTEGER NOT NULL,                                                                          "
            + "    replicationDegree INTEGER NOT NULL,                                                                "
            + "                                                                                                       "
            + "    CONSTRAINT chunk_PK PRIMARY KEY (id),                                                              "
            + "    CONSTRAINT fileId_chunkNo_Unique UNIQUE(fileId, chunkNo),                                          "
            + "    CONSTRAINT chunk_file_FK FOREIGN KEY (fileId) REFERENCES File(id) ON DELETE CASCADE                "
            + "    CONSTRAINT chunk_replication_degree_CHECK CHECK(replicationDegree >= 0 AND replicationDegree <= 9) "
            + ");                                                                                                     "
            + "                                                                                                       "
            + "CREATE TABLE Ip (                                                                                      "
            + "    chunkId INTEGER NOT NULL,                                                                          "
            + "    IP TEXT NOT NULL,                                                                                  "
            + "                                                                                                       "
            + "    CONSTRAINT Ip_PK PRIMARY KEY(chunkId, IP),                                                         "
            + "    CONSTRAINT ip_chunk_FK FOREIGN KEY (chunkId) REFERENCES Chunk(id) ON DELETE CASCADE                "
            + ");                                                                                                     "
            + "                                                                                                       ";

	 private SQLiteQueue queue;

	public Database(String databaseFilePath) {
        this(new File(databaseFilePath));
    }


	public Database(File databaseFile) {
        try {

            SQLiteConnection db = new SQLiteConnection(databaseFile.getAbsoluteFile());
            db.open(true);

            int numResults = 0;
            SQLiteStatement testSt = null;
            try {
                testSt = db.prepare("SELECT 1 FROM sqlite_master WHERE type='table' AND name='File'");
                while (testSt.step())
                    numResults++;
            } finally {
                if (testSt != null)
                    testSt.dispose();
            }

            if (numResults > 0)
                loadDatabase(db);
            else
                createDatabase(db);

            db.dispose();
            queue = new SQLiteQueue(databaseFile.getAbsoluteFile());
            queue.start();

            queue.execute(new SQLiteJob<Object>() {
                @Override
                protected Object job(SQLiteConnection connection) throws Throwable {
                    connection.exec("PRAGMA foreign_keys = ON;");
                    return null;
                }
            });
        } catch (SQLiteException e) { }
    }

	private void createDatabase(SQLiteConnection db) throws SQLiteException {
        db.exec(_databaseCreationString);
    }

	private void loadDatabase(SQLiteConnection db) {
		
	}

}
