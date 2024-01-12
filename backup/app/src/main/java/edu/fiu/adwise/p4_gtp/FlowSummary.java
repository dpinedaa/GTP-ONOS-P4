import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.TableId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.ByteBuffer;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.model.PiActionId;
import com.google.common.collect.Lists;
import edu.fiu.adwise.p4_gtp.common.Utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.List;



@Component(immediate = true)
public class FlowSummary {

    
    private Map<String, FlowDetails> summaryMap;
    private static final Logger log = LoggerFactory.getLogger(FlowSummary.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    private DeviceId deviceId = DeviceId.deviceId("device:s1");
    private ScheduledExecutorService scheduledExecutor;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.flow-summary");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new CreateFlowSummary(deviceId), 0, 5, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        log.info("Flow Summary Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        log.info("Stopped");
    }

    private class CreateFlowSummary implements Runnable {
        private DeviceId deviceId;

        public CreateFlowSummary(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            List<FlowDetails> flowDetailsList = GetFlow.getFlowDetailsList();
            GenerateFlowSummary(flowDetailsList);
        }

    }
    private void GenerateFlowSummary(List<FlowDetails> flowDetailsList) {
        Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);
    
        // Create a map to hold the source IP to flow details mapping
        Map<String, List<FlowDetails>> srcIpToFlows = new HashMap<>();
    
        // Iterate over the flow details list and populate the map
        for (FlowDetails flow : flowDetailsList) {
            String srcIp = flow.getSrcInnerIpv4();
    
            // If the map does not contain the source IP as a key, add it
            if (!srcIpToFlows.containsKey(srcIp)) {
                srcIpToFlows.put(srcIp, new ArrayList<>());
            }
    
            // Add the flow details to the list associated with the source IP
            srcIpToFlows.get(srcIp).add(flow);
        }
    
        // Now you can use the map 'srcIpToFlows' as needed
        // For example, you can print the map to the console
        for (Map.Entry<String, List<FlowDetails>> entry : srcIpToFlows.entrySet()) {
            
    
            // Send the entire entry (source IP and associated flows) to the server
            sendFlowMessagesToServer(entry.getKey(), entry.getValue());
        }
    }
    
    // Modified sendFlowMessagesToServer method to handle the map entry
    private void sendFlowMessagesToServer(String srcIp, List<FlowDetails> flowDetailsList) {
        try {
            // Convert the map entry to JSON format (assuming you have a method for this)
            String jsonMessages = convertFlowSummaryToJson(srcIp, flowDetailsList);
    
            Socket socket = new Socket("10.102.211.11", 3000);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(jsonMessages.getBytes());
    
            // Receive a response from the server (in this case, "1")
            InputStream inputStream = socket.getInputStream();
            byte[] responseBuffer = new byte[1];
            int bytesRead = inputStream.read(responseBuffer);
    
            if (bytesRead == 1 && responseBuffer[0] == '1') {
                // Successfully received acknowledgment from the server
                log.info("Received acknowledgment from the server");
            } else {
                // Handle acknowledgment failure
                log.error("Received unexpected response from the server");
            }
    
            socket.close();
        } catch (IOException e) {
            log.error("Error sending flow messages to the server", e);
        }
    }
    
    // Assuming you have a method to convert the map entry to JSON
    private String convertFlowSummaryToJson(String srcIp, List<FlowDetails> flowDetailsList) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Create a map or object that represents the source IP and associated flows
            Map<String, Object> flowSummaryMap = new HashMap<>();
            flowSummaryMap.put("sourceIp", srcIp);
            flowSummaryMap.put("flows", flowDetailsList);
    
            // Convert the map or object to JSON
            return objectMapper.writeValueAsString(flowSummaryMap);
        } catch (JsonProcessingException e) {
            log.error("Error converting FlowSummary to JSON", e);
            return "{}"; // Return empty JSON as a fallback
        }
    }
    
    
    
}
