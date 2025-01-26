package com.example.proxy.controller;

import com.example.proxy.service.ChromeLauncherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BrowserController {

    private final ChromeLauncherService chromeLauncher;

    @GetMapping("/open-browser")
    public String openBrowser(@RequestParam String url) {
        try {
            chromeLauncher.launchChromeWithProxy(url);
            return "Tarayıcı başarıyla başlatıldı ve URL yüklendi: " + url;
        } catch (Exception e) {
            e.printStackTrace();
            return "Tarayıcı başlatılamadı: " + e.getMessage();
        }
    }
}

