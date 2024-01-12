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
public class Statistics {   
    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();


    private static final Logger log = LoggerFactory.getLogger(Statistics.class);
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
        appId = coreService.registerApplication("edu.fiu.adwise.calculate-statistics");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new UpdateStatistics(deviceId), 0, 2, TimeUnit.SECONDS);
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


    private class UpdateStatistics implements Runnable {
        private DeviceId deviceId;

        public UpdateStatistics(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            updateExistingFlows();
        }

    }



    private void updateExistingFlows() {
        for (UnidirectionalFlow unidirectionalFlow : unidirectionalFlowList) {
            // Calculate and set FlowBytesPerSecond for both forward and backward flows

            // Calculate and set FlowBytesPerSecond for the forward flow
            long totalBytes = unidirectionalFlow.getForwardFlow().getCurrentBytesCount() + unidirectionalFlow.getBackwardFlow().getCurrentBytesCount(); 
            double flowBytesPerSecond = (double) totalBytes / unidirectionalFlow.getForwardFlow().getCurrentDurationSeconds();  

            unidirectionalFlow.setFlowBytesPerSecond(flowBytesPerSecond);

            // Calculate and set BwdPacketsPerSecond for the backward flow
            long backwardPackets = unidirectionalFlow.getBackwardFlow().getCurrentPacketCount();
            double bwdPacketsPerSecond = (double) backwardPackets / unidirectionalFlow.getBackwardFlow().getCurrentDurationSeconds();
            unidirectionalFlow.setBwdPacketsPerSecond(bwdPacketsPerSecond);

            // Calculate the difference in packet count
            int packetCountDifference = (int) (unidirectionalFlow.getBackwardFlow().getCurrentPacketCount() - unidirectionalFlow.getBackwardFlow().getPastPacketCount());
            
            // Check if there is at least one new packet
            if (packetCountDifference > 0) {
                // Calculate the average packet length
                int avgPacketLength = (int) ((unidirectionalFlow.getBackwardFlow().getCurrentBytesCount() - unidirectionalFlow.getBackwardFlow().getPastBytesCount()) / packetCountDifference);
            
                // Update the maximum packet length if needed
                if (unidirectionalFlow.getBwdPacketLengthMax() < avgPacketLength) {
                    unidirectionalFlow.setBwdPacketLengthMax(avgPacketLength);
                }
            
                // Update the minimum packet length if needed
                if (unidirectionalFlow.getBwdPacketLengthMin() > avgPacketLength || unidirectionalFlow.getBwdPacketLengthMin() == 0) {
                    unidirectionalFlow.setBwdPacketLengthMin(avgPacketLength);
                }
            } else if (packetCountDifference == 0 && unidirectionalFlow.getBwdPacketLengthMin() == 0 && unidirectionalFlow.getBwdPacketLengthMax() == 0) {
                // If no new packets, and min and max are both 0, set them based on current packets and bytes
                int currentPacketCount = (int) unidirectionalFlow.getBackwardFlow().getCurrentPacketCount();
                int currentBytesCount = (int) unidirectionalFlow.getBackwardFlow().getCurrentBytesCount();
            
                // Set both min and max to the current values
                unidirectionalFlow.setBwdPacketLengthMin(currentPacketCount);
                unidirectionalFlow.setBwdPacketLengthMax(currentPacketCount);
            }





            // Send only the results to the server
            //sendFlowMessagesToServer(flowBytesPerSecond, bwdPacketsPerSecond);
        }
    }

    private void sendFlowMessagesToServer(double flowBytesPerSecond, double bwdPacketsPerSecond) {
        try {
            // Convert the results to a JSON format
            String jsonResults = "{ \"flowBytesPerSecond\": " + flowBytesPerSecond + ", \"bwdPacketsPerSecond\": " + bwdPacketsPerSecond + " }";

            Socket socket = new Socket("10.102.211.11", 6000);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(jsonResults.getBytes());

            // Receive a response from the server (in this case, "1")
            InputStream inputStream = socket.getInputStream();
            byte[] responseBuffer = new byte[1];
            int bytesRead = inputStream.read(responseBuffer);

            socket.close();
        } catch (IOException e) {
            log.error("Error sending flow messages to the server", e);
        }
    }


  
/*     private void sendFlowMessagesToServer(UnidirectionalFlow unidirectionalFlow) {
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
    } */

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
