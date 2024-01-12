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




@Component(immediate = true)
public class PredictionTracker { 

    String flowIndex = "";
    String LR = "";
    String RF = "";
    String NB = "";
    String KNN = "";
    int flowIndexInt = 0;
    int LRInt = 0;
    int RFInt = 0;
    int NBInt = 0;
    int KNNInt = 0;

    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();
    List<String> predictionStringList = ModelPredictions.getPredictionStringList();

    private static final Logger log = LoggerFactory.getLogger(PredictionTracker.class);
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
        appId = coreService.registerApplication("edu.fiu.adwise.detection");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        scheduledExecutor = Executors.newScheduledThreadPool(1);
        scheduledExecutor.scheduleAtFixedRate(new UpdatePredicition(deviceId), 0, 2, TimeUnit.SECONDS);
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


    private class UpdatePredicition implements Runnable {
        private DeviceId deviceId;

        public UpdatePredicition(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            evaluateExistingPredictions();
        }

    }
    
    private void evaluateExistingPredictions() {
        Iterator<String> iterator = predictionStringList.iterator();
        
        while (iterator.hasNext()) {

            String predictionString = iterator.next();
            // Define the patterns
            Pattern patternFlowIndex = Pattern.compile("\"flowIndex\":\\s*\"([^\"]*)\"");
            Pattern patternLR = Pattern.compile("\"logistic_regression\":\\s*\\{\"prediction\":\\s*([^\"]*)\"");
            Pattern patternRF = Pattern.compile("\"random_forest\":\\s*\\{\"prediction\":\\s*([^\"]*)\"");
            Pattern patternNB = Pattern.compile("\"naive_bayes\":\\s*\\{\"prediction\":\\s*([^\"]*)\"");
            Pattern patternKNN = Pattern.compile("\"knearest_neighbours\":\\s*\\{\"prediction\":\\s*([^\"]*)\"");

            // Create matchers
            Matcher matcherFlowIndex = patternFlowIndex.matcher(predictionString);
            Matcher matcherLR = patternLR.matcher(predictionString);

            Matcher matcherRF = patternRF.matcher(predictionString);
            Matcher matcherNB = patternNB.matcher(predictionString);

            Matcher matcherKNN = patternKNN.matcher(predictionString);

            if (matcherFlowIndex.find()) {
                // Extract and print the matched text
                flowIndex = matcherFlowIndex.group(1);
                //sendFlowMessagesToServer(flowIndex);
                //Convert the FlowIndex into an int and remove anything that it's nto a number 
                flowIndexInt = Integer.parseInt(flowIndex.replaceAll("[^0-9]", ""));

            }

            if (matcherLR.find()) {
                // Extract and print the matched text
                LR = matcherLR.group(1);
                //sendFlowMessagesToServer(LR);
                LRInt = Integer.parseInt(LR.replaceAll("[^0-9]", ""));
            }

            if (matcherRF.find()) {
                // Extract and print the matched text
                RF = matcherRF.group(1);
                //sendFlowMessagesToServer(RF);
                RFInt = Integer.parseInt(RF.replaceAll("[^0-9]", ""));
            }

            if (matcherNB.find()) {
                // Extract and print the matched text
                NB = matcherNB.group(1);
                //sendFlowMessagesToServer(NB);
                NBInt = Integer.parseInt(NB.replaceAll("[^0-9]", ""));

            }

            if (matcherKNN.find()) {
                // Extract and print the matched text
                KNN = matcherKNN.group(1);
                //sendFlowMessagesToServer(KNN);
                KNNInt = Integer.parseInt(KNN.replaceAll("[^0-9]", ""));

            }

            String extractedData = flowIndexInt + " " + LRInt + " " +
                    KNNInt + " " + NBInt + " "  + RFInt;
            sendFlowMessagesToServer(extractedData);
            updatePredictionsUnidirectionalFlow(flowIndexInt, LRInt, RFInt, NBInt, KNNInt);
            iterator.remove();
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


    private void updatePredictionsUnidirectionalFlow(int flowIndex, int LR, int RF, int NB, int KNN) {
        UnidirectionalFlow unidirectionalFlow = unidirectionalFlowList.get(flowIndex);
        unidirectionalFlow.setPredictionQueueLR(LR);
        unidirectionalFlow.setPredictionQueueRF(RF);
        unidirectionalFlow.setPredictionQueueNB(NB);
        unidirectionalFlow.setPredictionQueueKNN(KNN);

        // Update the prediction values
        double averageLR = calculateAverage(unidirectionalFlow.getPredictionQueueLR());
        double averageRF = calculateAverage(unidirectionalFlow.getPredictionQueueRF());
        double averageNB = calculateAverage(unidirectionalFlow.getPredictionQueueNB());
        double averageKNN = calculateAverage(unidirectionalFlow.getPredictionQueueKNN());

        unidirectionalFlow.setPredictionLR(averageLR);
        unidirectionalFlow.setPredictionRF(averageRF);
        unidirectionalFlow.setPredictionNB(averageNB);
        unidirectionalFlow.setPredictionKNN(averageKNN);
    }

    private double calculateAverage(Queue<Integer> queue) {
        if (queue.isEmpty()) {
            return 0.0;
        }

        int sum = 0;
        for (Integer value : queue) {
            sum += value;
        }

        return (double) sum / queue.size();
    }
  
    // Assuming you have a method to convert UnidirectionalFlow to JSON
    private String convertUnidirectionalFlowToJson(UnidirectionalFlow unidirectionalFlow) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(unidirectionalFlow);
        } catch (JsonProcessingException e) {
            log.error("Error converting UnidirectionalFlow to JSON", e);
            return "{}"; // Return empty JSON as a fallback
        }
    }
}
