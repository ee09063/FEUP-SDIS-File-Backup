import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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
}
