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


@Component(immediate = true)
public class GetFlow {
    List<FlowRule> flowRulesList = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(GetFlow.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    private DeviceId deviceId = DeviceId.deviceId("device:s1");
    private ScheduledExecutorService scheduledExecutor;

    private double packetRateThreshold = 100.0;
    private double byteRateThreshold = 10000.0;
    private double flowDurationThreshold = 60.0;
    private double flowSizeThreshold = 10000.0;
    

    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.get-flow");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new SendActiveFlows(deviceId), 0, 5, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        log.info("Get-flow Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        log.info("Stopped");
    }

    private class SendActiveFlows implements Runnable {
        private DeviceId deviceId;

        public SendActiveFlows(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            getFlowsStatsAndSendToServer();
        }
    }

    private void getFlowsStatsAndSendToServer() {
        Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);
    
        for (FlowEntry entry : flowEntries) {
            if (entry.table().toString().equals("IngressPipeImpl.gtp_tunnel") && entry.treatment().toString().contains("IngressPipeImpl.track_tunnel")) {

                List<String> flowMessages = new ArrayList<>();
                String message = "Flow ID: " + entry.id() +
                        ", Packet Rate: " + packetRate +
                        ", Byte Rate: " + byteRate +
                        ", Flow Duration: " + flowDuration +
                        ", Flow Size: " + flowSize +
                        ", Flow Treatment: " + entry.treatment().toString();
                flowMessages.add(message);

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonMessages;
                try {
                    jsonMessages = objectMapper.writeValueAsString(flowMessages);
                } catch (IOException e) {
                    log.error("Error converting flow messages to JSON", e);
                    return;
                }

                if (!flowMessages.isEmpty()) {
                    sendFlowMessagesToServer(jsonMessages, entry);
                }


            }
        }
    }
    

    private double calculatePacketRate(FlowEntry entry) {
        long packets = entry.packets();
        long lifetimeInSeconds = entry.life();
        if (lifetimeInSeconds > 0) {
            return (double) packets / lifetimeInSeconds;
        } else {
            return 0.0;
        }
    }

    private double calculateByteRate(FlowEntry entry) {
        long bytes = entry.bytes();
        long lifetimeInSeconds = entry.life();
        if (lifetimeInSeconds > 0) {
            return (double) bytes / lifetimeInSeconds;
        } else {
            return 0.0;
        }
    }

    private double calculateFlowDuration(FlowEntry entry) {
        long lifetimeInSeconds = entry.life();
        return (double) lifetimeInSeconds;
    }

    private double calculateFlowSize(FlowEntry entry) {
        long bytes = entry.bytes();
        return (double) bytes;
    }

    private void sendFlowMessagesToServer(String jsonMessages, FlowEntry flowEntry) {
        try {
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
                dropGTPTunnel(flowEntry);

            } else {
                // Handle acknowledgment failure
                log.error("Received unexpected response from the server");
            }
    
            socket.close();
        } catch (IOException e) {
            log.error("Error sending flow messages to the server", e);
        }
    }

    private void dropGTPTunnel(FlowEntry flowEntry) {

       

        log.info("Hi Diana\nI'm about to delete the flow rule: {}", flowEntry);
        TrafficSelector selector = flowEntry.selector();
        String selectorString = selector.toString();
    
        // Define a pattern to match the tunnel ID
        Pattern pattern = Pattern.compile("hdr\\.gtp\\.teid=0x([0-9a-fA-F]+)");
        Matcher matcher = pattern.matcher(selectorString);
    
        if (matcher.find()) {
            // Extract the tunnel ID
            String tunnelIdHex = matcher.group(1);
            log.info("Tunnel ID (Hex): {}", tunnelIdHex);
    
            // Parse the tunnel ID
            try {
                long tunnelId = Long.parseLong(tunnelIdHex, 16);
                log.info("Tunnel ID (Decimal): {}", tunnelId);
                
                // Create a byte array from the tunnel ID
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(tunnelId);
                byte[] tunnelIDBytes = buffer.array();
                log.info("Tunnel ID (Bytes): {}", tunnelIDBytes);
    
                // Build the gtpTunnelCriterion
                final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
                    .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), tunnelIDBytes)
                    .build();
                log.info("gtpTunnelCriterion: {}", gtpTunnelCriterion);
    
                // Create a PiAction to drop
                final PiAction dropAction = PiAction.builder()
                        .withId(PiActionId.of("IngressPipeImpl.drop"))
                        .build();
                // Build the FlowRule with the specified index
                final FlowRule rule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", gtpTunnelCriterion, dropAction);
                // Insert the FlowRule
                log.info("FlowRule: {}", rule);
                flowRuleService.applyFlowRules(rule); 
    
            } catch (NumberFormatException e) {
                log.error("Failed to parse tunnel ID hex string: {}", tunnelIdHex, e);
            }
        }
    }
    
}
