package Files;


public class ChunkInfo{

    private Integer chunkNo;
    private String fileID;
    private Integer desiredRD;
    private Integer actualRD;
	
    public ChunkInfo(String fileID, Integer chunkNo, Integer desiredRD, Integer actualRD) {
        this.fileID = fileID;
        this.desiredRD = desiredRD;
        this.actualRD = actualRD;
        this.chunkNo = chunkNo;
    }

    public Integer getExcessDegree() {
        return actualRD - desiredRD;
    }

    public String getFileId() {
        return fileID;
    }

    public Integer getChunkNo() {
        return chunkNo;
    }

    public Integer getDesiredRD() {
        return desiredRD;
    }
    
    public Integer getActualRD(){
    	return this.actualRD;
    }
    
    public void setActualRD(Integer i){
    	this.actualRD = i;
    }
    
    @Override
    public boolean equals(Object other) {
    	if (other instanceof ChunkInfo) {
    		ChunkInfo otherChunk = (ChunkInfo) other;
    		return (this.fileID.equals(otherChunk.fileID) && this.chunkNo == otherChunk.chunkNo && this.desiredRD == otherChunk.desiredRD && this.actualRD == otherChunk.actualRD);
    	}

    	return false;
    }
   
}