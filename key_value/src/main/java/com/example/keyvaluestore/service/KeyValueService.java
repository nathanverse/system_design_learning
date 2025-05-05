package com.example.keyvaluestore.service;

import com.example.keyvaluestore.config.NodeConfig;
import com.example.keyvaluestore.model.KeyValueEntry;
import com.example.keyvaluestore.storage.NodeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KeyValueService {
    private static final Logger logger = LoggerFactory.getLogger(KeyValueService.class);

    @Autowired
    private NodeStorage nodeStorage;

    @Autowired
    private NodeConfig nodeConfig;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> put(String key, String value, long timestamp) {
        int targetNodeId = computeShardId(key);
        logger.info("Processing put for key {} on node {}", key, targetNodeId);
        if (targetNodeId == nodeConfig.getId()) {
            KeyValueEntry existing = nodeStorage.get(key);
            if (existing == null || existing.getTimestamp() <= timestamp) {
                nodeStorage.put(key, value, timestamp);
                logger.info("Stored key {} on node {}", key, targetNodeId);
                return ResponseEntity.ok("Stored successfully on node " + targetNodeId);
            }
            logger.warn("Rejected put for key {} due to older timestamp {}", key, timestamp);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Older timestamp rejected");
        } else {
            NodeConfig.NodeInfo targetNode = findNodeById(targetNodeId);
            String url = String.format("http://%s:%d/api/kv/put?key=%s&value=%s&timestamp=%d",
                    targetNode.getHost(), targetNode.getPort(), key, value, timestamp);
            logger.info("Forwarding put for key {} to node {} at {}", key, targetNodeId, url);
            return restTemplate.postForEntity(url, null, String.class);
        }
    }

    public ResponseEntity<String> get(String key) {
        int targetNodeId = computeShardId(key);
        logger.info("Processing get for key {} on node {}", key, targetNodeId);
        if (targetNodeId == nodeConfig.getId()) {
            KeyValueEntry entry = nodeStorage.get(key);
            if (entry != null) {
                logger.info("Retrieved key {} from node {}", key, targetNodeId);
                return ResponseEntity.ok(entry.getValue());
            }
            logger.warn("Key {} not found on node {}", key, targetNodeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found");
        } else {
            NodeConfig.NodeInfo targetNode = findNodeById(targetNodeId);
            String url = String.format("http://%s:%d/api/kv/get?key=%s",
                    targetNode.getHost(), targetNode.getPort(), key);
            logger.info("Forwarding get for key {} to node {} at {}", key, targetNodeId, url);
            return restTemplate.getForEntity(url, String.class);
        }
    }

    public ResponseEntity<String> delete(String key) {
        int targetNodeId = computeShardId(key);
        logger.info("Processing delete for key {} on node {}", key, targetNodeId);
        if (targetNodeId == nodeConfig.getId()) {
            boolean deleted = nodeStorage.delete(key);
            if (deleted) {
                logger.info("Deleted key {} from node {}", key, targetNodeId);
                return ResponseEntity.ok("Deleted successfully");
            }
            logger.warn("Key {} not found on node {}", key, targetNodeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Key not found");
        } else {
            NodeConfig.NodeInfo targetNode = findNodeById(targetNodeId);
            String url = String.format("http://%s:%d/api/kv/delete?key=%s",
                    targetNode.getHost(), targetNode.getPort(), key);
            logger.info("Forwarding delete for key {} to node {} at {}", key, targetNodeId, url);
            restTemplate.delete(url);
            return ResponseEntity.ok("Deleted successfully");
        }
    }

    private int computeShardId(String key) {
        return Math.abs(key.hashCode() % nodeConfig.getNodes().size());
    }

    private NodeConfig.NodeInfo findNodeById(int id) {
        return nodeConfig.getNodes().stream()
                .filter(node -> node.getId() == id)
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Node {} not found", id);
                    return new RuntimeException("Node not found");
                });
    }
}