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



@Component(immediate = true)
public class GetFlow {
    public static List<FlowDetails> flowDetailsList = new ArrayList<>();

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
        flowDetailsList.clear();
        log.info("Stopped");
    }


    public static List<FlowDetails> getFlowDetailsList() {
        return flowDetailsList;
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
    
        // Initialize flow entry count
        int flowEntryCount = 0;
    
        for (FlowEntry entry : flowEntries) {
            if (entry.table().toString().equals("IngressPipeImpl.gtp_tunnel") && entry.treatment().toString().contains("IngressPipeImpl.track_tunnel")) {

                if (!flowDetailsList.stream().anyMatch(flowDetails -> flowDetails.getFlowId().equals(entry.id().toString()))) {
                    // Create FlowDetails object and add to the list
                    String gtpTunnelId = extractValue(entry.selector().toString(), "hdr.gtp.teid=(\\S+)");
                    String srcIpAddress = convertHexToIPv4(extractValue(entry.selector().toString(), "hdr.inner_ipv4.src_addr=(\\S+)"));
                    log.info("srcIpAddress: " + srcIpAddress);
                    String dstIpAddress = convertHexToIPv4(extractValue(entry.selector().toString(), "hdr.inner_ipv4.dst_addr=(\\S+)"));
                    log.info("dstIpAddress: " + dstIpAddress);
                    String protocol = String.valueOf(Integer.parseInt(extractValue(entry.selector().toString(), "hdr.inner_ipv4.protocol=(\\S+)").substring(2), 16));
                    String srcPort = convertHexToDecimal(extractValue(entry.selector().toString(), "hdr.inner_udp.srcPort=(\\S+)"));
                    String dstPort = convertHexToDecimal(extractValue(entry.selector().toString(), "hdr.inner_udp.dstPort=(\\S+)"));
                    /* String srcPortHex = extractValue(entry.selector().toString(), "hdr.inner_udp.srcPort=(\\S+)");
                    String dstPortHex = extractValue(entry.selector().toString(), "hdr.inner_udp.dstPort=(\\S+)");

                    // Remove the "0x" prefix and convert to decimal
                    String srcPortDec = String.valueOf(Integer.parseInt(srcPortHex.substring(2), 16));
                    String dstPortDec = String.valueOf(Integer.parseInt(dstPortHex.substring(2), 16));
                    lo
 */
                    
                    FlowDetails flowDetails = new FlowDetails(
                            entry.id().toString(),
                            gtpTunnelId,
                            srcIpAddress,
                            dstIpAddress,
                            protocol,
                            srcPort,
                            dstPort,
                            entry.packets(),
                            entry.bytes(),
                            entry.life()
                    );
                    flowDetailsList.add(flowDetails);
                    sendFlowMessagesToServer(flowDetails);

                    //sendFlowMessagesToServer(jsonMessages, entry);
                }
            }
        }
    
        // Print or use flow entry count as needed
        log.info("Total Flow Entries: " + flowEntryCount);
    }

    public String extractValue(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            // Get the matched group (index 1) and remove commas, square brackets, and curly braces
            String extractedValue = matcher.group(1).replaceAll(",", "")
                                                      .replaceAll("\\]", "")
                                                      .replaceAll("\\}", "");
            return extractedValue;
        }
        return null;
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


    public static String convertHexToIPv4(String hexValue) {
        try {
            // Parse the hexadecimal string and convert it to decimal
            long decimalValue = Long.parseLong(hexValue.substring(2), 16);

            // Extract octets from the decimal value
            int octet1 = (int) ((decimalValue >> 24) & 0xFF);
            int octet2 = (int) ((decimalValue >> 16) & 0xFF);
            int octet3 = (int) ((decimalValue >> 8) & 0xFF);
            int octet4 = (int) (decimalValue & 0xFF);

            // Construct the IPv4 address string
            String ipv4Address = octet1 + "." + octet2 + "." + octet3 + "." + octet4;
            return ipv4Address;
        } catch (NumberFormatException e) {
            // Handle exception if conversion fails
            return null;
        }
    }


    // Add the convertHexToDecimal method here (if you haven't added it already)
    private String convertHexToDecimal(String hexValue) {
        try {
            // Check if the input string is valid
            if (hexValue == null || hexValue.length() < 3 || !hexValue.startsWith("0x")) {
                log.error("Error: Invalid hexadecimal input");
                return null;
            }

            // Convert hexadecimal to decimal and store as string
            return Integer.toString(Integer.parseInt(hexValue.substring(2), 16));
        } catch (NumberFormatException e) {
            log.error("Error: Unable to convert hex to decimal - " + e.getMessage());
            return null;
        }
    }
    
    
}
