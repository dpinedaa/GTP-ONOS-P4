package edu.fiu.adwise.p4_gtp;
public class UnidirectionalFlow {
	private FlowDetails forwardFlow;
    private FlowDetails backwardFlow;
    private double FlowBytesPerSecond;
    private double BwdPacketsPerSecond;
    
        
	public UnidirectionalFlow(FlowDetails forwardFlow, FlowDetails backwardFlow, double flowBytesPerSecond,
		double bwdPacketsPerSecond) {
		this.forwardFlow = forwardFlow;
		this.backwardFlow = backwardFlow;
		FlowBytesPerSecond = flowBytesPerSecond;
		BwdPacketsPerSecond = bwdPacketsPerSecond;
	}
	public FlowDetails getForwardFlow() {
		return forwardFlow;
	}
	public void setForwardFlow(FlowDetails forwardFlow) {
		this.forwardFlow = forwardFlow;
	}
	public FlowDetails getBackwardFlow() {
		return backwardFlow;
	}
	public void setBackwardFlow(FlowDetails backwardFlow) {
		this.backwardFlow = backwardFlow;
	}
	public double getFlowBytesPerSecond() {
		return FlowBytesPerSecond;
	}
	public void setFlowBytesPerSecond(double flowBytesPerSecond) {
		FlowBytesPerSecond = flowBytesPerSecond;
	}
	public double getBwdPacketsPerSecond() {
		return BwdPacketsPerSecond;
	}
	public void setBwdPacketsPerSecond(double bwdPacketsPerSecond) {
		BwdPacketsPerSecond = bwdPacketsPerSecond;
	}
    
        
}
