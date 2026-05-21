package com.example.oms.controller;

import com.example.oms.entity.*;
import com.example.oms.repository.*;
import com.example.oms.service.OmsSimulatorService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class OmsSimulatorController {

    private final OmsSimulatorService omsSimulatorService;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final IntegrationLogRepository integrationLogRepository;
    private final OrderRepository orderRepository;

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetData() {
        log.info("API 收到重置模拟器数据请求");
        omsSimulatorService.resetData();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "模拟器数据与库存已全部重置为初始状态");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runStep(@RequestBody SimulationRequest request) {
        log.info("API 运行模拟步骤: scenarioId={}, step={}", request.getScenarioId(), request.getStep());
        List<IntegrationLog> newLogs = omsSimulatorService.runScenarioStep(request.getScenarioId(), request.getStep());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", newLogs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getLogs(@RequestParam(required = false) Integer scenarioId) {
        List<IntegrationLog> logs;
        if (scenarioId != null) {
            logs = integrationLogRepository.findByScenarioIdOrderByTimestampAsc(scenarioId);
        } else {
            logs = integrationLogRepository.findTop50ByOrderByTimestampDesc();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", logs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stocks")
    public ResponseEntity<Map<String, Object>> getStocks() {
        List<Stock> stocks = stockRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stocks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts() {
        List<Product> products = productRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/warehouses")
    public ResponseEntity<Map<String, Object>> getWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", warehouses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders() {
        List<Order> orders = orderRepository.findTop10ByOrderByCreatedAtDesc();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", orders);
        return ResponseEntity.ok(response);
    }

    @Data
    public static class SimulationRequest {
        private Integer scenarioId;
        private Integer step;
    }
}
