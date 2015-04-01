package PeerProtocol;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import Files.FileSystem;
import Main.Peer;
import Message.Message;

public class PeerFileDeletion {
	public PeerFileDeletion(Message msg){
		File file = new File(Peer.getBackupDir() + File.separator + msg.getFileID().toString());
		String absPath = file.getAbsolutePath();
		Path p = FileSystems.getDefault().getPath(absPath);

		if(FileSystem.deleteFile(file, true)){
			System.out.println("DELETED BACKUP " + p.toString());
			Peer.deleteChunks(msg.getFileID().toString());
		} else 
			System.out.println("COULD NOT DELETE BACKUP " + p.toString());
	}
}
