package com.easycard.controller;

import com.easycard.entity.Card;
import com.easycard.service.WhatsAppService;
import com.easycard.service.ExpiryNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppController {

    private final WhatsAppService whatsAppService;
    private final ExpiryNotificationService expiryNotificationService;

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("WhatsApp webhook verified");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received WhatsApp webhook: {}", payload);

            if (payload.containsKey("entry")) {
                @SuppressWarnings("unchecked")
                var entry = ((java.util.List<Map<String, Object>>) payload.get("entry")).get(0);
                @SuppressWarnings("unchecked")
                var changes = ((java.util.List<Map<String, Object>>) entry.get("changes")).get(0);
                @SuppressWarnings("unchecked")
                var value = (Map<String, Object>) changes.get("value");
                
                if (value.containsKey("messages")) {
                    @SuppressWarnings("unchecked")
                    var messages = (java.util.List<Map<String, Object>>) value.get("messages");
                    
                    for (var message : messages) {
                        String from = (String) message.get("from");
                        @SuppressWarnings("unchecked")
                        var text = (Map<String, String>) message.get("text");
                        String messageText = text.get("body");
                        
                        String response = whatsAppService.processIncomingMessage(from, messageText);
                        whatsAppService.sendWhatsAppMessage(from, response);
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Error processing WhatsApp webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/cancel-link/{cardId}")
    public ResponseEntity<Map<String, String>> getCancelLink(@PathVariable Long cardId) {
        Card card = whatsAppService.getCardForCancellationLink(cardId);
        if (card != null) {
            String lastFour = card.getCardNumber().substring(card.getCardNumber().length() - 4);
            String link = "https://wa.me/" + whatsAppService.getBusinessPhone() + "?text=CANCEL+" + lastFour;
            return ResponseEntity.ok(Map.of("link", link, "cardLastFour", lastFour));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/notify-expiry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerExpiryNotification() {
        expiryNotificationService.sendExpiryNotifications();
        return ResponseEntity.ok(Map.of("status", "Notification batch triggered"));
    }

    @PostMapping("/notify-expiry/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerSingleExpiryNotification(@PathVariable Long cardId) {
        expiryNotificationService.sendManualExpiryNotification(cardId);
        return ResponseEntity.ok(Map.of("status", "Notification sent for card " + cardId));
    }
}
