import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;


public class ChunkMaker {

		public static void makeChunks(File file){
			int part_counter = 0;
			
			int chunk_size = 1024 * 64; //64KB
			
			/*CREATE HEADER FIRST, GET SIZE THEN SUBTRACT TO GET THE BODY SIZE*/
			String codifier = null;
			codifier = codifierString(file);
			byte[] codified = hashString(codifier);
			
			String newStr = new String(codified);
			
			FileID fid = new FileID(newStr);
			
			System.out.println(fid);
			
			try {
				System.out.write(codified);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public static String codifierString(File file){
			UserPrincipal owner = null;
			try {
				owner = java.nio.file.Files.getOwner(file.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        String owner_str = owner.getName();
			System.out.println("owner: " + owner_str);
	  
			System.out.println("Before Format : " + file.lastModified());	 
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			String date_str = sdf.format(file.lastModified());
			System.out.println("After Format : " + date_str);
		
			return owner.getName() + " " + date_str;
		}
		
		public static byte[] hashString(String codifier){
			MessageDigest sha = null;
			try {
				sha = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sha.update(codifier.getBytes());
			byte[] digest = sha.digest();
			return digest;
		}
}
