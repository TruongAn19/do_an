package com.example.quanly.service;

import com.example.quanly.domain.dto.SlotEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishHeld(Long subCourtId, Long availableTimeId, LocalDate bookingDate,
                            Long userId, LocalDateTime holdEndTime) {
        SlotEvent event = SlotEvent.builder()
                .type(SlotEvent.Type.SLOT_HELD)
                .subCourtId(subCourtId)
                .availableTimeId(availableTimeId)
                .bookingDate(bookingDate)
                .userId(userId)
                .holdEndTime(holdEndTime)
                .build();
        send(event);
    }

    public void publishReleased(Long subCourtId, Long availableTimeId, LocalDate bookingDate) {
        SlotEvent event = SlotEvent.builder()
                .type(SlotEvent.Type.SLOT_RELEASED)
                .subCourtId(subCourtId)
                .availableTimeId(availableTimeId)
                .bookingDate(bookingDate)
                .build();
        send(event);
    }

    private void send(SlotEvent event) {
        String topic = String.format("/topic/slot-events/%d/%s",
                event.getSubCourtId(), event.getBookingDate());
        log.debug("Publishing slot event to {}: {}", topic, event.getType());
        messagingTemplate.convertAndSend(topic, event);
    }
}
