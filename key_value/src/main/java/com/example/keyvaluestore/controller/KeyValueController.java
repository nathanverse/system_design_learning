package com.example.keyvaluestore.controller;

import com.example.keyvaluestore.model.KeyValueEntry;
import com.example.keyvaluestore.service.KeyValueService;
import com.example.keyvaluestore.storage.NodeStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kv")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:8080"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
public class KeyValueController {

    @Autowired
    private KeyValueService keyValueService;

    @Autowired
    private NodeStorage nodeStorage;

    @PostMapping("/put")
    public ResponseEntity<?> put(@RequestParam String key, @RequestParam String value, @RequestParam long timestamp) {
        try {
            return keyValueService.put(key, value, timestamp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", new java.util.Date().toString());
            errorResponse.put("status", 400);
            errorResponse.put("error", "Bad Request");
            errorResponse.put("message", "Invalid timestamp value: " + e.getMessage());
            errorResponse.put("path", "/api/kv/put");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> get(@RequestParam String key) {
        try {
            return keyValueService.get(key);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", new java.util.Date().toString());
            errorResponse.put("status", 400);
            errorResponse.put("error", "Bad Request");
            errorResponse.put("message", "Invalid key: " + e.getMessage());
            errorResponse.put("path", "/api/kv/get");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam String key) {
        try {
            return keyValueService.delete(key);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", new java.util.Date().toString());
            errorResponse.put("status", 400);
            errorResponse.put("error", "Bad Request");
            errorResponse.put("message", "Invalid key: " + e.getMessage());
            errorResponse.put("path", "/api/kv/delete");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        try {
            List<Map<String, Object>> entries = nodeStorage.getStore().values().stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("key", entry.getKey());
                        map.put("value", entry.getValue());
                        map.put("timestamp", entry.getTimestamp());
                        return map;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", new java.util.Date().toString());
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to list entries: " + e.getMessage());
            errorResponse.put("path", "/api/kv/list");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}