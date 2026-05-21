package com.example.oms.repository;

import com.example.oms.entity.BOM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BOMRepository extends JpaRepository<BOM, Long> {
    List<BOM> findByCompositeProductCode(String compositeProductCode);
}
