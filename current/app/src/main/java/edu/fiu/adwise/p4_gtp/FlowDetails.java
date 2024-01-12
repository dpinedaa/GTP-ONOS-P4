package edu.fiu.adwise.p4_gtp;
public class FlowDetails {
	private String flowId;
    private String srcInnerIpv4;
    private String dstInnerIpv4;
    private int innerIpv4Protocol;
    private long currentPacketCount;
    private long currentBytesCount;
    private long currentDurationSeconds;
    private long currentDurationMicroseconds;
    private long pastPacketCount;
    private long pastBytesCount;
    private long pastDurationSeconds;
    private long pastDurationMicroseconds;
    
      
    
    
	public FlowDetails(String flowId, String srcInnerIpv4, String dstInnerIpv4, int innerIpv4Protocol,
			long currentPacketCount, long currentBytesCount, long currentDurationSeconds,
			long currentDurationMicroseconds, long pastPacketCount, long pastBytesCount, long pastDurationSeconds,
			long pastDurationMicroseconds) {
		this.flowId = flowId;
		this.srcInnerIpv4 = srcInnerIpv4;
		this.dstInnerIpv4 = dstInnerIpv4;
		this.innerIpv4Protocol = innerIpv4Protocol;
		this.currentPacketCount = currentPacketCount;
		this.currentBytesCount = currentBytesCount;
		this.currentDurationSeconds = currentDurationSeconds;
		this.currentDurationMicroseconds = currentDurationMicroseconds;
		this.pastPacketCount = pastPacketCount;
		this.pastBytesCount = pastBytesCount;
		this.pastDurationSeconds = pastDurationSeconds;
		this.pastDurationMicroseconds = pastDurationMicroseconds;
	}
	public String getFlowId() {
		return flowId;
	}
	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}
	public String getSrcInnerIpv4() {
		return srcInnerIpv4;
	}
	public void setSrcInnerIpv4(String srcInnerIpv4) {
		this.srcInnerIpv4 = srcInnerIpv4;
	}
	public String getDstInnerIpv4() {
		return dstInnerIpv4;
	}
	public void setDstInnerIpv4(String dstInnerIpv4) {
		this.dstInnerIpv4 = dstInnerIpv4;
	}
	public int getInnerIpv4Protocol() {
		return innerIpv4Protocol;
	}
	public void setInnerIpv4Protocol(int innerIpv4Protocol) {
		this.innerIpv4Protocol = innerIpv4Protocol;
	}
	public long getCurrentPacketCount() {
		return currentPacketCount;
	}
	public void setCurrentPacketCount(long currentPacketCount) {
		this.currentPacketCount = currentPacketCount;
	}
	public long getCurrentBytesCount() {
		return currentBytesCount;
	}
	public void setCurrentBytesCount(long currentBytesCount) {
		this.currentBytesCount = currentBytesCount;
	}
	public long getCurrentDurationSeconds() {
		return currentDurationSeconds;
	}
	public void setCurrentDurationSeconds(long currentDurationSeconds) {
		this.currentDurationSeconds = currentDurationSeconds;
	}
	public long getCurrentDurationMicroseconds() {
		return currentDurationMicroseconds;
	}
	public void setCurrentDurationMicroseconds(long currentDurationMicroseconds) {
		this.currentDurationMicroseconds = currentDurationMicroseconds;
	}
	public long getPastPacketCount() {
		return pastPacketCount;
	}
	public void setPastPacketCount(long pastPacketCount) {
		this.pastPacketCount = pastPacketCount;
	}
	public long getPastBytesCount() {
		return pastBytesCount;
	}
	public void setPastBytesCount(long pastBytesCount) {
		this.pastBytesCount = pastBytesCount;
	}
	public long getPastDurationSeconds() {
		return pastDurationSeconds;
	}
	public void setPastDurationSeconds(long pastDurationSeconds) {
		this.pastDurationSeconds = pastDurationSeconds;
	}
	public long getPastDurationMicroseconds() {
		return pastDurationMicroseconds;
	}
	public void setPastDurationMicroseconds(long pastDurationMicroseconds) {
		this.pastDurationMicroseconds = pastDurationMicroseconds;
	}
    
    
    
    
	
}
