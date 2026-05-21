package com.example.oms.repository;

import com.example.oms.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByWarehouseCodeAndProductCode(String warehouseCode, String productCode);
    List<Stock> findByWarehouseCode(String warehouseCode);
}
