package com.caelum.chronos.shared.application.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String destination, Object payload) {
        log.info("Sending notification to destination: {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }
}
