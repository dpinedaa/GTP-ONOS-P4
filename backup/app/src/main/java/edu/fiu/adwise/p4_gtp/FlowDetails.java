package edu.fiu.adwise.p4_gtp;
public class FlowDetails {
	private String flowId;
    private String srcInnerIpv4;
    private String dstInnerIpv4;
    private int innerIpv4Protocol;
    private int innerSrcPort;
    private int innerDstPort;
    private long packetCount;
    private long bytesCount;
    private long duration;
   
	public FlowDetails(String flowId, String srcInnerIpv4, String dstInnerIpv4, int innerIpv4Protocol,
			int innerSrcPort, int innerDstPort, long packetCount, long bytesCount, long duration) {
		this.flowId = flowId;
		this.srcInnerIpv4 = srcInnerIpv4;
		this.dstInnerIpv4 = dstInnerIpv4;
		this.innerIpv4Protocol = innerIpv4Protocol;
		this.innerSrcPort = innerSrcPort;
		this.innerDstPort = innerDstPort;
		this.packetCount = packetCount;
		this.bytesCount = bytesCount;
		this.duration = duration;			
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
	public int getSrcPort() {
		return innerSrcPort;
	}
	public void setSrcPort(int innerSrcPort) {
		this.innerSrcPort = innerSrcPort;
	}
	public int getDstPort() {
		return innerDstPort;
	}
	public void setDstPort(int innerDstPort) {
		this.innerDstPort = innerDstPort;
	}
	public long getPacketCount() {
		return packetCount;
	}
	public void setPacketCount(long packetCount) {
		this.packetCount = packetCount;
	}
	public long getBytesCount() {
		return bytesCount;
	}
	public void setBytesCount(long bytesCount) {
		this.bytesCount = bytesCount;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}



}
