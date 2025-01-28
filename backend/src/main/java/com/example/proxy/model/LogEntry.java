package com.example.proxy.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String guid;
    private String direction;
    @Lob
    @Column(columnDefinition = "text")
    private String data;
    private LocalDateTime timestamp;
    private String status;
}
