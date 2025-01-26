package com.example.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyResponseLog {
    private String timestamp;
    private int statusCode;
    private Map<String, String> headers;
    private String body;
}

