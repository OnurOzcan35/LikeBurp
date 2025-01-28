package com.example.proxy.service;

import com.example.proxy.model.LogEntry;
import com.example.proxy.model.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public void saveLog(String direction, String guid, String data, String status) {
        LogEntry logEntry = new LogEntry();
        logEntry.setDirection(direction);
        logEntry.setData(data);
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setStatus(status);
        logEntry.setGuid(guid);
        logRepository.save(logEntry);
    }
}
