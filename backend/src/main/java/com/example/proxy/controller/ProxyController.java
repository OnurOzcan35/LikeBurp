package com.example.proxy.controller;

import com.example.proxy.service.ProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/proxy")
@RequiredArgsConstructor
public class ProxyController {

    private final ProxyService proxyService;


    @PostMapping("/start")
    public String startProxy(@RequestParam(defaultValue = "8080") int port) {
        try {
            proxyService.startProxy(port);
            return "Proxy server başlatıldı ve " + port + " portunda dinliyor.";
        } catch (IOException e) {
            return "Proxy başlatılırken hata oluştu: " + e.getMessage();
        }
    }

    @PostMapping("/stop")
    public String stopProxy() {
        try {
            proxyService.stopProxy();
            return "Proxy server durduruldu.";
        } catch (IOException e) {
            return "Proxy durdurulurken hata oluştu: " + e.getMessage();
        }
    }
}