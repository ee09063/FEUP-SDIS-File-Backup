package Database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import Files.ChunkInfo;
import Files.FileID;
import Files.MyFile;
import Main.Peer;
import Utilities.Pair;


public class Database {
	final static String filePath = "fileList.txt";
	final static String chunkPath = "chunkList.txt";
	
	public static void loadDatabase(){
		/*
		 * LOAD THE FILE LIST
		 */
		try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
			for(String line; (line = reader.readLine()) != null; ){
				String[] info = line.split(Pattern.quote("|"));
				String path = info[0].trim();
				String fileID = info[1];
				Integer nOfChunks = Integer.parseInt(info[2]);
				Pair<FileID, Integer> pair = new Pair<FileID, Integer>(new FileID(fileID), nOfChunks);
				Peer.fileList.put(path, pair);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * LOAD THE CHUNK LIST
		 */
		try(BufferedReader reader = new BufferedReader(new FileReader(chunkPath))){
			for(String line; (line = reader.readLine()) != null; ){
				String[] info = line.split(Pattern.quote("|"));
				String fileID = info[0].trim();
				Integer chunkNo = Integer.parseInt(info[1]);
				Integer drd = Integer.parseInt(info[2]);
				Integer ard = Integer.parseInt(info[3]);
				ChunkInfo ci = new ChunkInfo(fileID,chunkNo,drd,ard);
				Peer.chunks.add(ci);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateDatabase(){
		/*
		 * UPDATE THE FILES
		 */
		try {
			PrintWriter pwFLU = new PrintWriter(filePath);
			for(Entry<String, Pair<FileID, Integer>> entry : Peer.fileList.entrySet()){
				String path = entry.getKey();
				Pair<FileID, Integer> pair = entry.getValue();
				pwFLU.println(path + "|" + pair.getfirst().toString() + "|" + pair.getsecond());
			}
			pwFLU.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/*
		 * UPDATE THE CHUNKS
		 */
		try {
			PrintWriter pwCLU = new PrintWriter(chunkPath);
			for(ChunkInfo ci : Peer.chunks){
				pwCLU.println(ci.getFileId() + "|" + ci.getChunkNo() + "|" + ci.getDesiredRD() + "|" + ci.getActualRD());
			}
			pwCLU.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}










































