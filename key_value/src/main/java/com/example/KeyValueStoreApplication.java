package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.net.BindException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class KeyValueStoreApplication {
    private static final Logger logger = LoggerFactory.getLogger(KeyValueStoreApplication.class);

    public static void main(String[] args) {
        // Define node configurations
        List<NodeConfig> nodeConfigs = Arrays.asList(
                new NodeConfig(0, 8080),
                new NodeConfig(1, 8081),
                new NodeConfig(2, 8082)
        );

        // Check port availability before starting nodes
        for (NodeConfig config : nodeConfigs) {
            if (!isPortAvailable(config.port)) {
                logger.error("Port {} is already in use. Cannot start node {}. Please free the port or change the configuration.", config.port, config.id);
                System.exit(1);
            }
        }

        // Start each node in a separate thread with a delay
        for (NodeConfig config : nodeConfigs) {
            new Thread(() -> {
                try {
                    logger.info("Starting node {} on port {}", config.id, config.port);
                    new SpringApplicationBuilder(KeyValueStoreApplication.class)
                            .properties(
                                    "server.port=" + config.port,
                                    "node.id=" + config.id,
                                    "node.port=" + config.port,
                                    "node.nodes[0].id=0",
                                    "node.nodes[0].host=localhost",
                                    "node.nodes[0].port=8080",
                                    "node.nodes[1].id=1",
                                    "node.nodes[1].host=localhost",
                                    "node.nodes[1].port=8081",
                                    "node.nodes[2].id=2",
                                    "node.nodes[2].host=localhost",
                                    "node.nodes[2].port=8082"
                            )
                            .run(args);
                    logger.info("Node {} started on port {}", config.id, config.port);
                } catch (Exception e) {
                    logger.error("Failed to start node {} on port {}: {}", config.id, config.port, e.getMessage(), e);
                    System.exit(1);
                }
            }).start();
            try {
                Thread.sleep(1000); // Delay to avoid race conditions
            } catch (InterruptedException e) {
                logger.error("Interrupted while starting node {}", config.id, e);
                System.exit(1);
            }
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Temporary class for node configuration
    private static class NodeConfig {
        int id;
        int port;

        NodeConfig(int id, int port) {
            this.id = id;
            this.port = port;
        }
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (BindException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking port {} availability: {}", port, e.getMessage(), e);
            return false;
        }
    }
}