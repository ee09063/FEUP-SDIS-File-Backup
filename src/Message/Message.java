package Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import Files.ChunkInfo;
import Files.FileID;
import Main.Chunk;


public class Message {
	public enum Type{
		PUTCHUNK, GETCHUNK, CHUNK, STORED, DELETE, REMOVED
	}
	
	public final Type type;
	
	public Message(Type type){
		this.type=type;
	}
	
	private FileID fileID = null;
	
	protected void setFileID(byte[] fileID){
		this.fileID = new FileID(fileID); 
	}
	
	public void setFileID(FileID fileID){
		this.fileID = fileID;
	}
	
	public String getHexFileID(){
		return this.fileID.toString();
	}
	
	private byte[] version = null;
	
	public void setVersion(int i, int j){
		if(version == null){
			version = new byte[2];
		}
		version[0] = (byte)i;
		version[1] = (byte)j;
	}
	
	public int[] getVersion() {
		return new int[]{(int) version[0], (int) version[1]};
	}
	
	public Integer chunkNo = null;
	
	private Byte replicationDeg = null;
	
	private byte[] body = null;
	
	public FileID getFileID(){
		return this.fileID;
	}
	
	public void setChunkNo(int c){
		this.chunkNo = c;
	}
	
	public Integer getChunkNo(){
		return this.chunkNo;
	}
	
	public Integer getReplicationDeg(){
		return this.replicationDeg.intValue();
	}
	
	protected void setReplicationDegree(int i){
		this.replicationDeg = (byte)i;
	}
	
	protected void setBody(byte[] b){
		this.body = b;
	}
	
	public byte[] getBody(){
		return this.body;
	}
	
	public byte[] toByteArray(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(this.type.toString());
		
		if(this.version != null){
			sb.append(' ' + Byte.toString(version[0]) + '.' + Byte.toString(version[1]));
		}
		
		if(this.fileID != null){
			sb.append(' ');
			String fileIdStr = fileID.toString();
			sb.append(fileIdStr);
		}
		
		if(this.chunkNo != null){
			sb.append(' ' + this.chunkNo.toString());
		}
		
		if(this.replicationDeg != null){
			sb.append(' ' + this.replicationDeg.toString());
		}
		
		sb.append("\r\n\r\n");
		
		byte[] result;
		
		result = sb.toString().getBytes(StandardCharsets.US_ASCII);
		
		if(this.body != null){
			int prevLength = result.length;
			result = Arrays.copyOf(result, prevLength + this.body.length);
			System.arraycopy(body, 0, result, prevLength, body.length);
		}

		return result;
	}
	
	public static Message makePutChunk(Chunk chunk){
		Message result = new Message(Type.PUTCHUNK);
		
		result.setVersion(1, 0);
		result.setChunkNo(chunk.chunkNo);
		result.setReplicationDegree(chunk.replicationDeg);
		result.setFileID(chunk.fileID);
		result.setBody(chunk.data);
		
		return result;
	}
	
	public static Message makeStored(FileID fileID, int chunkNo){
		Message result = new Message(Type.STORED);
		
		result.setVersion(1, 0);
		result.setFileID(fileID);
		result.setChunkNo(chunkNo);
		
		return result;
	}
	
	public static Message makeGetChunk(FileID fileID, int chunkNo) {
        Message result = new Message(Type.GETCHUNK);
        
        result.setVersion(1, 0);
        result.setFileID(fileID);
        result.setChunkNo(chunkNo);

        return result;
    }
	
	 public static Message makeChunk(Chunk chunk) {
        Message result = new Message(Type.CHUNK);

        result.setVersion(1, 0);
        result.setChunkNo(chunk.chunkNo);
        result.setFileID(chunk.fileID);
        result.setBody(chunk.data);

        return result;
    }
	
	 public static Message makeDelete(FileID fileID){
		 Message result = new Message(Type.DELETE);
		 result.setVersion(1, 0);
		 result.setFileID(fileID);
		 return result;
	 }
	 
	 public static Message makeRemoved(FileID fileID, int chunkNo) {
	        Message result = new Message(Type.REMOVED);
	        
	        result.setVersion(1, 0);
	        result.setFileID(fileID);
	        result.setChunkNo(chunkNo);

	        return result;
	    }
	 
	public static Message fromByteArray(byte[] data) throws IOException{
		Message msg = null;
		
		byte[] header = null;
		byte[] body  = null;
		
		for(int i = 0; i < data.length; i++){
			if(data[i] == 0xD){
				header = new byte[i];
				System.arraycopy(data, 0, header, 0, header.length);
				body = new byte[data.length - (i+4)];
				System.arraycopy(data, i+4, body, 0, body.length);
				break;
			}
		}

		String messageHeader = new String(header, "iso8859-1");
		
		String[] headerParts = messageHeader.split(" ");
		String messageType = headerParts[0];
		String fileID = headerParts[2];
		
		
		if(messageType.equals("PUTCHUNK")){
			String chunkNo = headerParts[3];
			String replicationDegree = headerParts[4];
			msg = new Message(Message.Type.PUTCHUNK);
			msg.setVersion(1, 0);
			msg.setFileID(hexStringToByteArray(fileID));
			msg.setChunkNo(Integer.parseInt(chunkNo));
			msg.setReplicationDegree(Integer.parseInt(replicationDegree));
			msg.setBody(body);
			return msg;
		} else if(messageType.equals("STORED")) {
			String chunkNo = headerParts[3];
			msg = new Message(Message.Type.STORED);
			msg.setVersion(1, 0);
			msg.setFileID(hexStringToByteArray(fileID));
			msg.setChunkNo(Integer.parseInt(chunkNo));
			msg.setBody(null);
			return msg;
		} else if(messageType.equals("GETCHUNK")) {
			String chunkNo = headerParts[3];
			msg = new Message(Message.Type.GETCHUNK);
			msg.setVersion(1, 0);
			msg.setFileID(hexStringToByteArray(fileID));
			msg.setChunkNo(Integer.parseInt(chunkNo));
			msg.setBody(null);
			return msg;
		} else if(messageType.equals("CHUNK")){
			String chunkNo = headerParts[3];
			msg = new Message(Message.Type.CHUNK);
			msg.setVersion(1, 0);
			msg.setFileID(hexStringToByteArray(fileID));
			msg.setChunkNo(Integer.parseInt(chunkNo));
			msg.setBody(body);
			return msg;
		} else if(messageType.equals("DELETE")){
			msg = new Message(Message.Type.DELETE);
			msg.setVersion(1, 0);
			msg.setFileID(hexStringToByteArray(fileID));
			msg.setBody(null);
			return msg;
		} else if(messageType.equals("REMOVED")) {
			String chunkNo = headerParts[3];
			msg = new Message(Message.Type.REMOVED);
			msg.setVersion(1,0);
			msg.setFileID(hexStringToByteArray(fileID));
			msg.setChunkNo(Integer.parseInt(chunkNo));
			msg.setBody(null);
			return msg;
		}
		return null;
	}

	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	@Override
	public String toString(){
		return new String(this.toByteArray());
	}
	
	 public boolean equals(Object other) {
	    	if (other instanceof ChunkInfo) {
	    		Message om = (Message) other;
	    		return (this.type == om.type && this.fileID == om.fileID && this.chunkNo == om.chunkNo && this.body == om.body && this.replicationDeg == om.replicationDeg);
	    	}
	    	return false;
	    }
	
	
	
}