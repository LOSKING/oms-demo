package com.example.oms.service;

import com.example.oms.entity.*;
import com.example.oms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsSimulatorService {

    private final ProductRepository productRepository;
    private final BOMRepository bomRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    private final IntegrationLogRepository integrationLogRepository;
    private final OrderRepository orderRepository;

    /**
     * 重置并初始化模拟数据
     */
    @Transactional
    public void resetData() {
        log.info("开始重置模拟器数据...");

        // 1. 清理原有模拟数据
        integrationLogRepository.deleteAll();
        stockRepository.deleteAll();
        bomRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();

        // 清理模拟器生成的订单，保留原有的系统订单
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            if (!"RETAIL_SALE".equals(order.getOrderType()) || order.getOrderNo().startsWith("SIM-")) {
                orderRepository.delete(order);
            }
        }

        // 强制执行删除，避免 Hibernate 先执行 Insert 导致唯一约束冲突
        integrationLogRepository.flush();
        stockRepository.flush();
        bomRepository.flush();
        productRepository.flush();
        warehouseRepository.flush();
        orderRepository.flush();


        // 2. 初始化仓库
        createWarehouse("WH_SRM_VIRTUAL", "SRM供应商虚拟仓", "LOGICAL");
        createWarehouse("WH_open-OMS_CENTRAL", "open-OMS集团中央仓", "LOGICAL");
        createWarehouse("WH_WMS_SF", "顺丰WMS物理仓", "WMS");
        createWarehouse("WH_POS_STORE", "OMS直营店A仓", "LOCAL_POS");
        createWarehouse("WH_POS_STORE_B", "OMS加盟店B仓", "LOCAL_POS");

        // 3. 初始化商品
        createProduct("P001", "全棉四件套面料", "SINGLE", new BigDecimal("30.00"), "m");
        createProduct("P002", "枕套穿缝绣线", "SINGLE", new BigDecimal("10.00"), "axis");
        createProduct("P003", "拉链及包装辅料", "SINGLE", new BigDecimal("15.00"), "pcs");
        createProduct("P004", "纯棉床上用品四件套(组合装)", "COMPOSITE", new BigDecimal("300.00"), "set");
        createProduct("P005", "智能 5G 手机", "SINGLE", new BigDecimal("3000.00"), "unit");
        createProduct("P006", "潮流休闲卫衣", "SINGLE", new BigDecimal("150.00"), "pcs");

        // 4. 初始化 BOM 关系 (1套四件套 = 2.5m面料 + 0.05轴绣线 + 1.0pcs辅料)
        createBOM("P004", "P001", new BigDecimal("2.5000"));
        createBOM("P004", "P002", new BigDecimal("0.0500"));
        createBOM("P004", "P003", new BigDecimal("1.0000"));

        // 5. 初始化库存
        // 中央仓
        setStock("WH_open-OMS_CENTRAL", "P001", new BigDecimal("500.00"));
        setStock("WH_open-OMS_CENTRAL", "P002", new BigDecimal("200.00"));
        setStock("WH_open-OMS_CENTRAL", "P003", new BigDecimal("150.00"));
        setStock("WH_open-OMS_CENTRAL", "P004", new BigDecimal("0.00"));
        setStock("WH_open-OMS_CENTRAL", "P005", new BigDecimal("1000.00"));
        setStock("WH_open-OMS_CENTRAL", "P006", new BigDecimal("800.00"));

        // WMS 物理仓
        setStock("WH_WMS_SF", "P001", new BigDecimal("1000.00"));
        setStock("WH_WMS_SF", "P002", new BigDecimal("500.00"));
        setStock("WH_WMS_SF", "P003", new BigDecimal("300.00"));
        setStock("WH_WMS_SF", "P004", new BigDecimal("0.00"));
        setStock("WH_WMS_SF", "P005", new BigDecimal("2000.00"));
        setStock("WH_WMS_SF", "P006", new BigDecimal("1500.00"));

        // 门店 A 仓
        setStock("WH_POS_STORE", "P001", new BigDecimal("50.00"));
        setStock("WH_POS_STORE", "P002", new BigDecimal("20.00"));
        setStock("WH_POS_STORE", "P003", new BigDecimal("15.00"));
        setStock("WH_POS_STORE", "P004", new BigDecimal("5.00"));
        setStock("WH_POS_STORE", "P005", new BigDecimal("100.00"));
        setStock("WH_POS_STORE", "P006", new BigDecimal("80.00"));

        // 门店 B 仓
        setStock("WH_POS_STORE_B", "P001", new BigDecimal("10.00"));
        setStock("WH_POS_STORE_B", "P002", new BigDecimal("5.00"));
        setStock("WH_POS_STORE_B", "P003", new BigDecimal("5.00"));
        setStock("WH_POS_STORE_B", "P004", new BigDecimal("2.00"));
        setStock("WH_POS_STORE_B", "P005", new BigDecimal("20.00"));
        setStock("WH_POS_STORE_B", "P006", new BigDecimal("20.00"));

        log.info("模拟器数据初始化成功！");
    }

    private void createWarehouse(String code, String name, String type) {
        Warehouse w = new Warehouse();
        w.setCode(code);
        w.setName(name);
        w.setType(type);
        warehouseRepository.save(w);
    }

    private void createProduct(String code, String name, String type, BigDecimal price, String unit) {
        Product p = new Product();
        p.setCode(code);
        p.setName(name);
        p.setType(type);
        p.setPrice(price);
        p.setUnit(unit);
        productRepository.save(p);
    }

    private void createBOM(String composite, String material, BigDecimal ratio) {
        BOM b = new BOM();
        b.setCompositeProductCode(composite);
        b.setMaterialProductCode(material);
        b.setQuantityRatio(ratio);
        bomRepository.save(b);
    }

    private void setStock(String warehouseCode, String productCode, BigDecimal qty) {
        Stock s = new Stock();
        s.setWarehouseCode(warehouseCode);
        s.setProductCode(productCode);
        s.setQuantity(qty);
        stockRepository.save(s);
    }

    private void logIntegration(int scenarioId, String sender, String receiver, String interfaceName, String payload, String status, String message) {
        IntegrationLog logEntry = new IntegrationLog();
        logEntry.setScenarioId(scenarioId);
        logEntry.setSender(sender);
        logEntry.setReceiver(receiver);
        logEntry.setInterfaceName(interfaceName);
        logEntry.setPayload(payload);
        logEntry.setStatus(status);
        logEntry.setMessage(message);
        integrationLogRepository.save(logEntry);
    }

    /**
     * 执行模拟场景步骤
     */
    @Transactional
    public List<IntegrationLog> runScenarioStep(int scenarioId, int step) {
        List<IntegrationLog> logsBefore = integrationLogRepository.findByScenarioIdOrderByTimestampAsc(scenarioId);
        int currentLogsCount = logsBefore.size();

        switch (scenarioId) {
            case 1:
                runScenario1(step);
                break;
            case 2:
                runScenario2(step);
                break;
            case 3:
                runScenario3(step);
                break;
            case 4:
                runScenario4(step);
                break;
            case 5:
                runScenario5(step);
                break;
            case 6:
                runScenario6(step);
                break;
            default:
                throw new IllegalArgumentException("未知的场景ID: " + scenarioId);
        }

        // 返回该步骤新增的日志
        List<IntegrationLog> logsAfter = integrationLogRepository.findByScenarioIdOrderByTimestampAsc(scenarioId);
        return logsAfter.subList(currentLogsCount, logsAfter.size());
    }

    // ==========================================================
    // 场景 1: B2B 采购直发 WMS 流程
    // ==========================================================
    private void runScenario1(int step) {
        if (step == 1) {
            // open-OMS中新建采购订单 (PO-2026-0001)，向供应商采购智能 5G 手机
            Order po = new Order();
            po.setOrderNo("SIM-PO-2026-0001");
            po.setOrderType("PURCHASE_ORDER");
            po.setCustomerName("中央直采供应商");
            po.setProductName("智能 5G 手机");
            po.setProductCode("P005");
            po.setQuantity(200);
            po.setAmount(new BigDecimal("600000.00"));
            po.setSourceWarehouse("WH_SRM_VIRTUAL");
            po.setTargetWarehouse("WH_WMS_SF");
            po.setStatus(0); // 待确认
            po.setOperator("系统采购员");
            po.setRemark("B2B直采入库WMS仓");
            orderRepository.save(po);

            logIntegration(1, "open-OMS", "SRM", "PO_PUSH_API",
                    "{\"orderNo\": \"SIM-PO-2026-0001\", \"supplier\": \"中央直采供应商\", \"items\": [{\"code\": \"P005\", \"qty\": 200, \"price\": 3000.00}]}",
                    "SUCCESS", "采购订单已推送到SRM系统，等待供应商确认发货");
        } else if (step == 2) {
            // 供应商在SRM确认，并推送采购单 pb 与入库单到 open-OMS
            Optional<Order> poOpt = orderRepository.findByOrderNo("SIM-PO-2026-0001");
            if (poOpt.isPresent()) {
                Order po = poOpt.get();
                po.setStatus(1); // 已确认
                orderRepository.save(po);

                // 生成采购单 (PB-2026-0001)
                Order pb = new Order();
                pb.setOrderNo("SIM-PB-2026-0001");
                pb.setOrderType("PURCHASE_BILL");
                pb.setCustomerName(po.getCustomerName());
                pb.setProductName(po.getProductName());
                pb.setProductCode(po.getProductCode());
                pb.setQuantity(po.getQuantity());
                pb.setAmount(po.getAmount());
                pb.setSourceWarehouse(po.getSourceWarehouse());
                pb.setTargetWarehouse(po.getTargetWarehouse());
                pb.setParentOrderNo(po.getOrderNo());
                pb.setStatus(1);
                pb.setOperator("SRM系统");
                orderRepository.save(pb);

                // 生成入库单并推送到WMS
                Order inbound = new Order();
                inbound.setOrderNo("SIM-IN-2026-0001");
                inbound.setOrderType("INBOUND_ORDER");
                inbound.setCustomerName(po.getCustomerName());
                inbound.setProductName(po.getProductName());
                inbound.setProductCode(po.getProductCode());
                inbound.setQuantity(po.getQuantity());
                inbound.setAmount(po.getAmount());
                inbound.setSourceWarehouse(po.getSourceWarehouse());
                inbound.setTargetWarehouse(po.getTargetWarehouse());
                inbound.setParentOrderNo(pb.getOrderNo());
                inbound.setStatus(0); // 待入库
                inbound.setOperator("open-OMS中台");
                orderRepository.save(inbound);

                logIntegration(1, "SRM", "open-OMS", "PB_DELIVERY_CALLBACK",
                        "{\"purchaseBillNo\": \"SIM-PB-2026-0001\", \"parentPO\": \"SIM-PO-2026-0001\", \"status\": \"DELIVERED\"}",
                        "SUCCESS", "供应商确认发货，中台自动生成采购单(PB)并创建待入库单");

                logIntegration(1, "open-OMS", "WMS", "QIMEN_INBOUND_CREATE_API",
                        "{\"qimenOrderNo\": \"QIMEN-IN-001\", \"omsInboundNo\": \"SIM-IN-2026-0001\", \"warehouse\": \"WH_WMS_SF\", \"items\": [{\"code\": \"P005\", \"qty\": 200}]}",
                        "SUCCESS", "open-OMS通过奇门接口将入库单推送到顺丰WMS仓进行收货操作");
            }
        } else if (step == 3) {
            // WMS收货完成，回传open-OMS
            Optional<Order> inOpt = orderRepository.findByOrderNo("SIM-IN-2026-0001");
            if (inOpt.isPresent()) {
                Order inbound = inOpt.get();
                inbound.setStatus(3); // 已完成
                orderRepository.save(inbound);

                // 更新采购单状态
                Optional<Order> pbOpt = orderRepository.findByOrderNo("SIM-PB-2026-0001");
                if (pbOpt.isPresent()) {
                    Order pb = pbOpt.get();
                    pb.setStatus(3); // 已完成
                    orderRepository.save(pb);
                }

                // 增加WMS仓对应库存
                Optional<Stock> stockOpt = stockRepository.findByWarehouseCodeAndProductCode("WH_WMS_SF", "P005");
                if (stockOpt.isPresent()) {
                    Stock stock = stockOpt.get();
                    stock.setQuantity(stock.getQuantity().add(new BigDecimal("200")));
                    stockRepository.save(stock);
                }

                logIntegration(1, "WMS", "open-OMS", "QIMEN_INBOUND_CONFIRM_CALLBACK",
                        "{\"omsInboundNo\": \"SIM-IN-2026-0001\", \"actualQty\": 200, \"status\": \"COMPLETED\"}",
                        "SUCCESS", "顺丰WMS完成收货上架，向open-OMS回传收货确认。open-OMS自动更新库存(P005智能 5G 手机 +200)且标记采购单完成");
            }
        }
    }

    // ==========================================================
    // 场景 2: 总部采购与财务协同
    // ==========================================================
    private void runScenario2(int step) {
        if (step == 1) {
            // 门店在SRM提报需求 -> 汇总生成总部PO-2026-0002
            Order po = new Order();
            po.setOrderNo("SIM-PO-2026-0002");
            po.setOrderType("PURCHASE_ORDER");
            po.setCustomerName("OMS总部供应链");
            po.setProductName("潮流休闲卫衣");
            po.setProductCode("P006");
            po.setQuantity(100);
            po.setAmount(new BigDecimal("15000.00"));
            po.setSourceWarehouse("WH_SRM_VIRTUAL");
            po.setTargetWarehouse("WH_WMS_SF");
            po.setStatus(0);
            po.setOperator("SRM系统自动汇总");
            po.setRemark("加盟店/直营店销售需求汇总采购");
            orderRepository.save(po);

            logIntegration(2, "SRM", "open-OMS", "SRM_REQUIREMENT_PUSH",
                    "{\"requirementId\": \"REQ-9982\", \"targetPO\": \"SIM-PO-2026-0002\", \"items\": [{\"code\": \"P006\", \"qty\": 100}]}",
                    "SUCCESS", "SRM自动汇总门店采购申请，并向open-OMS中台推送采购订单(PO)");
        } else if (step == 2) {
            // 供应商发货 -> open-OMS生成采购单与入库单并推SF WMS
            Optional<Order> poOpt = orderRepository.findByOrderNo("SIM-PO-2026-0002");
            if (poOpt.isPresent()) {
                Order po = poOpt.get();
                po.setStatus(1);
                orderRepository.save(po);

                Order pb = new Order();
                pb.setOrderNo("SIM-PB-2026-0002");
                pb.setOrderType("PURCHASE_BILL");
                pb.setCustomerName(po.getCustomerName());
                pb.setProductName(po.getProductName());
                pb.setProductCode(po.getProductCode());
                pb.setQuantity(po.getQuantity());
                pb.setAmount(po.getAmount());
                pb.setSourceWarehouse(po.getSourceWarehouse());
                pb.setTargetWarehouse(po.getTargetWarehouse());
                pb.setParentOrderNo(po.getOrderNo());
                pb.setStatus(1);
                pb.setOperator("供应商发货人");
                orderRepository.save(pb);

                Order inbound = new Order();
                inbound.setOrderNo("SIM-IN-2026-0002");
                inbound.setOrderType("INBOUND_ORDER");
                inbound.setCustomerName(po.getCustomerName());
                inbound.setProductName(po.getProductName());
                inbound.setProductCode(po.getProductCode());
                inbound.setQuantity(po.getQuantity());
                inbound.setAmount(po.getAmount());
                inbound.setSourceWarehouse(po.getSourceWarehouse());
                inbound.setTargetWarehouse(po.getTargetWarehouse());
                inbound.setParentOrderNo(pb.getOrderNo());
                inbound.setStatus(0);
                inbound.setOperator("open-OMS系统");
                orderRepository.save(inbound);

                logIntegration(2, "SRM", "open-OMS", "SRM_SHIPPING_BILL_API",
                        "{\"shippingBillNo\": \"SRM-SHIP-002\", \"orderNo\": \"SIM-PO-2026-0002\"}",
                        "SUCCESS", "供应商在SRM系统发货，生成送货单推送中台，中台生成采购单并通知顺丰WMS入库");

                logIntegration(2, "open-OMS", "WMS", "QIMEN_INBOUND_CREATE_API",
                        "{\"omsInboundNo\": \"SIM-IN-2026-0002\", \"warehouse\": \"WH_WMS_SF\"}",
                        "SUCCESS", "open-OMS中台将入库单SIM-IN-2026-0002推送到顺丰WMS系统");
            }
        } else if (step == 3) {
            // WMS入库完成回传 -> open-OMS将入库单回传SRM -> SRM推送财务合同系统创建应付凭证
            Optional<Order> inOpt = orderRepository.findByOrderNo("SIM-IN-2026-0002");
            if (inOpt.isPresent()) {
                Order inbound = inOpt.get();
                inbound.setStatus(3);
                orderRepository.save(inbound);

                Optional<Order> pbOpt = orderRepository.findByOrderNo("SIM-PB-2026-0002");
                if (pbOpt.isPresent()) {
                    Order pb = pbOpt.get();
                    pb.setStatus(3);
                    orderRepository.save(pb);
                }

                // 增加顺丰WMS仓库存
                Optional<Stock> stockOpt = stockRepository.findByWarehouseCodeAndProductCode("WH_WMS_SF", "P006");
                if (stockOpt.isPresent()) {
                    Stock stock = stockOpt.get();
                    stock.setQuantity(stock.getQuantity().add(new BigDecimal("100")));
                    stockRepository.save(stock);
                }

                logIntegration(2, "WMS", "open-OMS", "QIMEN_INBOUND_CALLBACK",
                        "{\"omsInboundNo\": \"SIM-IN-2026-0002\", \"status\": \"COMPLETED\"}",
                        "SUCCESS", "顺丰WMS入库确认，open-OMS中台更新库存(P006潮流休闲卫衣 +100)并归档采购单");

                logIntegration(2, "open-OMS", "SRM", "SRM_INBOUND_STATUS_PUSH",
                        "{\"omsInboundNo\": \"SIM-IN-2026-0002\", \"status\": \"COMPLETED\"}",
                        "SUCCESS", "open-OMS中台将已完成的入库状态同步推送回SRM系统");

                logIntegration(2, "SRM", "FINANCE", "FIN_AP_BILL_CREATE",
                        "{\"apBillNo\": \"AP-2026-0002\", \"contractId\": \"CON-RETAIL-12\", \"amount\": 15000.00}",
                        "SUCCESS", "SRM系统验证入库单无误，将结算单据推送到财务合同系统，自动生成应付款项(AP)");
            }
        }
    }

    // ==========================================================
    // 场景 3: 直营店向总部采购
    // ==========================================================
    private void runScenario3(int step) {
        if (step == 1) {
            // 直营店在SRM下单 -> 推中台生成直营店采购订单 -> 自动生成总部的销售订单 (SO-2026-0003)
            Order po = new Order();
            po.setOrderNo("SIM-CPO-2026-0003");
            po.setOrderType("PURCHASE_ORDER");
            po.setCustomerName("OMS直营店A");
            po.setProductName("纯棉床上用品四件套(组合装)");
            po.setProductCode("P004");
            po.setQuantity(50);
            po.setAmount(new BigDecimal("15000.00"));
            po.setSourceWarehouse("WH_WMS_SF");
            po.setTargetWarehouse("WH_POS_STORE");
            po.setStatus(1); // 自动确认
            po.setOperator("门店下单员");
            orderRepository.save(po);

            // 自动生成总部(供应商角色)销售订单
            Order so = new Order();
            so.setOrderNo("SIM-SO-2026-0003");
            so.setOrderType("SALES_ORDER");
            so.setCustomerName("OMS直营店A");
            so.setProductName("纯棉床上用品四件套(组合装)");
            so.setProductCode("P004");
            so.setQuantity(50);
            so.setAmount(new BigDecimal("15000.00"));
            so.setSourceWarehouse("WH_WMS_SF");
            so.setTargetWarehouse("WH_POS_STORE");
            so.setParentOrderNo(po.getOrderNo());
            so.setStatus(1); // 自动审核
            so.setOperator("open-OMS中台");
            orderRepository.save(so);

            logIntegration(3, "SRM", "open-OMS", "CINEMA_CPO_PUSH",
                    "{\"storeOrderNo\": \"SIM-CPO-2026-0003\", \"store\": \"OMS直营店A\"}",
                    "SUCCESS", "直营店向总部下单，SRM推送采购需求，open-OMS中台自动生成直营店PO，并关联生成总部的销售订单(SO)及自动审核");
        } else if (step == 2) {
            // 审核销售订单 -> 生成销售单与出库单推 WMS (SF)
            Optional<Order> soOpt = orderRepository.findByOrderNo("SIM-SO-2026-0003");
            if (soOpt.isPresent()) {
                Order so = soOpt.get();
                
                Order sb = new Order();
                sb.setOrderNo("SIM-SB-2026-0003");
                sb.setOrderType("SALES_BILL");
                sb.setCustomerName(so.getCustomerName());
                sb.setProductName(so.getProductName());
                sb.setProductCode(so.getProductCode());
                sb.setQuantity(so.getQuantity());
                sb.setAmount(so.getAmount());
                sb.setSourceWarehouse(so.getSourceWarehouse());
                sb.setTargetWarehouse(so.getTargetWarehouse());
                sb.setParentOrderNo(so.getOrderNo());
                sb.setStatus(1);
                sb.setOperator("open-OMS中台");
                orderRepository.save(sb);

                Order outbound = new Order();
                outbound.setOrderNo("SIM-OUT-2026-0003");
                outbound.setOrderType("OUTBOUND_ORDER");
                outbound.setCustomerName(so.getCustomerName());
                outbound.setProductName(so.getProductName());
                outbound.setProductCode(so.getProductCode());
                outbound.setQuantity(so.getQuantity());
                outbound.setAmount(so.getAmount());
                outbound.setSourceWarehouse(so.getSourceWarehouse());
                outbound.setTargetWarehouse(so.getTargetWarehouse());
                outbound.setParentOrderNo(sb.getOrderNo());
                outbound.setStatus(0);
                outbound.setOperator("open-OMS中台");
                orderRepository.save(outbound);

                logIntegration(3, "open-OMS", "WMS", "QIMEN_OUTBOUND_CREATE_API",
                        "{\"omsOutboundNo\": \"SIM-OUT-2026-0003\", \"warehouse\": \"WH_WMS_SF\", \"items\": [{\"code\": \"P004\", \"qty\": 50}]}",
                        "SUCCESS", "总部执行发货，open-OMS中台生成销售单(SB)及出库单(OUT)，并通过奇门接口推送给顺丰WMS配货");
            }
        } else if (step == 3) {
            // WMS 出库确认 -> open-OMS自动生成直营店采购单pb与入库单 -> 直营店POS一键入库 -> 状态完成 -> 财务调拨账单
            Optional<Order> outOpt = orderRepository.findByOrderNo("SIM-OUT-2026-0003");
            if (outOpt.isPresent()) {
                Order outbound = outOpt.get();
                outbound.setStatus(3);
                orderRepository.save(outbound);

                // 更新销售订单和销售单完成
                Optional<Order> soOpt = orderRepository.findByOrderNo("SIM-SO-2026-0003");
                if (soOpt.isPresent()) {
                    Order so = soOpt.get();
                    so.setStatus(3);
                    orderRepository.save(so);
                }

                // open-OMS 扣减 WMS 物理仓库存 (四件套组合装扣减)
                Optional<Stock> sfStockOpt = stockRepository.findByWarehouseCodeAndProductCode("WH_WMS_SF", "P004");
                if (sfStockOpt.isPresent()) {
                    Stock sfStock = sfStockOpt.get();
                    sfStock.setQuantity(sfStock.getQuantity().subtract(new BigDecimal("50")));
                    stockRepository.save(sfStock);
                }

                // 自动生成直营店采购单与入库单
                Order pb = new Order();
                pb.setOrderNo("SIM-PB-2026-0003");
                pb.setOrderType("PURCHASE_BILL");
                pb.setCustomerName("OMS直营店A");
                pb.setProductName("纯棉床上用品四件套(组合装)");
                pb.setProductCode("P004");
                pb.setQuantity(50);
                pb.setAmount(new BigDecimal("15000.00"));
                pb.setSourceWarehouse("WH_WMS_SF");
                pb.setTargetWarehouse("WH_POS_STORE");
                pb.setParentOrderNo("SIM-CPO-2026-0003");
                pb.setStatus(3); // 完成
                orderRepository.save(pb);

                Order inbound = new Order();
                inbound.setOrderNo("SIM-IN-2026-0003");
                inbound.setOrderType("INBOUND_ORDER");
                inbound.setCustomerName("OMS直营店A");
                inbound.setProductName("纯棉床上用品四件套(组合装)");
                inbound.setProductCode("P004");
                inbound.setQuantity(50);
                inbound.setAmount(new BigDecimal("15000.00"));
                inbound.setSourceWarehouse("WH_WMS_SF");
                inbound.setTargetWarehouse("WH_POS_STORE");
                inbound.setParentOrderNo(pb.getOrderNo());
                inbound.setStatus(3); // 直营店在open-OMSPOS完成入库
                orderRepository.save(inbound);

                // 直营店POS门店仓入库增加
                Optional<Stock> storeStockOpt = stockRepository.findByWarehouseCodeAndProductCode("WH_POS_STORE", "P004");
                if (storeStockOpt.isPresent()) {
                    Stock storeStock = storeStockOpt.get();
                    storeStock.setQuantity(storeStock.getQuantity().add(new BigDecimal("50")));
                    stockRepository.save(storeStock);
                }

                logIntegration(3, "WMS", "open-OMS", "QIMEN_OUTBOUND_CONFIRM_CALLBACK",
                        "{\"omsOutboundNo\": \"SIM-OUT-2026-0003\", \"actualQty\": 50}",
                        "SUCCESS", "顺丰WMS出库回传，open-OMS扣减WMS库存，并自动生成直营店采购单(PB)和入库单(IN)");

                logIntegration(3, "POS", "open-OMS", "STORE_POS_INBOUND",
                        "{\"omsInboundNo\": \"SIM-IN-2026-0003\", \"warehouse\": \"WH_POS_STORE\"}",
                        "SUCCESS", "直营店在open-OMSPOS终端一键入库，系统增加门店零售库存(P004四件套 +50)");

                logIntegration(3, "open-OMS", "FINANCE", "MERCHANDISE_TRANSFER_PUSH",
                        "{\"billNo\": \"FIN-TO-2026-003\", \"type\": \"STORE_BUY_HEADQUARTERS\", \"amount\": 15000.00}",
                        "SUCCESS", "中台推送结算确认，生成商品调拨单，送财务合同系统结算，完成交易闭环");
            }
        }
    }

    // ==========================================================
    // 场景 4: 门店之间调拨
    // ==========================================================
    private void runScenario4(int step) {
        if (step == 1) {
            // 调出店在 POS 新建调拨订单 TO-2026-0004，自动生成协同订单与出库单
            Order to = new Order();
            to.setOrderNo("SIM-TO-2026-0004");
            to.setOrderType("TRANSFER_ORDER");
            to.setCustomerName("时光零售(加盟店B)");
            to.setProductName("智能 5G 手机");
            to.setProductCode("P005");
            to.setQuantity(10);
            to.setAmount(new BigDecimal("30000.00"));
            to.setSourceWarehouse("WH_POS_STORE");
            to.setTargetWarehouse("WH_POS_STORE_B");
            to.setStatus(1); // 自动确认
            to.setOperator("A店调拨员");
            orderRepository.save(to);

            // 自动生成协同订单 CO-2026-0004
            Order co = new Order();
            co.setOrderNo("SIM-CO-2026-0004");
            co.setOrderType("COLLABORATIVE_ORDER");
            co.setCustomerName("时光零售(加盟店B)");
            co.setProductName("智能 5G 手机");
            co.setProductCode("P005");
            co.setQuantity(10);
            co.setAmount(new BigDecimal("30000.00"));
            co.setSourceWarehouse("WH_POS_STORE");
            co.setTargetWarehouse("WH_POS_STORE_B");
            co.setParentOrderNo(to.getOrderNo());
            co.setStatus(1); // 审核状态
            co.setOperator("open-OMS中台自动协同");
            orderRepository.save(co);

            // 生成出库单并扣减调出仓库存
            Order outbound = new Order();
            outbound.setOrderNo("SIM-OUT-2026-0004");
            outbound.setOrderType("OUTBOUND_ORDER");
            outbound.setCustomerName("时光零售(加盟店B)");
            outbound.setProductName("智能 5G 手机");
            outbound.setProductCode("P005");
            outbound.setQuantity(10);
            outbound.setAmount(new BigDecimal("30000.00"));
            outbound.setSourceWarehouse("WH_POS_STORE");
            outbound.setTargetWarehouse("WH_POS_STORE_B");
            outbound.setParentOrderNo(to.getOrderNo());
            outbound.setStatus(3); // 门店仓调拨：调出即出库完成
            outbound.setOperator("POS系统");
            orderRepository.save(outbound);

            // A门店扣库存
            Optional<Stock> stockOpt = stockRepository.findByWarehouseCodeAndProductCode("WH_POS_STORE", "P005");
            if (stockOpt.isPresent()) {
                Stock stock = stockOpt.get();
                stock.setQuantity(stock.getQuantity().subtract(new BigDecimal("10")));
                stockRepository.save(stock);
            }

            logIntegration(4, "POS", "open-OMS", "TRANSFER_OUT_CREATE",
                    "{\"transferOrderNo\": \"SIM-TO-2026-0004\", \"from\": \"WH_POS_STORE\", \"to\": \"WH_POS_STORE_B\"}",
                    "SUCCESS", "直营店A在open-OMSPOS端提交调拨单，中台响应并生成对应的协同订单(CO-2026-0004)与出库单。扣除直营店A库存(P005智能 5G 手机 -10)");
        } else if (step == 2) {
            // open-OMS 将调拨出库单推送到财务合同系统的商品调拨单
            logIntegration(4, "open-OMS", "FINANCE", "FIN_TRANSFER_BILL_PUSH",
                    "{\"billNo\": \"FIN-TO-2026-004\", \"type\": \"OUT_POST\", \"amount\": 30000.00, \"status\": \"OUT_COMPLETED\"}",
                    "SUCCESS", "open-OMS中台将完成的调拨出库信息推送给财务合同系统的‘商品调拨单’，进行财务账目预记");
        } else if (step == 3) {
            // 调入店 B POS 一键入库，完成所有单据，并增加 B 仓库存，并推财务确认
            Optional<Order> coOpt = orderRepository.findByOrderNo("SIM-CO-2026-0004");
            if (coOpt.isPresent()) {
                Order co = coOpt.get();
                co.setStatus(3); // 完成
                orderRepository.save(co);

                // 生成入库单
                Order inbound = new Order();
                inbound.setOrderNo("SIM-IN-2026-0004");
                inbound.setOrderType("INBOUND_ORDER");
                inbound.setCustomerName("时光零售(加盟店B)");
                inbound.setProductName("智能 5G 手机");
                inbound.setProductCode("P005");
                inbound.setQuantity(10);
                inbound.setAmount(new BigDecimal("30000.00"));
                inbound.setSourceWarehouse("WH_POS_STORE");
                inbound.setTargetWarehouse("WH_POS_STORE_B");
                inbound.setParentOrderNo(co.getOrderNo());
                inbound.setStatus(3); // 入库完成
                inbound.setOperator("B店收货员");
                orderRepository.save(inbound);

                // B门店加库存
                Optional<Stock> stockOpt = stockRepository.findByWarehouseCodeAndProductCode("WH_POS_STORE_B", "P005");
                if (stockOpt.isPresent()) {
                    Stock stock = stockOpt.get();
                    stock.setQuantity(stock.getQuantity().add(new BigDecimal("10")));
                    stockRepository.save(stock);
                }

                logIntegration(4, "POS", "open-OMS", "TRANSFER_IN_CONFIRM",
                        "{\"omsInboundNo\": \"SIM-IN-2026-0004\", \"warehouse\": \"WH_POS_STORE_B\"}",
                        "SUCCESS", "加盟店B收到货，在open-OMSPOS操作‘一键入库’。open-OMS中台同步完成入库单和协同订单。增加加盟店B库存(P005智能 5G 手机 +10)");

                logIntegration(4, "open-OMS", "FINANCE", "FIN_TRANSFER_BILL_COMPLETE",
                        "{\"billNo\": \"FIN-TO-2026-004\", \"type\": \"IN_POST\", \"status\": \"CLOSED\"}",
                        "SUCCESS", "open-OMS中台将调拨入库完成的状态同步推送给财务系统，关闭该商品调拨结算流程");
            }
        }
    }

    // ==========================================================
    // 场景 5: 线上自提与退款 (BOM 拆解与复原)
    // ==========================================================
    private void runScenario5(int step) {
        if (step == 1) {
            // 线上平台支付订单 SP-2026-0005，买四件套，推送open-OMS自提订单中（审核状态）
            Order sp = new Order();
            sp.setOrderNo("SIM-SP-2026-0005");
            sp.setOrderType("SELF_PICKUP_ORDER");
            sp.setCustomerName("美团线上用户");
            sp.setProductName("纯棉床上用品四件套(组合装)");
            sp.setProductCode("P004");
            sp.setQuantity(5);
            sp.setAmount(new BigDecimal("1500.00"));
            sp.setSourceWarehouse("WH_POS_STORE"); // 取货地点
            sp.setStatus(1); // 审核状态
            sp.setOperator("核心小程序");
            sp.setRemark("美团APP已付款订单");
            orderRepository.save(sp);

            logIntegration(5, "CORE", "open-OMS", "SELF_PICKUP_ORDER_PUSH",
                    "{\"orderNo\": \"SIM-SP-2026-0005\", \"platform\": \"MEITUAN\", \"status\": \"PAID\", \"item\": \"P004\", \"qty\": 5}",
                    "SUCCESS", "美团/自营商城小程序等自营自渠系统，将已付款自提订单推送到open-OMS中台自提订单池，状态置为‘已审核’，等待到店提取");
        } else if (step == 2) {
            // 门店 POS 扫码核销，前台取货 -> 产生小票零售订单 RR-2026-0005
            // 触发 BOM 拆解引擎：由于 P004 是复合品，需要把它的原材料扣库存
            Optional<Order> spOpt = orderRepository.findByOrderNo("SIM-SP-2026-0005");
            if (spOpt.isPresent()) {
                Order sp = spOpt.get();
                sp.setStatus(3); // 已完成
                orderRepository.save(sp);

                // 创建零售小票
                Order rr = new Order();
                rr.setOrderNo("SIM-RR-2026-0005");
                rr.setOrderType("RETAIL_SALE");
                rr.setCustomerName(sp.getCustomerName());
                rr.setProductName(sp.getProductName());
                rr.setProductCode(sp.getProductCode());
                rr.setQuantity(sp.getQuantity());
                rr.setAmount(sp.getAmount());
                rr.setSourceWarehouse(sp.getSourceWarehouse());
                rr.setParentOrderNo(sp.getOrderNo());
                rr.setStatus(3);
                rr.setOperator("POS扫码收银员");
                orderRepository.save(rr);

                // 创建组装拆卸单（用于扣除BOM原材料库存）
                Order ad = new Order();
                ad.setOrderNo("SIM-AD-2026-0005");
                ad.setOrderType("COLLABORATIVE_ORDER"); // 借用作为组装拆卸单
                ad.setCustomerName("BOM引擎组装单");
                ad.setProductName("原材料组装扣减");
                ad.setProductCode("P004");
                ad.setQuantity(sp.getQuantity());
                ad.setAmount(sp.getAmount());
                ad.setSourceWarehouse(sp.getSourceWarehouse());
                ad.setParentOrderNo(rr.getOrderNo());
                ad.setStatus(3);
                ad.setOperator("BOM引擎");
                ad.setRemark("BOM类型: 组装(扣减原材料)");
                orderRepository.save(ad);

                // BOM 扣除库存
                // P004四件套 x 5 -> 面料 12.5m, 绣线 0.25轴, 辅料 5.0pcs
                List<BOM> bomList = bomRepository.findByCompositeProductCode("P004");
                for (BOM bom : bomList) {
                    BigDecimal totalDeduct = bom.getQuantityRatio().multiply(new BigDecimal(sp.getQuantity()));
                    Optional<Stock> stockOpt = stockRepository.findByWarehouseCodeAndProductCode(sp.getSourceWarehouse(), bom.getMaterialProductCode());
                    if (stockOpt.isPresent()) {
                        Stock stock = stockOpt.get();
                        stock.setQuantity(stock.getQuantity().subtract(totalDeduct));
                        stockRepository.save(stock);
                    }
                }

                logIntegration(5, "POS", "open-OMS", "SELF_PICKUP_CONFIRM",
                        "{\"orderNo\": \"SIM-SP-2026-0005\", \"pickupCode\": \"998827\"}",
                        "SUCCESS", "顾客到店展示核销码，门店POS确认核销，推送中台已核销状态并自动生成零售小票");

                logIntegration(5, "open-OMS", "POS", "BOM_DISASSEMBLY_DEDUCT",
                        "{\"assemblyNo\": \"SIM-AD-2026-0005\", \"composite\": \"P004\", \"qty\": 5, \"rawDeduction\": {\"P001(面料)\": 12.5, \"P002(绣线)\": 0.25, \"P003(辅料)\": 5.0}}",
                        "SUCCESS", "【BOM拆解引擎】识别出P004四件套组合装为组装商品。系统自动创建组装单并扣减原材料库存，不直接扣减四件套成品(门店根据销售配方随用随产)");
            }
        } else if (step == 3) {
            // 用户在小程序发起退货 -> 生成零售退单 RET-2026-0005 -> 触发 BOM 逆向复原：原材料加回
            Optional<Order> rrOpt = orderRepository.findByOrderNo("SIM-RR-2026-0005");
            if (rrOpt.isPresent()) {
                Order rr = rrOpt.get();
                rr.setStatus(4); // 作废
                orderRepository.save(rr);

                // 生成退货单
                Order ret = new Order();
                ret.setOrderNo("SIM-RET-2026-0005");
                ret.setOrderType("RETAIL_SALE");
                ret.setCustomerName(rr.getCustomerName());
                ret.setProductName("退货: " + rr.getProductName());
                ret.setProductCode(rr.getProductCode());
                ret.setQuantity(rr.getQuantity());
                ret.setAmount(rr.getAmount().negate()); // 负数金额
                ret.setSourceWarehouse(rr.getSourceWarehouse());
                ret.setParentOrderNo(rr.getOrderNo());
                ret.setStatus(3);
                ret.setOperator("线上自渠客服");
                orderRepository.save(ret);

                // 创建退货组装拆卸单（拆卸，原材料加回）
                Order adRet = new Order();
                adRet.setOrderNo("SIM-AD-RET-2026-0005");
                adRet.setOrderType("COLLABORATIVE_ORDER");
                adRet.setCustomerName("BOM引擎退货拆卸单");
                adRet.setProductName("原材料拆卸回加");
                adRet.setProductCode("P004");
                adRet.setQuantity(rr.getQuantity());
                adRet.setAmount(rr.getAmount());
                adRet.setSourceWarehouse(rr.getSourceWarehouse());
                adRet.setParentOrderNo(ret.getOrderNo());
                adRet.setStatus(3);
                adRet.setOperator("BOM引擎");
                adRet.setRemark("BOM类型: 拆卸(恢复原材料)");
                orderRepository.save(adRet);

                // BOM 原材料加回
                List<BOM> bomList = bomRepository.findByCompositeProductCode("P004");
                for (BOM bom : bomList) {
                    BigDecimal totalAdd = bom.getQuantityRatio().multiply(new BigDecimal(rr.getQuantity()));
                    Optional<Stock> stockOpt = stockRepository.findByWarehouseCodeAndProductCode(rr.getSourceWarehouse(), bom.getMaterialProductCode());
                    if (stockOpt.isPresent()) {
                        Stock stock = stockOpt.get();
                        stock.setQuantity(stock.getQuantity().add(totalAdd));
                        stockRepository.save(stock);
                    }
                }

                logIntegration(5, "CORE", "open-OMS", "ONLINE_REFUND_REQUEST",
                        "{\"originalOrderNo\": \"SIM-SP-2026-0005\", \"refundNo\": \"SIM-REF-005\", \"amount\": 1500.00}",
                        "SUCCESS", "美团线上渠道提交退款申请，中台自动作废对应销售，并生成零售退单(SIM-RET-2026-0005)");

                logIntegration(5, "open-OMS", "POS", "BOM_REASSEMBLY_RESTORE",
                        "{\"disassemblyNo\": \"SIM-AD-RET-2026-0005\", \"composite\": \"P004\", \"qty\": 5, \"rawRestoration\": {\"P001(面料)\": 12.5, \"P002(绣线)\": 0.25, \"P003(辅料)\": 5.0}}",
                        "SUCCESS", "【BOM复原引擎】系统启动逆向拆卸机制，将P004包含的原材料消耗量全部退回到门店零售仓仓库(P001 +12.5m, P002 +0.25轴, P003 +5.0pcs)");
            }
        }
    }

    // ==========================================================
    // 场景 6: 智能盘点业务
    // ==========================================================
    private void runScenario6(int step) {
        if (step == 1) {
            // 门店 POS 新建盘点单，过滤掉组合品（组合品四件套无库存，以原材料实物盘点为准）与 0 库存商品
            Order st = new Order();
            st.setOrderNo("SIM-ST-2026-0006");
            st.setOrderType("TRANSFER_ORDER"); // 借用
            st.setCustomerName("智能盘点任务-月盘");
            st.setProductName("实物库存盘点");
            st.setProductCode("P001,P002,P003,P005,P006");
            st.setQuantity(5); // 5个商品进行盘点
            st.setAmount(new BigDecimal("0.00"));
            st.setSourceWarehouse("WH_POS_STORE");
            st.setStatus(0); // 盘点中
            st.setOperator("店长张三");
            st.setRemark("过滤掉复合品四件套(P004)");
            orderRepository.save(st);

            logIntegration(6, "POS", "open-OMS", "STOCK_COUNT_START",
                    "{\"countNo\": \"SIM-ST-2026-0006\", \"scope\": \"MONTHLY_FULL_COUNT\", \"warehouse\": \"WH_POS_STORE\"}",
                    "SUCCESS", "门店店长发起月度盘点，中台过滤剔除组合商品(P004四件套无库存)及0库存物料，下发需盘点商品明细");
        } else if (step == 2) {
            // 输入实盘数并保存，计算预盈亏
            // 书面数: P001: 50, P003: 15
            // 实盘数: P001: 48 (亏), P003: 16 (盈)
            logIntegration(6, "POS", "open-OMS", "STOCK_COUNT_SAVE",
                    "{\"countNo\": \"SIM-ST-2026-0006\", \"countedItems\": [{\"code\": \"P001\", \"bookQty\": 50, \"physicalQty\": 48}, {\"code\": \"P003\", \"bookQty\": 15, \"physicalQty\": 16}]}",
                    "SUCCESS", "员工录入盘点数据并保存。中台实时计算差异盈亏：全棉四件套面料P001盈亏 -2m(盘亏)，拉链及包装辅料P003盈亏 +1pcs(盘盈)");
        } else if (step == 3) {
            // 审核完成 -> 调整库存 -> 产生盘盈盘亏单
            Optional<Order> stOpt = orderRepository.findByOrderNo("SIM-ST-2026-0006");
            if (stOpt.isPresent()) {
                Order st = stOpt.get();
                st.setStatus(3); // 完成
                orderRepository.save(st);

                // 产生盘盈盘亏单 (IVB-2026-0006)
                Order ivb = new Order();
                ivb.setOrderNo("SIM-IVB-2026-0006");
                ivb.setOrderType("TRANSFER_ORDER"); // 借用作为差异调整单
                ivb.setCustomerName("智能盘点-差异调整");
                ivb.setProductName("盘亏: P001(-2.0), 盘盈: P003(+1.0)");
                ivb.setProductCode("P001,P003");
                ivb.setQuantity(2);
                ivb.setAmount(new BigDecimal("-45.00")); // -2*30 + 1*15 = -45
                ivb.setSourceWarehouse("WH_POS_STORE");
                ivb.setStatus(3);
                ivb.setOperator("系统自动生成");
                orderRepository.save(ivb);

                // 更新库存
                // P001: 50 -> 48
                Optional<Stock> stockP001Opt = stockRepository.findByWarehouseCodeAndProductCode("WH_POS_STORE", "P001");
                if (stockP001Opt.isPresent()) {
                    Stock stock = stockP001Opt.get();
                    stock.setQuantity(new BigDecimal("48.00"));
                    stockRepository.save(stock);
                }
                // P003: 15 -> 16
                Optional<Stock> stockP003Opt = stockRepository.findByWarehouseCodeAndProductCode("WH_POS_STORE", "P003");
                if (stockP003Opt.isPresent()) {
                    Stock stock = stockP003Opt.get();
                    stock.setQuantity(new BigDecimal("16.00"));
                    stockRepository.save(stock);
                }

                logIntegration(6, "POS", "open-OMS", "STOCK_COUNT_APPROVE",
                        "{\"countNo\": \"SIM-ST-2026-0006\", \"approvedBy\": \"MGR-1002\"}",
                        "SUCCESS", "店长确认盘点无误提交审核。open-OMS中台自动审核，调整门店实物仓库存(P001置为48, P003置为16)，并自动生成盘盈盘亏报表账单");
            }
        }
    }
}
