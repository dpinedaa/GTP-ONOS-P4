package edu.fiu.adwise.p4_gtp;
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

import java.nio.ByteBuffer;
import java.util.Arrays; // Import the Arrays class
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.onosproject.net.pi.runtime.PiRegisterCell;
import org.onosproject.net.pi.runtime.PiRegisterCellId;
import org.onosproject.net.pi.model.PiRegisterId;
import org.onosproject.net.pi.model.PiData;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


import org.onosproject.net.pi.model.PiRegisterId;
import org.onosproject.net.pi.model.PiRegisterModel;
import org.onosproject.net.pi.runtime.PiEntity;
import org.onosproject.net.pi.runtime.PiHandle;
import org.onosproject.net.pi.runtime.PiRegisterCell;
import org.onosproject.net.pi.runtime.PiRegisterCellId;



@Component(immediate = true)
public class UpdatePacketSize {
    
    private static final Logger log = LoggerFactory.getLogger(UpdatePacketSize.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    private static DeviceId deviceId = DeviceId.deviceId("device:s1");
    private ScheduledExecutorService scheduledExecutor;


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private PiRegisterModel registerModel;

    List<FlowDetails> flowDetailsList = GetFlow.getFlowDetailsList();

    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.update-packet-size");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new SendUpdatedPacketSize(deviceId), 0, 5, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        log.info("Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        } 
        flowDetailsList.clear();
        log.info("Stopped");
    }



    private class SendUpdatedPacketSize implements Runnable {
        private DeviceId deviceId;

        public SendUpdatedPacketSize(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            UpdatePacketSizeMethod();
        }
    } 


    public void UpdatePacketSizeMethod() {
        // Assuming you have a PiRegisterId instance

        PiRegisterId registerId = PiRegisterId.of("IngressPipeImpl.r_size"); // Replace with your register ID
        String registerIdString = registerId.toString();
       

        long registerSize = registerModel.size();
        String registerSizeString = Long.toString(registerSize);

        PiRegisterCellId registerCellId = PiRegisterCellId.of(registerId, 0);
        String registerCellIdString = registerCellId.toString();
        PiData piData = PiRegisterCell.data();


        try{
            Socket socket = new Socket("10.102.211.11", 6000);
            OutputStream outputStream = socket.getOutputStream();

            String data = registerIdString + " " + registerCellIdString + " "  + registerSizeString + "\n";
            // Send the data to the server
            outputStream.write(data.getBytes());
            
            // Close the socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        

    }


    

    
    
}
