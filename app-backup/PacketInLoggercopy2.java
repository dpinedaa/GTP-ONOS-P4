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
public class PacketInLogger {

    private static final Logger log = LoggerFactory.getLogger(PacketInLogger.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService pipeconfService;

    private ApplicationId appId;
    private final PacketProcessor packetProcessor = new PacketInProcessor();


    private List<PiCounterCell> counterCells = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;


    private byte[] TunnelIDBytes;

    private DeviceId deviceId = DeviceId.deviceId("device:s1");

    private ScheduledExecutorService scheduledExecutor;

    List<FlowInfo> flowInfoList = new ArrayList<>();








    @Activate
    public void activate() {
        appId = coreService.registerApplication("co.edu.udea.gita.packet-in-logger");
        packetService.addProcessor(packetProcessor, PacketPriority.REACTIVE.priorityValue());
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        packetService.requestPackets(selector, PacketPriority.REACTIVE, appId);
        log.info("Packet-in Logger Started");
        
         // Start the scheduled executor to execute printActiveFlows every 2 seconds
         scheduledExecutor = Executors.newScheduledThreadPool(1);
         scheduledExecutor.scheduleAtFixedRate(new PrintActiveFlowsTask(deviceId), 0, 2, TimeUnit.SECONDS);

    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        log.info("Packet-in Logger Stopped");
        // Shutdown the scheduledExecutor to stop the scheduled task
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        flowRuleService.removeFlowRulesById(appId);
        log.info("Stopped");

    }

    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            String TunnelID = "";
            DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
            log.info("Device ID: {}", deviceId);
            Ethernet eth = context.inPacket().parsed();
            ByteBuffer rawPacketData = context.inPacket().unparsed();

            byte[] rawDataBytes = rawPacketData.array();
            log.info("Raw packet-in: {}", rawDataBytes);
            log.info("\n\n\nIntercepted a raw packet-in: {}\n\n\n", byteArrayToHexString(rawDataBytes));
            
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

                
                log.info("Intercepted a packet-in: {}", byteArrayToHexString(rawDataBytes));
                if (rawDataBytes.length >= 50) {
                // Extract bytes 46 to 49 from the byte array
                    TunnelIDBytes = Arrays.copyOfRange(rawDataBytes, 46, 50);
                    TunnelID = "0x" + byteArrayToHexString(TunnelIDBytes);

                    log.info("Tunnel ID: {}", TunnelID);

               }
            }

            printActiveFlows(deviceId);
            getIndirectCounter();
            addGTPRule(deviceId,TunnelID, TunnelIDBytes,0);
    
        }

        // Helper method to convert a byte array to a hexadecimal string
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

        private byte[] hexStringToByteArray(String hexString) {
            int length = hexString.length();
            byte[] byteArray = new byte[length / 2];

            for (int i = 0; i < length; i += 2) {
                byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
                        Character.digit(hexString.charAt(i + 1), 16));
            }

            return byteArray;
        }

        private void getIndirectCounter(){
            log.info("Hi Diana. \n\n I'm the getIndirectCounter method in the PacketInLogger class");

            Optional<PiPipeconf> pipeconf = pipeconfService.getPipeconf(PIPECONF_ID);

            if (pipeconf.isPresent()){
                PiPipeconf piPipeconf = pipeconf.get();
                log.info("Diana \n\n PiPipeconf ID: {}", piPipeconf.id());
                
                logCounters(piPipeconf.pipelineModel());
                logTables(piPipeconf.pipelineModel());
            }
        }
        private void logTables(PiPipelineModel pipelineModel) {
            pipelineModel.tables().forEach(table -> {
                log.info("\nDiana \n Table ID: {}\n\n", table.id());
                log.info("\nDiana \n Table Name: {}\n\n", table);
                // Add more logging for table details as needed
            });
        }

        private void logCounters(PiPipelineModel pipelineModel) {
            PiCounterId counterId = PiCounterId.of("IngressPipeImpl.tunnel_counter");
            //Get the counter that ID is gtp_tunnel_counter
            //PiCounterModel counter = pipelineModel.counter(new PiCounterId("gtp_tunnel_counter"));
            Optional<PiCounterModel> counterOptional = pipelineModel.counter(counterId);
            PiCounterModel counter = counterOptional.orElse(null);

            long counterSize = counter.size();
            //log.info("\nDiana \n Counter Size: {}\n\n", counter.size());
            //log.info("\nDiana \n counter: {}\n\n", counter);
            //log.info("\nDiana \n Counter ID: {}\n\n", counter.id());
            //Get the counter type
            PiCounterType counterType = counter.counterType();
            //log.info("\nDiana \n Counter Type: {}\n\n", counter.counterType());
                        
            //Help me to use PiCounterCell using the Pi CounterModel counter based on this counter: P4CounterModel{id=IngressPipeImpl.tunnel_counter, counterType=INDIRECT, unit=PACKETS, table=null, size=30}
            
            // Iterate over the cells
            for (long cellIndex = 0; cellIndex < counterSize; cellIndex++) {
                PiCounterCellId cellId = PiCounterCellId.ofIndirect(counter.id(), cellIndex);
                PiCounterCell cell = new PiCounterCell(cellId, 0, 0);
                // Access the cell's data if needed
                counterCells.add(cell);
                PiCounterCellData cellData = cell.data();
                //log.info("\nDiana \n Cell Data: {}\n\n", cellData);
                long packets = cellData.packets();
                long bytes = cellData.bytes();
                //log.info("\nDiana \n Cell ID:{} \n Packets:{} \n Bytes:{}\n\n", cellId, packets, bytes);
            }
        }


        private void addGTPRule(DeviceId deviceId, String TunnelID, byte[] TunnelIDBytes, int index) {
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

            FlowInfo flowInfo = new FlowInfo();
            flowInfo.setFlowRule(rule);

            flowInfoList.add(flowInfo);


            
            TrafficSelector flowRuleSelector = flowInfo.getFlowRule().selector();

            

            log.info("\nDIANA!!!!!!\nFlow Rule Selector: {}\n\n", flowRuleSelector);
        
            // Assuming you have a list of FlowEntry instances
            Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);
        
            for (FlowEntry flowEntry : flowEntries) {
                TrafficSelector flowEntrySelector = flowEntry.selector();
                log.info("\nDIANA!!!!!!\nFlow Entry Selector: {}\n\n", flowEntrySelector);
        
                // Check if the FlowRule's selector matches the FlowEntry's selector
                if (areSelectorsEqual(flowRuleSelector, flowEntrySelector)) {
                    log.info("\nDIANA!!!!!!!\nFlow Rule Selector: {} matches Flow Entry Selector: {}\n\n", flowRuleSelector, flowEntrySelector);
                    // Update the FlowInfo with the associated FlowEntry
                    flowInfo.setFlowEntry(flowEntry);
                    log.info("Associated Flow Rule: {} with Flow Entry: {}", flowInfo.getFlowRule(), flowInfo.getFlowEntry());
                    break;
                }
            }

        }
        private boolean areSelectorsEqual(TrafficSelector selector1, TrafficSelector selector2) {
            // Implement your custom logic to compare selectors here
            // Compare the individual criteria within the selectors
            // and return true if they are equivalent, otherwise return false
        
            // For simplicity, let's assume you want an exact match of selectors
            return selector1.equals(selector2);
        }
    }
        

        private class PrintActiveFlowsTask extends TimerTask {
            private final DeviceId deviceId;
    
            public PrintActiveFlowsTask(DeviceId deviceId) {
                this.deviceId = deviceId;
            }
    
            @Override
            public void run() {
                // Call the printActiveFlows method here
                printActiveFlows(deviceId);
            }
        }


        private void printActiveFlows(DeviceId deviceId) {
            for (FlowInfo flowInfo : flowInfoList) {
                // Get the FlowEntry associated with the FlowInfo
                FlowEntry flowEntry = flowInfo.getFlowEntry();

                TrafficSelector selector = flowEntry.selector();
                log.info("\nDiana\nFlow Entry Selector: {}\n", selector);
                String selectorString = selector.toString();

                log.info("\nDiana\nFlow Entry Selector String: {}\n", selectorString);


        
                if (flowEntry.packets()>=50) {
                    // Get the FlowEntry ID and packets count
                    long packets = flowEntry.packets();
                    log.info("\nDiana\nFlow State:{}\n", flowEntry.state());
                    log.info("\nDiana\nFlow Entry Packets: {}\n", packets);
                    log.info("\nDiana\nFlow Entry Selector: {}\n", flowInfo.getFlowEntry().selector());
                    log.info("\nDiana\nFlow Rule Selector: {}\n", flowInfo.getFlowRule().selector());
                    flowRuleService.removeFlowRules(flowInfo.getFlowRule());
                    log.info("\nDiana\nFlow Rule ID: {} removed\n", flowInfo.getFlowRule().id());

                    flowInfoList.remove(flowInfo);
                    dropGTPTunnel(flowEntry);

                } 
            }
        }

        private void dropGTPTunnel(FlowEntry flowEntry) {
            TrafficSelector selector = flowEntry.selector();
            String selectorString = selector.toString();

            // Define a pattern to match the tunnel ID
            Pattern pattern = Pattern.compile("hdr\\.gtp\\.teid=0x([0-9a-fA-F]+)");
            Matcher matcher = pattern.matcher(selectorString);

            if (matcher.find()) {
                // Extract the tunnel ID
                String tunnelIdHex = matcher.group(1);
                long tunnelId = Long.parseLong(tunnelIdHex, 16);

                // Convert the tunnel ID to a byte array
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(tunnelId);
                byte[] tunnelIDBytes = buffer.array();
            }


            final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
            .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
            .build();

            log.info("gtpTunnelCriterion: {}", gtpTunnelCriterion);

            // Create a PiAction to drop
            final PiAction dropAction = PiAction.builder()
                    .withId(PiActionId.of("IngressPipeImpl.drop"))
                    .build();

            // Build the FlowRule with the specified index
            final FlowRule rule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", gtpTunnelCriterion, dropAction);

            // Insert the FlowRule
            flowRuleService.applyFlowRules(rule); 
            scheduledExecutor.scheduleAtFixedRate(new PrintActiveFlowsTask(deviceId), 0, 2, TimeUnit.SECONDS);


        
        }
}
