package edu.fiu.adwise.p4_gtp;
import org.onlab.packet.Ethernet;
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
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import static edu.fiu.adwise.p4_gtp.AppConstants.PIPECONF_ID;
import org.onosproject.net.pi.service.PiPipeconfService;
import org.onosproject.net.pi.model.PiPipeconf;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficTreatment;
import edu.fiu.adwise.p4_gtp.common.Utils;
import com.google.common.collect.Lists;
import java.nio.ByteBuffer;
import java.util.Arrays; // Import the Arrays class
import java.math.BigInteger;
import java.nio.ByteOrder;



@Component(immediate = true)
public class CreateGTPFlows {
    private static final Logger log = LoggerFactory.getLogger(CreateGTPFlows.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiPipeconfService pipeconfService;
    private ApplicationId appId;
    private final PacketProcessor packetProcessor = new PacketInProcessor();
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;
    private DeviceId deviceId = DeviceId.deviceId("device:s1");
    

    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.create-gtp-flows");
        packetService.addProcessor(packetProcessor, PacketPriority.REACTIVE.priorityValue());
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        packetService.requestPackets(selector, PacketPriority.REACTIVE, appId);
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        flowRuleService.removeFlowRulesById(appId);
    }

    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            String TunnelID = "";
            

            //Packet In Processing
            DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
            Ethernet eth = context.inPacket().parsed();
            ByteBuffer rawPacketData = context.inPacket().unparsed();       
            byte[] rawDataBytes = rawPacketData.array();
            
            byte [] srcPortBytes = Arrays.copyOfRange(rawDataBytes, 34, 36);
            byte [] dstPortBytes = Arrays.copyOfRange(rawDataBytes, 36, 38);
            int srcPortValue = ((srcPortBytes[0] & 0xFF) << 8) | (srcPortBytes[1] & 0xFF);

            if(srcPortValue == 0x0868){
                if (rawDataBytes.length >= 82) {  // Adjust the length based on your actual byte structure      
                    byte[] TunnelIDBytes = Arrays.copyOfRange(rawDataBytes, 46, 50);
                    byte[] innerSrcAddressBytes = Arrays.copyOfRange(rawDataBytes, 70, 74);
                    byte[] innerDstAddressBytes = Arrays.copyOfRange(rawDataBytes, 74, 78);
                    byte[] innerProtocolBytes = Arrays.copyOfRange(rawDataBytes, 67, 68);
                    byte[] innerSrcPortBytes = Arrays.copyOfRange(rawDataBytes, 78, 80);
                    byte[] innerDstPortBytes = Arrays.copyOfRange(rawDataBytes, 80, 82);
                


                log.info("TunnelIDBytes: {}", Arrays.toString(TunnelIDBytes));
                log.info("innerSrcAddressBytes: {}", Arrays.toString(innerSrcAddressBytes));
                log.info("innerDstAddressBytes: {}", Arrays.toString(innerDstAddressBytes));
                log.info("innerProtocolBytes: {}", Arrays.toString(innerProtocolBytes));
                log.info("innerSrcPortBytes: {}", Arrays.toString(innerSrcPortBytes));
                log.info("innerDstPortBytes: {}", Arrays.toString(innerDstPortBytes));

                

                /* int innerProtocol = ByteBuffer.wrap(innerProtocolBytes).order(ByteOrder.BIG_ENDIAN).getInt();
                int innerSrcPort = ByteBuffer.wrap(innerSrcPortBytes).order(ByteOrder.BIG_ENDIAN).getShort() & 0xFFFF;
                int i nnerDstPort = ByteBuffer.wrap(innerDstPortBytes).order(ByteOrder.BIG_ENDIAN).getShort() & 0xFFFF;
    
                log.info("innerProtocol: {}", innerProtocol);
                log.info("innerSrcPort: {}", innerSrcPort);
                log.info("innerDstPort: {}", innerDstPort);*/
    
                // Create a PiCriterion to match on hdr.gtp.teid with TunnelID
                /* final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
                        .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
                        .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.src_addr"), innerSrcAddressBytes)
                        .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.dst_addr"), innerDstAddressBytes)
                        .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.protocol"), innerProtocol)
                        .matchExact(PiMatchFieldId.of("hdr.inner_udp.srcPort"), innerSrcPort)
                        .matchExact(PiMatchFieldId.of("hdr.inner_udp.dstPort"), innerDstPort)
                        .build(); */

/* 
                final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
                .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), 0x00000001)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.src_addr"), 0xA2D0003)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.dst_addr"), 0xA2D0003)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.protocol"), 0x11)  // Hex for 17
                .matchExact(PiMatchFieldId.of("hdr.inner_udp.srcPort"), 0x07D0)  // Hex for 2000
                .matchExact(PiMatchFieldId.of("hdr.inner_udp.dstPort"), 0x07D0)  // Hex for 2000
                .build();
     */
                

                final PiCriterion gtpTunnelCriterion = PiCriterion.builder()
                .matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.src_addr"), innerSrcAddressBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.dst_addr"), innerDstAddressBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.protocol"), innerProtocolBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_udp.srcPort"), innerSrcPortBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_udp.dstPort"), innerDstPortBytes)
                .build();
                            
        
                log.info("gtpTunnelCriterion: {}", gtpTunnelCriterion);
                final PiAction trackTunnelAction;
                trackTunnelAction = PiAction.builder()
                        .withId(PiActionId.of("IngressPipeImpl.track_tunnel"))
                        .build();

                // Build the FlowRule with the specified index
                final FlowRule rule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", gtpTunnelCriterion, trackTunnelAction);
                
                // Insert the FlowRule
                flowRuleService.applyFlowRules(rule);
                }
                
            }
        }
    }
        

    
}
