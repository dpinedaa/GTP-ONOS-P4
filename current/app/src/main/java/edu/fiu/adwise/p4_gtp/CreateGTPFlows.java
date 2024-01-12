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
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiActionParam;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import org.onosproject.net.ConnectPoint; 
import org.onosproject.net.PortNumber;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.flow.DefaultTrafficTreatment;





@Component(immediate = true)
public class CreateGTPFlows {
    //GetFlow getFlowInstance = new GetFlow();
    
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



    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            //String TunnelID = "";
            String innerSrcAddressString = "";
            String innerDstAddressString = "";
            int innerProtocol = 0;
            int innerSrcPort = 0;
            int innerDstPort = 0;
            String outputPort = "";

            // Get the port from which the packet is received
            //int receivedPort = context.inPacket().receivedFrom().port().toLong();
            PortNumber receivedPort = context.inPacket().receivedFrom().port();
            String receivedPortString = receivedPort.toString();



            //Packet In Processing
            DeviceId deviceId = context.inPacket().receivedFrom().deviceId();
            Ethernet eth = context.inPacket().parsed();
            ByteBuffer rawPacketData = context.inPacket().unparsed();       
            byte[] rawDataBytes = rawPacketData.array();

            if(receivedPortString.equals("3")){
                outputPort = "4";
            }
            else if(receivedPortString.equals("4")){
                outputPort = "3";
            }
            
            PortNumber outputPortNumber = PortNumber.portNumber(outputPort);

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(outputPortNumber)
                .build();

            ByteBuffer ethBuffer = ByteBuffer.wrap(eth.serialize());
            OutboundPacket packet = new DefaultOutboundPacket(deviceId, treatment, ethBuffer);

            
            
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
                //Check if innerProtocol is 1 (ICMP)
                if(innerProtocol == 1){
                    innerSrcPort = 0;
                    innerDstPort = 0;

                }
                else{
                    innerSrcPort = ((innerSrcPortBytes[0] & 0xFF) << 8) | (innerSrcPortBytes[1] & 0xFF);
                    innerDstPort = ((innerDstPortBytes[0] & 0xFF) << 8) | (innerDstPortBytes[1] & 0xFF);
                }
                                       
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
                
                // Insert the FlowRule
                flowRuleService.applyFlowRules(FwdRule);
              
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

                if(!isDuplicate(innerSrcAddressString, innerDstAddressString, innerProtocol)){
                    FlowDetails FwdFlowDetails = new FlowDetails(
                                " ", //FlowID
                                innerSrcAddressString, //SrcAddress
                                innerDstAddressString, //DstAddress
                                innerProtocol, //Protocol
                                0, //currentPacketCount
                                0, //currentByteCount
                                0, //currentDurationSeconds
                                0, //currentDurationMicroseconds
                                0, //pastPacketCount
                                0, //pastByteCount
                                0, //pastDurationSeconds
                                0 //pastDurationMicroseconds
                        );
                    flowDetailsList.add(FwdFlowDetails);

                    FlowDetails BwdFlowDetails = new FlowDetails(
                                " ", //FlowID
                                innerDstAddressString, //SrcAddress
                                innerSrcAddressString, //DstAddress
                                innerProtocol, //Protocol
                                0, //currentPacketCount
                                0, //currentByteCount
                                0, //currentDurationSeconds
                                0, //currentDurationMicroseconds
                                0, //pastPacketCount
                                0, //pastByteCount
                                0, //pastDurationSeconds
                                0 //pastDurationMicroseconds
                        );

                    flowDetailsList.add(BwdFlowDetails);
                    UnidirectionalFlow unidirectionalFlow = new UnidirectionalFlow(
                                FwdFlowDetails, //FwdFlowDetails
                                BwdFlowDetails, //BwdFlowDetails
                                0, //FlowBytesPerSecond
                                0, //BwdPacketsPerSecond
                                0, //BwdPacketLengthMax
                                0 //BwdPacketLengthMin
                    );
                    
                    unidirectionalFlowList.add(unidirectionalFlow);
                }



                try{
                    // Create a Socket connection to the Python server
                    Socket socket = new Socket("10.102.211.11", 6000);
                    OutputStream outputStream = socket.getOutputStream();

                    // Construct the data string
                    String data = "PORTPORT:" + "," + "DIANA!!!!!" + "," + receivedPortString + "," + innerSrcAddressString + "," + innerDstAddressString + "," +
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


        private boolean isDuplicate(String srcAddress, String dstAddress, int protocol) {
            for (FlowDetails flowDetails : flowDetailsList) {
                if(flowDetails.getSrcInnerIpv4().equals(srcAddress) &&
                        flowDetails.getDstInnerIpv4().equals(dstAddress) &&
                        flowDetails.getInnerIpv4Protocol() == protocol) {
                    // Flow details already exist, it's a duplicate
                    return true;
                }
            }
            return false;
        }
        
      

    
}
