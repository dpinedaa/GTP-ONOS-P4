package edu.fiu.adwise.p4_gtp;

import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;
import java.io.InputStreamReader;


@Component(immediate = true)
public class AddDBFlow {
    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();
    List<UnidirectionalFlow> currentList = new ArrayList<UnidirectionalFlow>();


    private static final Logger log = LoggerFactory.getLogger(AddDBFlow.class);

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
        appId = coreService.registerApplication("edu.fiu.adwise.addDBFlow");

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new CheckForNewElement(), 0, 5, TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        log.info("AddDBFlow Stopped");
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        log.info("Stopped");
    }

    private class CheckForNewElement implements Runnable {
        @Override
        public void run() {
            String postUrl = "http://10.102.211.11:3000/flows";
            postFlows(unidirectionalFlowList, postUrl);
        }
    }


    public static void postFlows(List<UnidirectionalFlow> flows, String postUrl) {

        for (UnidirectionalFlow flow : flows) {
            try {
                URL url = new URL(postUrl);
                
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    String jsonData = prepareJsonData(flow);

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
        
    }

    

    private static String prepareJsonData(UnidirectionalFlow flow) {
        return String.format(
                "{\"flowId\":\"%s\",\"srcIP\":\"%s\",\"dstIP\":\"%s\",\"protocol\":%d,\"forwardDuration\":%d,\"forwardPacketCount\":%d,\"forwardByteCount\":%d,\"backwardDuration\":%d,\"backwardPacketCount\":%d,\"backwardByteCount\":%d,\"flowBytesPerSecond\":%.1f,\"backwardPacketsPerSecond\":%.1f,\"lrPrediction\":%.2f,\"rfPrediction\":%.2f,\"nbPrediction\":%.2f,\"knnPrediction\":%.2f}",
                flow.getForwardFlow().getFlowId(), flow.getForwardFlow().getSrcInnerIpv4(), flow.getForwardFlow().getDstInnerIpv4(),
                flow.getForwardFlow().getInnerIpv4Protocol(),
                flow.getForwardFlow().getCurrentDurationSeconds(), flow.getForwardFlow().getCurrentPacketCount(),
                flow.getForwardFlow().getCurrentBytesCount(), flow.getBackwardFlow().getCurrentDurationSeconds(),
                flow.getBackwardFlow().getCurrentPacketCount(), flow.getBackwardFlow().getCurrentBytesCount(),
                flow.getFlowBytesPerSecond(), flow.getBwdPacketsPerSecond(), flow.getPredictionLR(),
                flow.getPredictionRF(), flow.getPredictionNB(), flow.getPredictionKNN());
    }

   
}
