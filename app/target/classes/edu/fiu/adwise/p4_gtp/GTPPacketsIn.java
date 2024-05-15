package us.fiu.adwise.app;
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
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiActionParam;
import static us.fiu.adwise.app.AppConstants.PIPECONF_ID;
import org.onosproject.net.pi.service.PiPipeconfService;
import org.onosproject.net.pi.model.PiPipeconf;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficTreatment;
import us.fiu.adwise.app.common.Utils;
import com.google.common.collect.Lists;
import java.nio.ByteBuffer;
import java.util.Arrays; // Import the Arrays class
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;






@Component(immediate = true)
public class GTPPacketsIn {
    //GetFlow getFlowInstance = new GetFlow();
    public static List<FlowRule> flowRulesList = new ArrayList<>();

    
    private static final Logger log = LoggerFactory.getLogger(GTPPacketsIn.class);
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
    private int flowIndex = 0;
     // Create an instance of GetFlow
    List<FlowDetails> flowDetailsList = GetFlow.getFlowDetailsList();
    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();




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
        flowIndex = 0;
    }

    public static List<FlowRule> getFlowRulesList() {
        return flowRulesList;
    }




    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            //String TunnelID = "";
            String innerSrcAddressString = "";
            String innerDstAddressString = "";
            int innerProtocol = 0;
            int innerSrcPort = 0;
            int innerDstPort = 0;
            FlowRule fwd = null;
            FlowRule bwd = null;
            


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
                //byte[] TunnelIDBytes = Arrays.copyOfRange(rawDataBytes, 46, 50);
                byte[] innerSrcAddressBytes = Arrays.copyOfRange(rawDataBytes, 70, 74);
                byte[] innerDstAddressBytes = Arrays.copyOfRange(rawDataBytes, 74, 78);
                byte[] innerProtocolBytes = Arrays.copyOfRange(rawDataBytes, 67, 68);
                byte[] innerSrcPortBytes = Arrays.copyOfRange(rawDataBytes, 78, 80);
                byte[] innerDstPortBytes = Arrays.copyOfRange(rawDataBytes, 80, 82);
                

                try{
                    InetAddress innerSrcAddress = InetAddress.getByAddress(innerSrcAddressBytes);   
                    innerSrcAddressString = innerSrcAddress.getHostAddress();
                }
                catch(UnknownHostException e){
                    log.info("Exception: {}", e);
                }

                try{
                    InetAddress innerDstAddress = InetAddress.getByAddress(innerDstAddressBytes);   
                    innerDstAddressString = innerDstAddress.getHostAddress();
                }
                catch(UnknownHostException e){
                    log.info("Exception: {}", e);
                }
                
                
                
                innerProtocol = ((innerProtocolBytes[0] & 0xFF));
                innerSrcPort = ((innerSrcPortBytes[0] & 0xFF) << 8) | (innerSrcPortBytes[1] & 0xFF);
                innerDstPort = ((innerDstPortBytes[0] & 0xFF) << 8) | (innerDstPortBytes[1] & 0xFF);
                       
                //Add Forward Flow
                final PiCriterion FwdTunnelCriterion = PiCriterion.builder()
                //.matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.src_addr"), innerSrcAddressBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.dst_addr"), innerDstAddressBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.protocol"), innerProtocolBytes)
                //.matchExact(PiMatchFieldId.of("hdr.inner_udp.srcPort"), innerSrcPortBytes)
                //.matchExact(PiMatchFieldId.of("hdr.inner_udp.dstPort"), innerDstPortBytes)
                .build();
                            
        
                log.info("FwdTunnelCriterion: {}", FwdTunnelCriterion);
                final PiAction trackForwardTunnelAction;
                trackForwardTunnelAction = PiAction.builder()
                        .withId(PiActionId.of("IngressPipeImpl.track_tunnel"))
                        //.withParameter(new PiActionParam(PiActionParamId.of("index"), flowIndex))
                        .build();

                // Build the FlowRule with the specified index
                final FlowRule FwdRule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", FwdTunnelCriterion, trackForwardTunnelAction);
                fwd = FwdRule;
                // Insert the FlowRule
                flowRuleService.applyFlowRules(FwdRule);

                flowRulesList.add(FwdRule);

                


                //Add Backward Flow
                final PiCriterion BwdTunnelCriterion = PiCriterion.builder()
                //.matchExact(PiMatchFieldId.of("hdr.gtp.teid"), TunnelIDBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.dst_addr"), innerSrcAddressBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.src_addr"), innerDstAddressBytes)
                .matchExact(PiMatchFieldId.of("hdr.inner_ipv4.protocol"), innerProtocolBytes)
                //.matchExact(PiMatchFieldId.of("hdr.inner_udp.dstPort"), innerSrcPortBytes)
                //.matchExact(PiMatchFieldId.of("hdr.inner_udp.srcPort"), innerDstPortBytes)
                .build();
                            
        
                log.info("BwdTunnelCriterion: {}", BwdTunnelCriterion);
                final PiAction trackBackwardTunnelAction;
                trackBackwardTunnelAction = PiAction.builder()
                        .withId(PiActionId.of("IngressPipeImpl.track_tunnel"))
                        //.withParameter(new PiActionParam(PiActionParamId.of("index"), flowIndex))
                        .build();

                // Build the FlowRule with the specified index
                final FlowRule BwdRule = Utils.buildFlowRule(deviceId, appId, "IngressPipeImpl.gtp_tunnel", BwdTunnelCriterion, trackBackwardTunnelAction);
                
                // Insert the FlowRule
                flowRuleService.applyFlowRules(BwdRule);

                flowRulesList.add(BwdRule); 
                bwd = BwdRule;
                // Add the FlowRule to the list if it's not duplicated with the src and dst addresses
                final String finalInnerSrcAddressString = innerSrcAddressString;
                final String finalInnerDstAddressString = innerDstAddressString;
                final int finalInnerProtocol = innerProtocol;

                if (!flowDetailsList.stream()
                        .anyMatch(flowDetails ->
                                flowDetails.getSrcInnerIpv4().equals(finalInnerSrcAddressString) &&
                                        flowDetails.getDstInnerIpv4().equals(finalInnerDstAddressString) &&
                                        flowDetails.getInnerIpv4Protocol() == finalInnerProtocol)) {

                    FlowDetails fwdFlowDetails = new FlowDetails(
                            " ",
                            innerSrcAddressString,
                            innerDstAddressString,
                            innerProtocol,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0
                    );

                    FlowDetails bwdFlowDetails = new FlowDetails(
                            " ",
                            innerDstAddressString,
                            innerSrcAddressString,
                            innerProtocol,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0
                    );

                    flowDetailsList.add(fwdFlowDetails);
                    flowDetailsList.add(bwdFlowDetails);

                    UnidirectionalFlow unidirectionalFlow = new UnidirectionalFlow(
                            fwdFlowDetails,
                            bwdFlowDetails,
                            0,
                            0,
                            0,
                            0, 
                            fwd,
                            bwd
                    );

                    unidirectionalFlowList.add(unidirectionalFlow);
                }

                
                   

                

                

                try{
                    // Create a Socket connection to the Python server
                    Socket socket = new Socket("10.102.211.11", 6000);
                    OutputStream outputStream = socket.getOutputStream();

                    // Construct the data string
                    String data = innerSrcAddressString + "," + innerDstAddressString + "," +
                                innerProtocol + "," + innerSrcPort + "," +
                                innerDstPort;

                    // Send the data to the server
                    outputStream.write(data.getBytes());
                    
                    // Close the socket
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                }
                
            }
            flowIndex++;
        }
    }
    
        

    
}
