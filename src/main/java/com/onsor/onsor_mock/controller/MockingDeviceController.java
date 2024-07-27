package com.onsor.onsor_mock.controller;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/devices")
public class MockingDeviceController {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "spring-boot-client";
    private static final String TOPIC = "v1/devices/me/telemetry";
    private static final String ACCESS_TOKEN = "wirHzDCrnf7WeRiskLfK";

    private MqttClient mqttClient;

    private Random random = new Random();

    @Autowired
    public MockingDeviceController() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(ACCESS_TOKEN);
        mqttClient.connect(options);
    }

    @GetMapping("/energy")
    public PieChartData getEnergyData() {
        return generateAndSendPieChartData();
    }

    @Scheduled(fixedRate = 5000)  // Send data every 2 seconds
    public void sendEnergyData() {
        generateAndSendPieChartData();
    }

    private PieChartData generateAndSendPieChartData() {
        // Simulate random values for different categories
        double value1 = random.nextDouble() * 35;
        double value2 = random.nextDouble() * 35;
        double value3 = random.nextDouble() * 35;

        // Calculate the total sum of the values
        double total = value1 + value2 + value3;

        // Normalize the values to ensure their sum is at most 35%
        if (total > 35) {
            value1 = (value1 / total) * 35;
            value2 = (value2 / total) * 35;
            value3 = (value3 / total) * 35;
        }

        Map<String, Double> dataMap = new HashMap<>();
        dataMap.put("device 1", value1);
        dataMap.put("device 2", value2);
        dataMap.put("device 3", value3);

        PieChartData pieChartData = new PieChartData(dataMap);

        // Prepare JSON payload
        StringBuilder payloadBuilder = new StringBuilder("{");
        dataMap.forEach((key, value) -> payloadBuilder.append(String.format("\"%s\": %.2f,", key, value)));
        payloadBuilder.setLength(payloadBuilder.length() - 1); // Remove last comma
        payloadBuilder.append("}");

        String payload = payloadBuilder.toString();

        // Publish the pie chart data to the MQTT topic
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);

        try {
            mqttClient.publish(TOPIC, message);
            System.out.println("Published: " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return pieChartData;
    }


    static class PieChartData {
        private Map<String, Double> data;

        public PieChartData(Map<String, Double> data) {
            this.data = data;
        }

        public Map<String, Double> getData() {
            return data;
        }

        public void setData(Map<String, Double> data) {
            this.data = data;
        }
    }
}
