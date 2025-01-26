package com.example.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyRequestLog {
    private long timestamp;
    private String method;
    private String url;
    private Map<String, String> headers;
    private String body;
}
