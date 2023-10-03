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





import java.nio.ByteBuffer;
import java.util.Arrays; // Import the Arrays class
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;



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




    @Activate
    public void activate() {
        appId = coreService.registerApplication("co.edu.udea.gita.packet-in-logger");
        packetService.addProcessor(packetProcessor, PacketPriority.REACTIVE.priorityValue());
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        packetService.requestPackets(selector, PacketPriority.REACTIVE, appId);
        log.info("Packet-in Logger Started");
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        log.info("Packet-in Logger Stopped");
    }

    private class PacketInProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
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
                    byte[] extractedBytes = Arrays.copyOfRange(rawDataBytes, 46, 50);
                    String TunnelID = byteArrayToHexString(extractedBytes);

                    log.info("Tunnel ID: 0x{}", TunnelID);

               }
            }

            getIndirectCounter();
            

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
                long packets = cellData.packets();
                long bytes = cellData.bytes();
                log.info("\nDiana \n Cell ID:{} \n Packets:{} \n Bytes:{}\n\n", cellId, packets, bytes);
            }
            
        }

        
        
    }
}
