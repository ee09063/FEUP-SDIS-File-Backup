import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;


public class Peer {
	/*ARGUMENTS -> MC_IP, MC_PORT, MDB_IP, MDB_PORT, MBD_IP, MDB_PORT*/
	public static void main(String args[]) throws IOException{
		/*if(args.length == 6){
			setUpSockets(args);
		}*/
		setUpSocketsDefault();
		while(true){
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			String command = inFromUser.readLine();
			String parts[] = command.split(" ");
			if(parts.length == 2 && parts[0].equals("BACKUP")){
				String filename = parts[1];
				MyFile test = new MyFile(filename);
				FileBackup fb = new FileBackup(test, 1);
				fb.Send();
			} else if(command.equals("PEER")){
				break;
			} else{
				System.out.println("INVALID INPUT");
				System.exit(-1);
			}
		}
		System.out.println("ACTING AS PEER - JOINING GROUP...");
		mdb_socket.joinGroup(mdb_saddr.getAddress());
		while(true){
			byte[] receiveData = new byte[Chunk.CHUNK_MAX_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			mdb_socket.receive(receivePacket);
			/*System.out.println("RECEIVED: ");
			System.out.println(new String(receivePacket.getData()));*/
			Message receivedMsg = Message.fromByteArray(receivePacket.getData());
			Peer.writeChunk(receivedMsg);
		}
	}
	
	public static void writeChunk(Message msg){
		String path = null;
		if(msg.type == Message.Type.PUTCHUNK){
			path = backupPath + File.separator + msg.getHexFileID() + File.separator + msg.chunkNo.toString();
		}
		//System.out.println(new String(msg.getBody()));
		long writtenSize = FileSystem.writeByteArray(path, msg.getBody());
	}
	
	static void setUpSockets(String args[]) throws IOException{
		/*MULTICAST CONTROL SETUP*/
		mc_saddr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
		mc_port = mc_saddr.getPort();
		mc_socket = new MulticastSocket(mc_saddr.getPort());
		mc_socket.setTimeToLive(1);
		/*MULTICAST DATA BACKUP CONTROL*/
		mdb_saddr = new InetSocketAddress(args[2], Integer.parseInt(args[3]));
		mdb_port = mdb_saddr.getPort();
		mdb_socket = new MulticastSocket(mdb_saddr.getPort());
		mdb_socket.setTimeToLive(1);
		/*MULTICAST DATA RESTORE CONTROL*/
		mdr_saddr = new InetSocketAddress(args[4], Integer.parseInt(args[5]));
		mdr_port = mdr_saddr.getPort();
		mdr_socket = new MulticastSocket(mdr_saddr.getPort());
		mdr_socket.setTimeToLive(1);	
	}
	
	static void setUpSocketsDefault() throws IOException{
		/*MULTICAST CONTROL SETUP*/
		mc_saddr = new InetSocketAddress("239.0.0.4", 4444);
		mc_port = mc_saddr.getPort();
		mc_socket = new MulticastSocket(mc_saddr.getPort());
		mc_socket.setTimeToLive(1);
		/*MULTICAST DATA BACKUP CONTROL*/
		mdb_saddr = new InetSocketAddress("239.0.0.3", 3333);
		mdb_port = mdb_saddr.getPort();
		mdb_socket = new MulticastSocket(mdb_saddr.getPort());
		mdb_socket.setTimeToLive(1);
	}
	
	/*MULTICAST CONTROL SOCKET*/
	public static MulticastSocket mc_socket;
	public static int mc_port;
	public static String mc_addr;
	public static InetSocketAddress mc_saddr;
	/*MULTICAST DATA BACKUP SOCKET*/
	public static MulticastSocket mdb_socket;
	public static int mdb_port;
	public static String mdb_addr;
	public static InetSocketAddress mdb_saddr;
	/*MULTICAST DATA RESTORE SOCKET*/
	public static MulticastSocket mdr_socket;
	public static int mdr_port;
	public static String mdr_addr;
	public static InetSocketAddress mdr_saddr;
	
	private final static String backupPath = "backup";
	
}
