package PeerProtocol;

import java.util.ArrayList;

import Files.ChunkInfo;
import Main.Peer;

public class PeerSpaceReclaiming {
	public PeerSpaceReclaiming(){
		Peer.reclaimInProgress = true;
		/*
		 * GET ALL THE CHUNKS WITH EXTRA RD
		 */
		ArrayList<ChunkInfo> list = Peer.getChunksWithHighRD();
	}
}
