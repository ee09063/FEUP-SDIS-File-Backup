
public class MessageFilter {
	private Peer peer;
	
	public MessageFilter(Peer peer){
		this.peer = peer;
	}
	
	public void filterMessage(Message message){
		//System.out.println("FILTERING MESSAGE");
		if(message.type == Message.Type.STORED){
			Peer.stored.add(message);
			//System.out.println("STORED MESSAGES : " + stored.size());
		} else if(message.type == Message.Type.PUTCHUNK){
			PeerChunkBackup pcb = new PeerChunkBackup(message);
		}
	}
}
