package InitiatorProtocol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Files.FileID;
import Files.FileSystem;
import Main.Chunk;
import Main.Peer;
import Message.Message;
import Utilities.Pair;

public class FileRestore {

    private FileID fileId;
    private String destPath;
    private int numChunks;
    private int timeInterval = 1000;
    private int count = 0;
    private Timer timer;
	
    
	public FileRestore(String filePath, String destPath) throws IOException{
		
		File myFile = new File(filePath);
		String absPath = myFile.getAbsolutePath();
		Path p = FileSystems.getDefault().getPath(absPath);
		
		Pair<FileID, Integer> fileInfo = Peer.fileList.get(p.toString());
		
		if(fileInfo == null){
			throw new FileNotFoundException();
		}
		
		this.fileId = fileInfo.getfirst();
		this.numChunks = fileInfo.getsecond();
		this.destPath = destPath;
		/*
		 * 
		 */
		File dir = new File(Peer.getBackupDir() + File.separator + this.fileId.toString());
		if(!dir.exists()){
			throw new NoSuchFileException("Couldn't find directory '" + dir.getAbsolutePath() + "'");
		}
		
		File[] chunksListing = getSortedChunks(dir);
		
		File file = new File("restorationTest.jpg");
		if(!file.exists()){
			file.createNewFile();
		}
		
		try {
			FileOutputStream output = new FileOutputStream(file);
			byte[] chunk = new byte[Chunk.CHUNK_MAX_SIZE];
			for(File f : chunksListing){
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
				bis.read(chunk);
				output.write(chunk, 0, (int)f.length());
				bis.close();
			}
			output.close();
		} catch (IOException e) {
			if(file.exists()) file.delete();
			throw e;
		}
		
		System.out.println("File Restoration Complete");
		
		/*DELETE THE DIRECTORY IN RESTORE*/
		File deleteDir = new File(Peer.getRestoreDir() + File.separator + this.fileId.toString());
		FileSystem.deleteFile(deleteDir);
		/*
		 * 
		 */
		try {
			Restore();
			TaskManager task = new TaskManager();
			task.startTask();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Restore() throws IOException{
		for(int i = 0; i < this.numChunks; i++){
			new ChunkRestore(this.fileId, i+1);
		}
	}

	private int countChunks() throws NoSuchFileException{
		File dir = new File(Peer.getRestoreDir() + File.separator + this.fileId.toString());
		
		if(!dir.exists()){
			return 0;
		}
		
		File[] listing = dir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.matches("^[0-9]+$");
			}
		});
		
		return listing.length;
	}
	
	private void restoreFile() throws IOException{
		File dir = new File(Peer.getRestoreDir() + File.separator + this.fileId.toString());
		if(!dir.exists()){
			throw new NoSuchFileException("Couldn't find directory '" + dir.getAbsolutePath() + "'");
		}
		
		File[] chunksListing = getSortedChunks(dir);
		
		writeChunksToFile(chunksListing);
	}
	
	private File[] getSortedChunks(File dir){
		File[] listing = dir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.matches("^[0-9]+$");
			}
		});
		Arrays.sort(listing, new Comparator<File>(){
			@Override
			public int compare(File a, File b){
				Integer numA = Integer.parseInt(a.getName());
				Integer numB = Integer.parseInt(b.getName());
				return numA.compareTo(numB);
			}
		});
		return listing;
	}
	
	private void writeChunksToFile(File[] listing) throws IOException{
		File file = new File(this.destPath);
		if(!file.exists()){
			file.createNewFile();
		}
		
		try {
			FileOutputStream output = new FileOutputStream(file);
			byte[] chunk = new byte[Chunk.CHUNK_MAX_SIZE];
			for(File f : listing){
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
				bis.read(chunk);
				output.write(chunk, 0, (int)f.length());
				bis.close();
			}
			output.close();
		} catch (IOException e) {
			if(file.exists()) file.delete();
			throw e;
		}
		
		System.out.println("File Restoration Complete");
		
		/*DELETE THE DIRECTORY IN RESTORE*/
		File deleteDir = new File(Peer.getRestoreDir() + File.separator + this.fileId.toString());
		FileSystem.deleteFile(deleteDir);
	}
	
	public class TaskManager {
	    private Timer timer = new Timer();
	 
	    public void startTask() {
	        timer.schedule(new PeriodicTask(), timeInterval);
	    }

	    private class PeriodicTask extends TimerTask {
	        @Override
	        public void run(){
	        	count++;
	        	if(count == 5){
	        		System.out.println("GAME OVER MAN, GAME OVER! -> FileRestore");
	        		timer.cancel();
	        		timer.purge();
	        	}else{
	        		try {
						if(countChunks() == numChunks){
							try {
								restoreFile();
								timer.cancel();
								timer.purge();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}else{
							timeInterval*=2;
							timer.schedule(new PeriodicTask(), timeInterval);
						}
					} catch (NoSuchFileException e) {
						e.printStackTrace();
					}
	        	}
	        }
	    }
	}
}
