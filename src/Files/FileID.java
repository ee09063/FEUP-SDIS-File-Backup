package Files;
import java.util.Arrays;


public class FileID {
	
	public String hexFileID = null;
	public byte[] fileID;
	
	public FileID(byte[] id){
		if(id.length != 32){
			System.err.println("FILEID LENGHT MUST BE 32; IS " + id.length);
			System.exit(0);
		}
		fileID = id.clone();
	}
	
	@Override
	public boolean equals(Object other){
		return(other instanceof FileID) && Arrays.equals(fileID, ((FileID)other).fileID);
	}
	
	@Override
	public String toString(){
		if(hexFileID == null){
			if(fileID == null){
				hexFileID = "";
				return hexFileID;
			}
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < fileID.length; i++){
			sb.append(String.format("%02X", fileID[i])); // convert byte to hexadecimal, one by one
		}
		hexFileID = sb.toString().toLowerCase();
		return hexFileID;
	}
	
	public FileID(String hexString) {
        String[] chars = hexString.split("(?<=\\G..)");
        
        if (chars.length != 32){
        	System.err.println("FILEID LENGHT MUST BE 32; IS " + chars.length);
			System.exit(0);
        }
        
        fileID = new byte[32];
        
        for (int i = 0; i < fileID.length; ++i)
            fileID[i] = (byte) Short.parseShort(chars[i], 16);
        
        hexFileID = hexString.toLowerCase();
    }
	
	public byte[] toArray(){
		return fileID;
	}
}
