package com.example.oms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_integration_log")
public class IntegrationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scenario_id")
    private Integer scenarioId;

    @Column(name = "sender", nullable = false, length = 32)
    private String sender; // e.g. "SRM", "open-OMS", "WMS", "POS", "FINANCE", "CORE"

    @Column(name = "receiver", nullable = false, length = 32)
    private String receiver;

    @Column(name = "interface_name", nullable = false, length = 128)
    private String interfaceName;

    @Column(name = "payload", length = 2048)
    private String payload; // JSON format payload

    @Column(name = "status", nullable = false, length = 16)
    private String status; // "SUCCESS", "ERROR"

    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
