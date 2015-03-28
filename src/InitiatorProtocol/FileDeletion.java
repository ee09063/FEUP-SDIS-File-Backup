package InitiatorProtocol;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import Files.FileID;
import Main.Peer;
import Message.Message;
import Utilities.Pair;

public class FileDeletion {
	
	public FileDeletion(String filePath) throws IOException{
		
		File myFile = new File(filePath);
		String absPath = myFile.getAbsolutePath();
		Path p = FileSystems.getDefault().getPath(absPath);
		
		System.out.println("DELETING " + p.toString());
		
		Peer.removeOwnFile(p.toString());
		
		Pair<FileID, Integer> fileInfo = Peer.fileList.get(p.toString());
		
		if(fileInfo == null){
			System.out.println("BACKUP NOT DETECTED.");
			return;
		} else {
			//System.out.println("REMOVING FILE FROM LOCAL STORAGE");
			Peer.fileList.remove(p.toString());
		}
		
		Message msg = Message.makeDelete(fileInfo.getfirst());
		DatagramPacket packet = new DatagramPacket(msg.toByteArray(),
													msg.toByteArray().length,
													Peer.mc_saddr.getAddress(),
													Peer.mc_saddr.getPort());
		
		System.out.println("SENDING DELETE REQUEST...");
		Peer.mc_socket.send(packet);
	}

}
