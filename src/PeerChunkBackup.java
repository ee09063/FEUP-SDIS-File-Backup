
public class PeerChunkBackup {
	/*
	 * RECEIVING A PUTCHUNK MESSAGE CREATES A NEW PEERCHUNKBACKUP THAT
	 * RECEIVES THE MESSAGE? BYTE ARRAY? STRING?, GUARDS THE CHUNK AND INITIATES A TIMER/SCHEDULE 
	 * WITH A RANDOM DELAY BETWEEN 0 AND 400 MS 
	 */
	
	PeerChunkBackup(Message msg){
		Message storedMsg = new Message(Message.Type.STORED);
		storedMsg.setFileID(msg.getFileID());
		storedMsg.setVersion(1, 0);
		storedMsg.setChunkNo(msg.getChunkNo());
		
		/*System.out.println(new String(storedMsg.toByteArray()));*/
		
		Peer.writeChunk(msg);
	}
}
