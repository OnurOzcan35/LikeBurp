package com.example.proxy.service;

import com.example.proxy.config.RootCertificateCreator;
import com.example.proxy.security.SSLContextManager;
import com.example.proxy.utils.CertificateUtils;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RootCertificateCreator rootCertificateCreator;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, SSLContext> sslContextCache = new ConcurrentHashMap<>();
    private volatile boolean isClosed = false;

    private boolean isRunning = false;

    static{ Security.addProvider(new BouncyCastleProvider()); }

    public void startProxy(int port) throws IOException {
        if (isRunning) {
            System.out.println("Proxy already running.");
            return;
        }
        isRunning = true;

        System.out.println("Proxy started on port: " + port);

        threadPool.execute(() -> {
            while (isRunning) {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (isRunning) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void stopProxy() throws IOException {
        if (isRunning) {
            isRunning = false;
//            if (serverSocket != null && !serverSocket.isClosed()) {
//                serverSocket.close();
//            }
            threadPool.shutdown();
            System.out.println("Proxy stopped.");
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                InputStream clientInput = new BufferedInputStream(clientSocket.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
        ) {
            String requestLine = reader.readLine();
            System.out.println(requestLine);
            if (requestLine != null && requestLine.startsWith("CONNECT")) {
                //System.out.println("CONNECT request: " + requestLine);

                String[] parts = requestLine.split(" ");
                String targetHost = parts[1].split(":")[0];
                int targetPort = Integer.parseInt(parts[1].split(":")[1]);
                OutputStream clientOutput = clientSocket.getOutputStream();
                clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                clientOutput.flush();

                System.out.println("Received ClientHello from browser, starting TLS handshake...");
                SSLContext sslContext = getOrCreateSSLContext(targetHost);
                handleTLSConnection(clientSocket, targetHost, targetPort, sslContext);
                //handleProxy(clientSocket, sslContext);
//                if (isAlreadyTLS(clientInput)) {
//                    //
//                } else {
//                    System.out.println("Non-TLS traffic detected. This is not HTTPS.");
//                    forwardData(clientInput, clientOutput);
//                }
            }
            else if (requestLine != null) {
                System.out.println("HTTP request: " + requestLine);

                String[] parts = requestLine.split(" ");
                String method = parts[0];
                String url = parts[1];

                String host = getHostFromHeaders(reader);
                int port = 80;

                try (Socket targetSocket = new Socket(host, port)) {
                    OutputStream targetOutput = targetSocket.getOutputStream();
                    targetOutput.write((requestLine + "\r\n").getBytes());

                    String header;
                    while (!(header = reader.readLine()).isEmpty()) {
                        targetOutput.write((header + "\r\n").getBytes());
                    }
                    targetOutput.write("\r\n".getBytes());
                    targetOutput.flush();

                    InputStream targetInput = targetSocket.getInputStream();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = targetInput.read(buffer)) != -1) {
                        OutputStream clientOutput = clientSocket.getOutputStream();
                        clientOutput.write(buffer, 0, bytesRead);
                        clientOutput.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getHostFromHeaders(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.toLowerCase().startsWith("host:")) {
                return line.split(" ")[1].trim();
            }
        }
        throw new IOException("Host header not found in HTTP request");
    }

    private boolean isAlreadyTLS(InputStream clientInput) {
        try {
            if (!clientInput.markSupported()) {
                return false;
            }
            clientInput.mark(5);
            byte[] header = new byte[5];
            int bytesRead = clientInput.read(header);
            clientInput.reset();
            return bytesRead == 5 && header[0] == 0x16 && header[1] == 0x03;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleTLSConnection(Socket clientSocket, String targetHost, int targetPort, SSLContext sslContext) {
        SSLSocket targetSSLSocket = null;
        SSLSocket clientSSLSocket = null;
        try {
            SSLSocketFactory targetSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            targetSSLSocket = (SSLSocket) targetSocketFactory.createSocket(targetHost, targetPort);
            targetSSLSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            targetSSLSocket.setSoTimeout(30000);
            targetSSLSocket.startHandshake();
            System.out.println("Handshake with target server successful!");

            SSLSocketFactory clientSocketFactory = sslContext.getSocketFactory();
            clientSSLSocket = (SSLSocket) clientSocketFactory.createSocket(
                    clientSocket, clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true
            );
            clientSSLSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
            clientSSLSocket.setUseClientMode(false);
            clientSSLSocket.setSoTimeout(30000);
            clientSSLSocket.startHandshake();
            System.out.println("Handshake with browser successful!");

            forwardData(clientSSLSocket, targetSSLSocket);
        } catch (IOException e) {
            System.err.println("Error during proxy connection: " + e.getMessage());
        } finally {
            closeSocket(clientSSLSocket);
            closeSocket(targetSSLSocket);
        }
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    private void forwardData(SSLSocket sourceSocket, SSLSocket destinationSocket) {
        Thread forwardThread1 = new Thread(() -> {
            try (InputStream in = sourceSocket.getInputStream();
                 OutputStream out = destinationSocket.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while (!isClosed && (bytesRead = in.read(buffer)) != -1) {
                    logData(buffer, bytesRead, "Source to Destination");
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Error forwarding data (source to destination): " + e.getMessage());
            } finally {
                closeSocketsSafely(sourceSocket, destinationSocket);
            }
        });

        Thread forwardThread2 = new Thread(() -> {
            try (InputStream in = destinationSocket.getInputStream();
                 OutputStream out = sourceSocket.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while (!isClosed && (bytesRead = in.read(buffer)) != -1) {
                    logData(buffer, bytesRead, "Destination to Source");
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Error forwarding data (destination to source): " + e.getMessage());
            } finally {
                closeSocketsSafely(sourceSocket, destinationSocket);
            }
        });

        forwardThread1.start();
        forwardThread2.start();

        try {
            forwardThread1.join();
            forwardThread2.join();
        } catch (InterruptedException e) {
            System.err.println("Thread interruption: " + e.getMessage());
        }
    }

    private void logData(byte[] buffer, int bytesRead, String direction) {
        System.out.println(direction + ": " + new String(buffer, 0, bytesRead));
    }

    private synchronized void closeSocketsSafely(Socket... sockets) {
        if (isClosed) return;
        isClosed = true;

        for (Socket socket : sockets) {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    private SSLContext getOrCreateSSLContext(String targetHost) {
        return sslContextCache.computeIfAbsent(targetHost, host -> {
            try {
                KeyPair dynamicKeyPair = CertificateUtils.generateKeyPair();
                X509Certificate rootCertificate = rootCertificateCreator.getRootCertificate();
                X509Certificate certificate = CertificateUtils.dynamicCertificateGenerator(dynamicKeyPair, rootCertificateCreator.getRootKeyPair(), host, rootCertificate);

                return SSLContextManager.createDynamicSSLContext(dynamicKeyPair.getPrivate(), certificate, rootCertificate);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create SSLContext for host: " + host, e);
            }
        });
    }
}
