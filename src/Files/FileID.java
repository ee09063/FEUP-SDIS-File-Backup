package Files;
import java.util.Arrays;


public class FileID {
	
	public String _hexFileID = null;
	public byte[] _fileID;
	
	public FileID(byte[] id){
		if(id.length != 32) throw new IllegalArgumentException("FileID Byte Array length must be 32. Has " + id.length);
		_fileID = id.clone();
	}
	
	@Override
	public boolean equals(Object other){
		return(other instanceof FileID) && Arrays.equals(_fileID, ((FileID)other)._fileID);
	}
	
	@Override
	public int hashCode(){
		return toString().hashCode();
	}
	
	@Override
	public String toString(){
		if(_hexFileID == null){
			if(_fileID == null){
				_hexFileID = "";
				return _hexFileID;
			}
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < _fileID.length; i++){
			sb.append(String.format("%02X", _fileID[i])); // convert byte to hexadecimal, one by one
		}
		_hexFileID = sb.toString().toLowerCase();
		return _hexFileID;
	}
	
	public FileID(String hexString) {
        String[] chars = hexString.split("(?<=\\G..)");
        
        if (chars.length != 32) 
            throw new IllegalArgumentException("FileID must have 32 bytes.");
        
        _fileID = new byte[32];
        
        for (int i = 0; i < _fileID.length; ++i)
            _fileID[i] = (byte) Short.parseShort(chars[i], 16);
        
        _hexFileID = hexString.toLowerCase();
    }
	
	public byte[] toArray() {return _fileID;}
}
