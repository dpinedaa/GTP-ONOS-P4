
public class FlowDetails {
    

	private String flowId;
    private String gtpTunnelId;
    private String srcInnerIpv4;
    private String dstInnerIpv4;
    private String innerIpv4Protocol;
    private String innerSrcPort;
    private String innerDstPort;
    private long packetCount;
    private long bytesCount;
    private long duration;
    

    public FlowDetails(String flowId, String gtpTunnelId, String srcInnerIpv4, String dstInnerIpv4,
			String innerIpv4Protocol, String innerSrcPort, String innerDstPort, long packetCount, long bytesCount,
			long duration) {
		this.flowId = flowId;
		this.gtpTunnelId = gtpTunnelId;
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
	public String getGtpTunnelId() {
		return gtpTunnelId;
	}
	public void setGtpTunnelId(String gtpTunnelId) {
		this.gtpTunnelId = gtpTunnelId;
	}
	public String getSrcInnerIpv4() {
		return srcInnerIpv4;
	}
	public void setSrcInnerIpv4(String srcInnerIpv4) {
		this.srcInnerIpv4 = srcInnerIpv4;
	}
	public String getInnerIpv4Protocol() {
		return innerIpv4Protocol;
	}
	public void setInnerIpv4Protocol(String innerIpv4Protocol) {
		this.innerIpv4Protocol = innerIpv4Protocol;
	}
	public String getSrcPort() {
		return innerSrcPort;
	}
	public void setSrcPort(String innerSrcPort) {
		this.innerSrcPort = innerSrcPort;
	}
	public String getDstPort() {
		return innerDstPort;
	}
	public void setDstPort(String innerDstPort) {
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

	public String getDstInnerIpv4() {
		return dstInnerIpv4;
	}

	public void setDstInnerIpv4(String dstInnerIpv4) {
		this.dstInnerIpv4 = dstInnerIpv4;
	}

}
