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

import java.util.Random;

@RestController
@RequestMapping("/api/devices")
public class EnergyMeterController {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "spring-boot-client";
    private static final String TOPIC = "v1/devices/me/telemetry";
    private static final String ACCESS_TOKEN = "fI5uqO0XEVDMrJcS7tqI";

    private MqttClient mqttClient;

    private Random random = new Random();

    @Autowired
    public EnergyMeterController() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(ACCESS_TOKEN);
        mqttClient.connect(options);
    }

    @GetMapping("/energy")
    public TemperatureReading getTemperature() {
        return generateAndSendTemperature();
    }

    @Scheduled(fixedRate = 2000)  // Send data every 3 seconds
    public void sendTemperatureData() {
        generateAndSendTemperature();
    }

    private TemperatureReading generateAndSendTemperature() {
        // Simulate a random temperature reading between -10 and 40 degrees Celsius
        double temperature = 50 * random.nextDouble();
        TemperatureReading temperatureReading = new TemperatureReading(temperature);

        // Prepare JSON payload
        String payload = String.format("{\"energy\": %f}", temperature);

        // Publish the temperature reading to the MQTT topic
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);

        try {
            mqttClient.publish(TOPIC, message);
            System.out.println("Published: " + payload);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return temperatureReading;
    }

    static class TemperatureReading {
        private double temperature;

        public TemperatureReading(double temperature) {
            this.temperature = temperature;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }
}
