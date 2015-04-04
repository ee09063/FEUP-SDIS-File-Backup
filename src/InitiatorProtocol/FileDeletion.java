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
	
	Path path;
	
	public FileDeletion(String filePath) throws IOException{
		File myFile = new File(filePath);
		String absPath = myFile.getAbsolutePath();
		path = FileSystems.getDefault().getPath(absPath);
	}
	
	public void deleteOwnFile(){
		System.out.println("DELETING " + path.toString());
		Peer.removeOwnFile(path.toString());
	}
	
	public void sendDeleteRequest() throws IOException{
		Pair<FileID, Integer> fileInfo = Peer.fileList.get(path.toString());
		
		if(fileInfo == null){
			System.out.println("BACKUP NOT DETECTED.");
			return;
		} else {
			Peer.deleteChunks(fileInfo.getFirst().toString());
			Peer.fileList.remove(path.toString());
		}
		
		Message msg = Message.makeDelete(fileInfo.getFirst());
		DatagramPacket packet = new DatagramPacket(msg.toByteArray(),
													msg.toByteArray().length,
													Peer.mc_saddr.getAddress(),
													Peer.mc_saddr.getPort());
		
		System.out.println("SENDING DELETE REQUEST...");
		Peer.mc_socket.send(packet);
	}

}
