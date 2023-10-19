package edu.fiu.adwise.p4_gtp;
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

import static edu.fiu.adwise.p4_gtp.AppConstants.PIPECONF_ID;
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

import edu.fiu.adwise.p4_gtp.common.Utils;
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
import edu.fiu.adwise.p4_gtp.FlowInfo;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.flow.criteria.PiCriterion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Map;           // Add this import for Map
import java.util.HashMap;  

@Component(immediate = true)
public class PacketInLogger {
    List<FlowRule> flowRulesList = new ArrayList<>();
    List<FlowInfo> flowInfoList = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(PacketInLogger.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService pipeconfService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    private final PacketProcessor packetProcessor = new PacketInProcessor();
    
    private byte[] TunnelIDBytes;
    private DeviceId deviceId = DeviceId.deviceId("device:s1");

    private final FlowRuleListener flowListener = new InternalFlowListener();

     
    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.packet-in-logger");
        packetService.addProcessor(packetProcessor, PacketPriority.REACTIVE.priorityValue());
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        packetService.requestPackets(selector, PacketPriority.REACTIVE, appId);
        log.info("Packet-in Logger Started");
        flowRuleService.addListener(flowListener);

    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        log.info("Packet-in Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        log.info("Stopped");
        flowRuleService.removeListener(flowListener);
    }



    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
  

    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            String TunnelID = "";
            DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
            Ethernet eth = context.inPacket().parsed();
            ByteBuffer rawPacketData = context.inPacket().unparsed();

            byte[] rawDataBytes = rawPacketData.array();
            
            String hexString = byteArrayToHexString(rawDataBytes);

            log.info("Ethernet packet-in: {}", eth);

            IPv4 ipv4Packet = (IPv4) eth.getPayload();
            log.info("\n\n\nIPv4 packet-in: {}", ipv4Packet);

            UDP udpPacket = (UDP) ipv4Packet.getPayload();
            log.info("\n\n\nUDP packet-in: {}", udpPacket);

            // Extract UDP source and destination ports
            int srcPort = udpPacket.getSourcePort();
            int dstPort = udpPacket.getDestinationPort();
            // Now you have the UDP source and destination ports
            log.info("\n\nUDP Source Port: {}, UDP Destination Port: {}\n\n", srcPort, dstPort);

            if(dstPort == 2152 || srcPort == 2152){
                log.info("\n\nThis is a GTP packet\n\n");
                TunnelIDBytes = Arrays.copyOfRange(rawDataBytes, 46, 50);
                TunnelID = "0x" + byteArrayToHexString(TunnelIDBytes);
                log.info("Tunnel ID: {}", TunnelID);
            }

            log.info("Hi Diana. \n\n I'm the addGTPRule method in the PacketInLogger class");
            log.info("Tunnel ID: {}", TunnelID);
        
            // Create a PiCriterion to match on hdr.gtp.teid with TunnelID
            final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
                    .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
                    .build();
        
            log.info("gtpTunnelCriterion: {}", gtpTunnelCriterion);
        
            // Create a PiAction based on the TunnelIDBytes
            final PiAction trackTunnelAction;
            if (TunnelID.equals("0x00000006")){
                // If TunnelIDBytes is 0x00000006, create a PiAction to drop
                trackTunnelAction = PiAction.builder()
                        .withId(PiActionId.of("IngressPipeImpl.drop"))
                        .build();
            } else {
                // Otherwise, create a PiAction to apply the "track_tunnel" action
                trackTunnelAction = PiAction.builder()
                        .withId(PiActionId.of("IngressPipeImpl.track_tunnel"))
                        .withParameter(new PiActionParam(PiActionParamId.of("index"), 0))
                        .build();
            }
        
            // Build the FlowRule with the specified index
            final FlowRule rule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", gtpTunnelCriterion, trackTunnelAction);
            
            // Insert the FlowRule
            flowRuleService.applyFlowRules(rule);

            // Add FlowInfo to the list
            flowRulesList.add(rule);
        }
    }

    private void checkFrequency() {
        long currentTimeMillis = System.currentTimeMillis();
        Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);
        
        for (FlowEntry flowEntry : flowEntries) {
            long packetCount = flowEntry.packets(); // Get packet count
            long elapsedTimeMillis = currentTimeMillis - (flowEntry.life() * 1000);
            long packets = flowEntry.packets();
            double packetFrequency = (double) packets / elapsedTimeMillis;
            
            if (packetFrequency > 0.00000000001) {
                
                Optional<FlowRule> foundFlowRule = flowRulesList.stream()
                        .filter(flowRule -> flowRule.selector().equals(flowEntry.selector()))
                        .findFirst();
                if (foundFlowRule.isPresent()) {
                    
                    log.info("Found flow rule: {}", foundFlowRule.get());
                    flowRulesList.remove(foundFlowRule.get());
                    log.info("Flow Rule List: {}", flowRulesList);
                    FlowRule specificFlowRule = foundFlowRule.get();
                    log.info("Found specific flow rule: {}", specificFlowRule);
                    flowRuleService.removeFlowRules(specificFlowRule);
                    dropGTPTunnel(flowEntry);
                } else {
                    //log.info("Specific flow rule not found for selector: {}", flowEntry.selector());
                    // Handle the case where the specific flow rule was not found
                }
            }
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
    

    private class InternalFlowListener implements FlowRuleListener {
        @Override
        public void event(FlowRuleEvent event) {
            checkFrequency();
        }
    }
    

}


