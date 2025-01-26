package com.example.proxy.service;

import com.example.proxy.config.RootCertificateCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class ChromeLauncherService {

    private final RootCertificateCreator rootCertificateCreator;

    public void launchChromeWithProxy(String targetUrl) throws Exception {

        String rootFingerPrint = rootCertificateCreator.getRootFingerPrint();

        String chromePath = new File("src/main/resources/chrome/chrome.exe").getAbsolutePath();

        ProcessBuilder processBuilder = new ProcessBuilder(
                chromePath,
                "--proxy-server=http://127.0.0.1:8080",
                "--ignore-certificate-errors-spki-list="+rootFingerPrint,
                "--user-data-dir=src/main/resources/chrome-profile",
                targetUrl
        );

        processBuilder.start();
    }
}
