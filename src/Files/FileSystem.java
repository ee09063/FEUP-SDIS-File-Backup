package Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import Main.Peer;


public class FileSystem {
	
	public static void createDirectory(String path){
		File directory = new File(path);
		directory.mkdirs();
	}
	
	public static long writeByteArray(String path, byte[] data){
		File file = new File(path).getAbsoluteFile();
		createDirectory(file.getParent());
	
		if(file.exists()) return 0L;
		try{
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
			return file.length();
		} catch (IOException e){
			e.printStackTrace();
			return 0L;
		}
	}
	
	public static boolean deleteFile(File path, boolean updateSpace) {
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFile(files[i], updateSpace);
                } else {
                	if(updateSpace)
                		Peer.usedSpace-=files[i].length();
                	files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
