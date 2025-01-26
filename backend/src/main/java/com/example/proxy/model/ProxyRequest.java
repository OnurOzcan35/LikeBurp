package com.example.proxy.model;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProxyRequest {
    private String method; // GET, POST, PUT, DELETE
    private String url;    // Hedef URL
    private Map<String, String> headers; // Başlıklar
    private String body;   // İstek içeriği
}
