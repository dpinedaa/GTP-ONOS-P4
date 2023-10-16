package co.edu.udea.gita.tutorial;
import org.onlab.packet.Ethernet;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.IPv4;
import org.onlab.packet.UDP;

import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;

import static co.edu.udea.gita.tutorial.AppConstants.PIPECONF_ID;
import org.onosproject.net.pi.model.PiPipelineModel;
import org.onosproject.net.pi.model.DefaultPiPipeconf;
import org.onosproject.net.pi.service.PiPipeconfService;
import org.onosproject.net.pi.model.PiPipeconf;
import org.onosproject.net.pi.model.PiCounterModel;
import org.onosproject.net.pi.model.PiCounterId;
import org.onosproject.net.pi.model.PiCounterType;
import org.onosproject.net.pi.runtime.PiCounterCellData;
import org.onosproject.net.pi.runtime.PiCounterCellId;
import org.onosproject.net.pi.runtime.PiCounterCell;

import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.TableId;
import org.onosproject.net.flow.FlowRuleListener;
import org.onosproject.net.flow.FlowRuleEvent;

import co.edu.udea.gita.tutorial.common.Utils;
import com.google.common.collect.Lists;
import java.util.Collection;

import java.nio.ByteBuffer;
import java.util.Arrays; // Import the Arrays class
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.TimerTask;
import java.util.Timer;
import co.edu.udea.gita.tutorial.FlowInfo;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.PiCriterion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(immediate = true)
public class FlowInfoList {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService pipeconfService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private PacketInLogger packetInLogger;

    List<FlowInfo> flowInfoList = packetInLogger.getFlowInfoList();
    private DeviceId deviceId = DeviceId.deviceId("device:s1");

    private final FlowRuleListener flowListener = new InternalFlowListener();




    
    @Activate
    public void activate() {
        log.info("FlowInfoList Started");
        flowRuleService.addListener(flowListener);
    }

    @Deactivate
    public void deactivate() {
        
    }

    private class UpdateFlowInfoListTask extends TimerTask {
        private final DeviceId deviceId;
        public UpdateFlowInfoListTask(DeviceId deviceId) {
            this.deviceId = deviceId;
        }
        @Override
        public void run() {
            // Call the printActiveFlows method here
            UpdateFlowInfoList();
        }
    }

    private void UpdateFlowInfoList(){
        List<FlowInfo> flowInfoList = packetInLogger.getFlowInfoList();
        log.info("\nDIANA!!!!!!\nFlow Info List: {}\n\n", flowInfoList);
        for (FlowInfo flowInfo : flowInfoList) {
            if (flowInfo.getFlowEntry() == null) {
                log.info("\nDIANA!!!!!!\nFlow Entry: {}\n\n", flowInfo.getFlowEntry());
                TrafficSelector flowRuleSelector = flowInfo.getFlowRule().selector();
                log.info("\nDIANA!!!!!!\nFlow Rule Selector: {}\n\n", flowRuleSelector);

                // Assuming you have a list of FlowEntry instances
                Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);
                for (FlowEntry flowEntry : flowEntries) {
                    TrafficSelector flowEntrySelector = flowEntry.selector();
                    log.info("\nDIANA!!!!!!\nFlow Entry Selector: {}\n\n", flowEntrySelector);

                    // Check if the FlowRule's selector matches the FlowEntry's selector
                    if (flowRuleSelector.equals(flowEntrySelector)) {
                        log.info("\nDIANA!!!!!!!\nFlow Rule Selector: {} matches Flow Entry Selector: {}\n\n", flowRuleSelector, flowEntrySelector);

                        // Update the FlowInfo with the associated FlowEntry
                        flowInfo.setFlowEntry(flowEntry);
                        log.info("Associated Flow Rule: {} with Flow Entry: {}", flowInfo.getFlowRule(), flowInfo.getFlowEntry());
                        break;
                    }
                }
            }
        }

    }
}
