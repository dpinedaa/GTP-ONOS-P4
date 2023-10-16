package co.edu.udea.gita.tutorial;

import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;

public class FlowInfo {
    private FlowRule flowRule;
    private FlowEntry flowEntry;
    

    public FlowInfo() {
        // Default constructor
    }

    public FlowInfo(FlowRule flowRule, FlowEntry flowEntry) {
        this.flowRule = flowRule;
        this.flowEntry = flowEntry;
    }

    public FlowRule getFlowRule() {
        return flowRule;
    }

    public void setFlowRule(FlowRule flowRule) {
        this.flowRule = flowRule;
    }

    public FlowEntry getFlowEntry() {
        return flowEntry;
    }

    public void setFlowEntry(FlowEntry flowEntry) {
        this.flowEntry = flowEntry;
    }
}

