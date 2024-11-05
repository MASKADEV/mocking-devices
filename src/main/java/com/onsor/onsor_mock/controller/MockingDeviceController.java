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
    private static final String ACCESS_TOKEN = "YgaoLbYFNU06NFrt8AjE";

    private MqttClient mqttClient;

    private Random random = new Random();

    @Autowired
    public MockingDeviceController() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID, new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(ACCESS_TOKEN);
        mqttClient.connect(options);
    }

    @GetMapping("/energy")
    public void getEnergyData() {
        generateAndSendPieChartData();
    }

    @Scheduled(fixedRate = 5000)  // Send data every 2 seconds
    public void sendEnergyData() {
        generateAndSendPieChartData();
    }

    private void generateAndSendPieChartData() {
        double value1 = random.nextDouble() * 89;

        Map<String, Double> dataMap = new HashMap<>();
        dataMap.put("temperature", value1);

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
    }
}
