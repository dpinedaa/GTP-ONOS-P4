package edu.fiu.adwise.p4_gtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executors;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
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
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowRuleService;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.regex.Pattern;
//import JSONOBJECT
import org.json.JSONObject;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Arrays; // Import the Arrays class
import java.util.ArrayList;

@Component(immediate = true)
public class ModelPredictions {

    public static List<String> predictionStringList = new ArrayList<>();

    private int logisticRegressionPrediction;
    private int knearestNeighboursPrediction;
    private int naiveBayesPrediction;
    private int randomForestPrediction;
    private int flowIndex;

    List<UnidirectionalFlow> unidirectionalFlowList = UpdateUnidirectionalFlow.getUnidirectionalFlowList();

    private static final Logger log = LoggerFactory.getLogger(Detection.class);
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    private DeviceId deviceId = DeviceId.deviceId("device:s1");
    private ScheduledExecutorService scheduledExecutor;
    private ServerSocket serverSocket;
    private final int SERVER_PORT = 9000;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("edu.fiu.adwise.detection");
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();

        // Start the server socket in a separate thread
        Executors.newSingleThreadExecutor().execute(this::startServerSocket);
    }

    @Deactivate
    public void deactivate() {
        log.info("Logger Stopped");
        flowRuleService.removeFlowRulesById(appId);
        log.info("Stopped");

        // Close the server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket: {}", e.getMessage());
        }
    }

    public static List<String> getPredictionStringList() {
        return predictionStringList;
    }

    private void startServerSocket() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            log.info("Server socket started and listening on port {}", SERVER_PORT);

            while (true) {
                // Listen for incoming connections
                Socket clientSocket = serverSocket.accept();

                // Handle the connection in a separate thread (you can modify this part based on your needs)
                Executors.newSingleThreadExecutor().execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log.error("Error starting server socket: {}", e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            // Read data from the client
            String clientData = reader.readLine();
            log.info("Received data from client: {}", clientData);

            // Process the data as needed

            predictionStringList.add(clientData);

        } catch (IOException e) {
            log.error("Error handling client: {}", e.getMessage());
        }
    }


}
