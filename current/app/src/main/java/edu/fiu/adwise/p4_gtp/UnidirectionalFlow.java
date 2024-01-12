package edu.fiu.adwise.p4_gtp;
import java.util.LinkedList;
import java.util.Queue;

public class UnidirectionalFlow {
	private FlowDetails forwardFlow;
    private FlowDetails backwardFlow;
    private double FlowBytesPerSecond;
    private double BwdPacketsPerSecond;
    private int BwdPacketLengthMax;
    private int BwdPacketLengthMin;
	private Queue<Integer> predictionQueueLR = new LinkedList<>();
    private double predictionLR;
	private Queue<Integer> predictionQueueRF = new LinkedList<>();
	private double predictionRF;
	private Queue<Integer> predictionQueueNB = new LinkedList<>();
	private double predictionNB;
	private Queue<Integer> predictionQueueKNN = new LinkedList<>();
	private double predictionKNN;

	  
	public UnidirectionalFlow(FlowDetails forwardFlow, FlowDetails backwardFlow, double flowBytesPerSecond,
			double bwdPacketsPerSecond, int bwdPacketLengthMax, int bwdPacketLengthMin) {
		this.forwardFlow = forwardFlow;
		this.backwardFlow = backwardFlow;
		this.FlowBytesPerSecond = flowBytesPerSecond;
		this.BwdPacketsPerSecond = bwdPacketsPerSecond;
		this.BwdPacketLengthMax = bwdPacketLengthMax;
		this.BwdPacketLengthMin = bwdPacketLengthMin;
		this.predictionLR = 0;
		this.predictionRF = 0;
		this.predictionNB = 0;
		this.predictionKNN = 0;
		
		
		for (int i = 0; i < 5; i++) {
			predictionQueueLR.offer(0);
			predictionQueueRF.offer(0);
			predictionQueueNB.offer(0);
			predictionQueueKNN.offer(0);
		}
		
	}

	//LOGISTIC REGRESSION
	public Queue<Integer> getPredictionQueueLR() {
		return predictionQueueLR;
	}

	public void setPredictionQueueLR(int value){
		predictionQueueLR.offer(value);
		predictionQueueLR.poll();
	}

	public double getPredictionLR() {
		return predictionLR;
	}

	public void setPredictionLR(double predictionLR) {

		this.predictionLR = predictionLR;
	}

	//RANDOM FOREST
	public Queue<Integer> getPredictionQueueRF() {
		return predictionQueueRF;
	}

	public void setPredictionQueueRF(int value){
		predictionQueueRF.offer(value);
		predictionQueueRF.poll();
	}

	public double getPredictionRF() {
		return predictionRF;
	}

	public void setPredictionRF(double predictionRF) {
		this.predictionRF = predictionRF;
	}
	//NAIVE BAYES
	public Queue<Integer> getPredictionQueueNB() {
		return predictionQueueNB;
	}

	public void setPredictionQueueNB(int value){
		predictionQueueNB.offer(value);
		predictionQueueNB.poll();
	}

	public double getPredictionNB() {
		return predictionNB;
	}

	public void setPredictionNB(double predictionNB) {
		this.predictionNB = predictionNB;
	}
	//KNEAREST NEIGHBORS 
	public Queue<Integer> getPredictionQueueKNN() {
		return predictionQueueKNN;
	}

	public void setPredictionQueueKNN(int value){
		predictionQueueKNN.offer(value);
		predictionQueueKNN.poll();
	}

	public double getPredictionKNN() {
		return predictionKNN;
	}

	public void setPredictionKNN(double predictionKNN) {
		this.predictionKNN = predictionKNN;
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
	public int getBwdPacketLengthMax() {
		return BwdPacketLengthMax;
	}
	public void setBwdPacketLengthMax(int bwdPacketLengthMax) {
		BwdPacketLengthMax = bwdPacketLengthMax;
	}
	public int getBwdPacketLengthMin() {
		return BwdPacketLengthMin;
	}
	public void setBwdPacketLengthMin(int bwdPacketLengthMin) {
		BwdPacketLengthMin = bwdPacketLengthMin;
	} 
    
    
    
    
}
