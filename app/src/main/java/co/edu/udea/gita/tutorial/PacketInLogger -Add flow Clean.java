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

import java.util.Timer;
import java.util.TimerTask;

import co.edu.udea.gita.tutorial.common.Utils;
import com.google.common.collect.Lists;
import java.util.Collection;

import org.onosproject.net.flow.DefaultFlowRule;


import java.nio.ByteBuffer;
import java.util.Arrays; // Import the Arrays class
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.onosproject.net.flow.FlowRuleProvider;




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

    private ScheduledExecutorService scheduledExecutor;



    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;


    private byte[] TunnelIDBytes;
    
    //Add the DeviceId variable
    private DeviceId deviceId = DeviceId.deviceId("device:s1");

    private Timer timer; // Declare the timer here

    


    

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
        // Shutdown the scheduled executor
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
    }

    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            
            DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
            log.info("Device ID: {}", deviceId);
            Ethernet eth = context.inPacket().parsed();
            ByteBuffer rawPacketData = context.inPacket().unparsed();

            byte[] rawDataBytes = rawPacketData.array();
            log.info("Raw packet-in: {}", rawDataBytes);


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

                
                if (rawDataBytes.length >= 50) {
                // Extract bytes 46 to 49 from the byte array
                    TunnelIDBytes = Arrays.copyOfRange(rawDataBytes, 46, 50);

               }
            }

            addGTPRule(deviceId, TunnelIDBytes,0);
    
        }

       

        private void addGTPRule(DeviceId deviceId, byte[] TunnelIDBytes, int index) {
            log.info("Hi Diana. \n\n I'm the addGTPRule method in the PacketInLogger class");

            // Create a PiCriterion to match on hdr.gtp.teid with TunnelID
            final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
                    .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
                    .build();
            
            log.info("gtpTunnelCriterion: {}", gtpTunnelCriterion);

            if (Arrays.equals(TunnelIDBytes, new byte[]{0x00, 0x00, 0x00, 0x06})) {
                 // Create a PiAction to apply the "track_tunnel" action
                final PiAction trackTunnelAction = PiAction.builder()
                .withId(PiActionId.of("IngressPipeImpl.drop"))
                .build();
            }

            else{
                 // Create a PiAction to apply the "track_tunnel" action
                final PiAction trackTunnelAction = PiAction.builder()
                .withId(PiActionId.of("IngressPipeImpl.track_tunnel"))
                .withParameter(new PiActionParam(PiActionParamId.of("index"), 0))
                .build();
            }
        
            final FlowRule rule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", gtpTunnelCriterion, trackTunnelAction);
            // Insert the FlowRule
            flowRuleService.applyFlowRules(rule);
        
        
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
        // Get all flow entries for the specified device
        Iterable<FlowEntry> flowEntries = flowRuleService.getFlowEntries(deviceId);
        log.info("Active flows in the 'IngressPipeImpl.gtp_tunnel' table:");
        for (FlowEntry flowEntry : flowEntries) {
            //Show only the flows that the selector contains the hdr.gtp.teid field Flow Entry Selector: DefaultTrafficSelector{criteria=[hdr.gtp.teid=0x1]} 
            
                if (flowEntry.packets() >= 50) {
                    try {
                        if (flowEntry.packets() >= 50) {
                            log.info("Flow has reached 50 packets. Deleting the flow...");
                            log.info("This is the flow entry details: {}", flowEntry);
                            log.info("Flow Entry Table ID: {}", flowEntry.tableId());
                            log.info("Flow Entry ID: {}", flowEntry.id());
                            log.info("Flow Entry Selector: {}", flowEntry.selector());
                            log.info("Flow Number of packets: {}", flowEntry.packets());
                            log.info("Flow Number of bytes: {}", flowEntry.bytes());
    
                            log.info("Hi Diana. \n\n I'm the deleteFlow method in the PacketInLogger class");
                            FlowRule flowRule = DefaultFlowRule.builder()
                                    .forDevice(flowEntry.deviceId())
                                    .forTable(flowEntry.tableId())
                                    .fromApp(appId)
                                    .withPriority(flowEntry.priority())
                                    .withSelector(flowEntry.selector())
                                    .withTreatment(flowEntry.treatment())
                                    .makePermanent()
                                    .build();
    
                            log.info("Flow Rule: {}", flowRule);
    
                            // Remove the flow rule using the FlowRuleService
                            flowRuleService.removeFlowRules(flowRule);
    
                            log.info("Flow Entry with ID {} has been deleted.", flowEntry.id());
                        }
                    } catch (Exception e) {
                        // Handle exceptions that may occur within the if block
                        log.error("Error while processing flow entry: {}", e.getMessage());
                    }
                } 
                       
            
        }
    }

    
       
    
}
