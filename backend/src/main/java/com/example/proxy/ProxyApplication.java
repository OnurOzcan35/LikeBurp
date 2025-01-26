package com.example.proxy;

import com.example.proxy.config.RootCertificateCreator;
import com.example.proxy.utils.CertificateUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static com.example.proxy.security.SSLContextManager.createDynamicSSLContext;

@SpringBootApplication
public class ProxyApplication {

      public static void main(String[] args) {
          SpringApplication.run(ProxyApplication.class, args);
      }
}
