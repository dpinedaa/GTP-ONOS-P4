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
public class Detection {   
    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();


    private static final Logger log = LoggerFactory.getLogger(Detection.class);
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
        appId = coreService.registerApplication("edu.fiu.adwise.detection");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new DetectAtack(deviceId), 0, 5, TimeUnit.SECONDS);
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


    private class DetectAtack implements Runnable {
        private DeviceId deviceId;

        public DetectAtack(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            evaluateExistingFlows();
        }

    }
    
    private void evaluateExistingFlows() {

        for (int i = 0; i < unidirectionalFlowList.size(); i++) {
            UnidirectionalFlow unidirectionalFlow = unidirectionalFlowList.get(i);




            // Construct flowString
            //unidirectionalFlow.getForwardFlow().getDuration() + " " +

            //Add the unidirectional flow index to the flowString


            String flowString = i + " " + 
                                unidirectionalFlow.getForwardFlow().getInnerIpv4Protocol() + " " +
                                unidirectionalFlow.getForwardFlow().getCurrentDurationMicroseconds() + " " +
                                unidirectionalFlow.getForwardFlow().getCurrentPacketCount() + " " +
                                unidirectionalFlow.getBackwardFlow().getCurrentPacketCount() + " " +
                                unidirectionalFlow.getForwardFlow().getCurrentBytesCount() + " " +
                                unidirectionalFlow.getBackwardFlow().getCurrentBytesCount() + " " +
                                unidirectionalFlow.getFlowBytesPerSecond() + " " +
                                unidirectionalFlow.getBwdPacketsPerSecond() + " " +
                                unidirectionalFlow.getBwdPacketLengthMax() + " " +
                                unidirectionalFlow.getBwdPacketLengthMin();





            // Send only the results to the server
            sendFlowMessagesToServer(flowString);
        }
    }

    private void sendFlowMessagesToServer(String flowString) {
        try {
            // Convert the results to a JSON format
            String jsonResults = "{ \"flowString\": \"" + flowString + "\" }";

            Socket socket = new Socket("10.102.211.38", 4500);
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
