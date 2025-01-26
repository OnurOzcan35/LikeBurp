package com.example.proxy.model;

import lombok.*;
import org.springframework.http.HttpHeaders;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProxyResponse {
    private int statusCode;       // HTTP durum kodu
    private HttpHeaders headers;  // Yanıt başlıkları
    private String body;          // Yanıt içeriği
}

