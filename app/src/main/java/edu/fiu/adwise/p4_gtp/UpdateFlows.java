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
public class UpdateFlows {

    

    private static final Logger log = LoggerFactory.getLogger(UpdateFlows.class);
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
        appId = coreService.registerApplication("edu.fiu.adwise.update-flows");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new UpdateActiveFlows(deviceId), 0, 5, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        log.info("Update-flow Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        log.info("Stopped");
    }

    private class UpdateActiveFlows implements Runnable {
        private DeviceId deviceId;

        public UpdateActiveFlows(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            List<FlowDetails> flowDetailsList = GetFlow.getFlowDetailsList();
            updateExistingFlows(flowDetailsList);
        }

    }

    private void updateExistingFlows(List<FlowDetails> flowDetailsList) {
        Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);

        for (FlowDetails flowDetails : flowDetailsList) {
            // Find the corresponding FlowEntry in flowEntries by matching flow ID
            Optional<FlowEntry> matchingFlowEntry = StreamSupport.stream(flowEntries.spliterator(), false)
                    .filter(entry -> entry.id().toString().equals(flowDetails.getFlowId()))
                    .findFirst();

            // Update the existing flow details if a matching FlowEntry is found
            matchingFlowEntry.ifPresent(entry -> {
                // Update packet count, bytes, and duration/life in FlowDetails
                flowDetails.setPacketCount(entry.packets());
                flowDetails.setBytesCount(entry.bytes());
                flowDetails.setDuration(entry.life());

                // You can also call the method to send the updated flow details to the server if needed
                //sendFlowMessagesToServer(flowDetails);
            });
        }
    }

   
    

    private void sendFlowMessagesToServer(FlowDetails flowDetails) {
        try {
            // Convert FlowDetails object to JSON format (assuming you have a method for this)
            String jsonMessages = convertFlowDetailsToJson(flowDetails);
    
            Socket socket = new Socket("10.102.211.38", 7000);
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
    
    // Assuming you have a method to convert FlowDetails to JSON
    private String convertFlowDetailsToJson(FlowDetails flowDetails) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(flowDetails);
        } catch (JsonProcessingException e) {
            log.error("Error converting FlowDetails to JSON", e);
            return "{}"; // Return empty JSON as a fallback
        }
    }
    
    
}
