package com.example.oms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_bom")
public class BOM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "composite_product_code", nullable = false, length = 32)
    private String compositeProductCode;

    @Column(name = "material_product_code", nullable = false, length = 32)
    private String materialProductCode;

    @Column(name = "quantity_ratio", nullable = false, precision = 12, scale = 4)
    private BigDecimal quantityRatio;
}
