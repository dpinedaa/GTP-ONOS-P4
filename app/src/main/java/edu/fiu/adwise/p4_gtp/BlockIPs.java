package edu.fiu.adwise.p4_gtp;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.io.BufferedReader;
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
import java.io.InputStreamReader;
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
import org.json.JSONObject;
import java.util.Iterator;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Queue;


import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.FlowEntry;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;





@Component(immediate = true)
public class BlockIPs { 
    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();
    List<FlowDetails> flowDetailsList = GetFlow.getFlowDetailsList();
    List<FlowRule> flowRuleList = CreateGTPFlows.getFlowRulesList();
    List<String> flaggedIPAddress = FlagAttacks.getFlaggedIPAddress();
    List <String> blockedIPAddress = RemoveFlows.getRemovedIPAddress();
    

    private static final Logger log = LoggerFactory.getLogger(BlockIPs.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    private DeviceId deviceId = DeviceId.deviceId("device:s1");
    private ScheduledExecutorService scheduledExecutor;
    private ScheduledExecutorService executor;



    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.detection");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new BlockIPAddress(deviceId), 0, 2, TimeUnit.SECONDS);
        
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


    private class BlockIPAddress implements Runnable {
        private DeviceId deviceId;

        public BlockIPAddress(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
                BlockFlaggedIPs();
            // BlockFlaggedIPs();
        }

    }
   public void BlockFlaggedIPs() {
    String message = "Hello I'm in the BlockFlaggedIPs() method";
    sendFlowMessagesToServer(message);

    for(int i=0; i<blockedIPAddress.size(); i++){
        message ="I'm in the for loop";        
        String ip = blockedIPAddress.get(i);
            message = "Blocking IP address: " + ip;
            sendFlowMessagesToServer(ip);
            sendFlowMessagesToServer(message);
            addDropFlow(ip);
            addIPtoDB(ip);
            blockedIPAddress.remove(i);
            
        }
    }

    

private void addDropFlow(String srcInnerIpv4){
    // Convert IPv4 address string to bytes
    byte[] ipBytes = ipAddressToBytes(srcInnerIpv4);
    final PiCriterion piCriterion = PiCriterion.builder()
    .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.src_addr"), ipBytes)
    .build();
    final PiAction blockIpv4; 
    blockIpv4 = PiAction.builder()
    .withId(PiActionId.of("IngressPipeImpl.drop"))
    .build();

    
    final FlowRule blockedIp = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.drop_inner_ipv4", piCriterion, blockIpv4);    
    flowRuleService.applyFlowRules(blockedIp);

}

private void addIPtoDB(String ip){
    String message1String = "Adding IP address to the database: " + ip;
    sendFlowMessagesToServer(message1String);

    try {
        // Convert the results to a JSON format
        String jsonData = "{ \"_id\": \"" + ip + "\", \"srcIP\": \"" + ip + "\" }";
        String postUrl = "http://10.102.211.11:3001/ip";
        URL url = new URL(postUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                log.info("Flow data posted successfully.");
            } else {
                log.info("Failed to post flow data. HTTP Response Code: " + responseCode);
                // Read the error message from the server
                InputStream errorStream = connection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                log.info("Server error message: " + response.toString());
            }

            connection.disconnect();

        } catch (Exception e) {
        log.info("Exception occurred while posting flows: " + e.getMessage());
        e.printStackTrace();  // Print the stack trace for more detailed error information
        }
    }



        


private void sendFlowMessagesToServer(String flowString) {
        try {
            // Convert the results to a JSON format
            String jsonResults = "{ \"flowString\": \"" + flowString + "\" }";

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


    private static byte[] ipAddressToBytes(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String byteArrayToString(byte[] byteArray) {
        StringBuilder result = new StringBuilder();
        for (byte b : byteArray) {
            result.append(String.format("%02X", b)).append(" ");
        }
        return result.toString().trim();
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






    
}
