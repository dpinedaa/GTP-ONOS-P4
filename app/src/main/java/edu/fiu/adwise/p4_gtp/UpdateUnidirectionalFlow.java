package edu.fiu.adwise.p4_gtp;
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
public class UpdateUnidirectionalFlow {   
    public static List<UnidirectionalFlow> unidirectionalFlowList = new ArrayList<UnidirectionalFlow>();


    private static final Logger log = LoggerFactory.getLogger(UpdateUnidirectionalFlow.class);
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
        appId = coreService.registerApplication("edu.fiu.adwise.update-unidirectional-flow");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new UpdateUnidirectionalFlows(deviceId), 0, 5, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        log.info("Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        log.info("Stopped");
    }

    public static List<UnidirectionalFlow> getUnidirectionalFlowList() {
        return unidirectionalFlowList;
    }

    private class UpdateUnidirectionalFlows implements Runnable {
        private DeviceId deviceId;

        public UpdateUnidirectionalFlows(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            updateExistingFlows();
        }

    }

    private void updateExistingFlows() {
        Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);

        for (UnidirectionalFlow unidirectionalFlow : unidirectionalFlowList) {
            // Assuming that the flow ID is stored in the FlowDetails of the UnidirectionalFlow
            FlowDetails forwardFlowDetails = unidirectionalFlow.getForwardFlow();
            FlowDetails backwardFlowDetails = unidirectionalFlow.getBackwardFlow();

            updateFlowDetails(forwardFlowDetails, flowEntries);
            updateFlowDetails(backwardFlowDetails, flowEntries);
        
            sendFlowMessagesToServer(unidirectionalFlow);
        }
    }

    private void updateFlowDetails(FlowDetails flowDetails, Iterable<FlowEntry> flowEntries) {
        // Find the corresponding FlowEntry in flowEntries by matching flow ID
        Optional<FlowEntry> matchingFlowEntry = StreamSupport.stream(flowEntries.spliterator(), false)
                .filter(entry -> entry.id().toString().equals(flowDetails.getFlowId()))
                .findFirst();

            // Update the existing flow details if a matching FlowEntry is found
            matchingFlowEntry.ifPresent(entry -> {
            // Update packet count, bytes, and duration/life in FlowDetails

            flowDetails.setPastBytesCount(flowDetails.getCurrentBytesCount());
            flowDetails.setPastPacketCount(flowDetails.getCurrentPacketCount());
            flowDetails.setPastDurationSeconds(flowDetails.getCurrentDurationSeconds());
            flowDetails.setPastDurationMicroseconds(flowDetails.getCurrentDurationMicroseconds());

            flowDetails.setCurrentBytesCount(entry.bytes());
            flowDetails.setCurrentPacketCount(entry.packets());
            // Convert seconds to microseconds and update the duration
            flowDetails.setCurrentDurationSeconds(entry.life());
            flowDetails.setCurrentDurationMicroseconds(entry.life() * 1000000);


        });
    }
  
    private void sendFlowMessagesToServer(UnidirectionalFlow unidirectionalFlow) {
        try {
            // Convert UnidirectionalFlow object to JSON format
            String jsonMessages = convertUnidirectionalFlowToJson(unidirectionalFlow);

            Socket socket = new Socket("10.102.211.11", 6000);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(jsonMessages.getBytes());

            // Receive a response from the server (in this case, "1")
            InputStream inputStream = socket.getInputStream();
            byte[] responseBuffer = new byte[1];
            int bytesRead = inputStream.read(responseBuffer);

            socket.close();
        } catch (IOException e) {
            log.error("Error sending flow messages to the server", e);
        }
    }

    // Assuming you have a method to convert UnidirectionalFlow to JSON
    private String convertUnidirectionalFlowToJson(UnidirectionalFlow unidirectionalFlow) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(unidirectionalFlow);
        } catch (JsonProcessingException e) {
            log.error("Error converting UnidirectionalFlow to JSON", e);
            return "{}"; // Return empty JSON as a fallback
        }
    }

    
    
}
