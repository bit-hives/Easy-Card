package com.easycard.service;

import com.easycard.entity.Card;
import com.easycard.entity.User;
import com.easycard.repository.CardRepository;
import com.easycard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiryNotificationService {

    private final CardRepository cardRepository;
    private final WhatsAppService whatsAppService;
    private final UserRepository userRepository;

    @Value("${whatsapp.expiry-notification-days:30}")
    private int notificationDaysBefore;

    @Scheduled(cron = "${scheduler.cron:0 0 9 * * *}")
    public void sendExpiryNotifications() {
        log.info("Starting scheduled expiry notification check");
        
        LocalDate expiryThreshold = LocalDate.now().plusDays(notificationDaysBefore);
        
        List<Card> expiringCards = cardRepository.findAll().stream()
                .filter(card -> card.getStatus() == Card.CardStatus.ACTIVE)
                .filter(card -> card.getExpiryDate() != null)
                .filter(card -> !card.getExpiryDate().isAfter(expiryThreshold))
                .toList();

        log.info("Found {} cards expiring within {} days", expiringCards.size(), notificationDaysBefore);

        for (Card card : expiringCards) {
            try {
                User user = card.getUser();
                if (user != null && user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                    String message = buildExpiryMessage(card);
                    whatsAppService.sendWhatsAppMessage(user.getPhoneNumber(), message);
                    log.info("Sent expiry notification for card {} to user {}", 
                            card.getCardNumber(), user.getEmail());
                }
            } catch (Exception e) {
                log.error("Failed to send expiry notification for card {}: {}", 
                        card.getCardNumber(), e.getMessage());
            }
        }
        
        log.info("Completed expiry notification batch");
    }

    private String buildExpiryMessage(Card card) {
        LocalDate expiryDate = card.getExpiryDate();
        LocalDate today = LocalDate.now();
        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
        
        String urgency;
        if (daysUntilExpiry <= 0) {
            urgency = "EXPIRED";
        } else if (daysUntilExpiry <= 7) {
            urgency = "URGENT";
        } else {
            urgency = "Reminder";
        }
        
        String lastFour = card.getCardNumber().substring(card.getCardNumber().length() - 4);
        
        return "🔔 " + urgency + ": Card Expiry Notification\n\n" +
               "Your Easy Card ending in " + lastFour + " is " + 
               (daysUntilExpiry <= 0 ? "EXPIRED" : "expiring in " + daysUntilExpiry + " days") + ".\n\n" +
               "Expiry Date: " + expiryDate + "\n\n" +
               "Please contact us to renew your card or ignore if already renewed.\n\n" +
               "Thank you for using Easy Card!";
    }

    public void sendManualExpiryNotification(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        User user = card.getUser();
        if (user != null && user.getPhoneNumber() != null) {
            String message = buildExpiryMessage(card);
            whatsAppService.sendWhatsAppMessage(user.getPhoneNumber(), message);
        }
    }
}
