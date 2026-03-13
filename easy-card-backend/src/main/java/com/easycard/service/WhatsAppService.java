package com.easycard.service;

import com.easycard.entity.Card;
import com.easycard.entity.User;
import com.easycard.repository.CardRepository;
import com.easycard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.auth-token}")
    private String authToken;

    @Value("${whatsapp.business-phone}")
    private String businessPhone;

    private static final String CANCEL_COMMAND = "CANCEL";
    private static final String HELP_COMMAND = "HELP";

    public String processIncomingMessage(String from, String message) {
        log.info("Received WhatsApp message from {}: {}", from, message);

        String normalizedMessage = message.trim().toUpperCase();

        if (normalizedMessage.startsWith(CANCEL_COMMAND)) {
            return handleCancellationRequest(from, normalizedMessage);
        } else if (normalizedMessage.equals(HELP_COMMAND)) {
            return getHelpMessage();
        } else {
            return getWelcomeMessage();
        }
    }

    private String handleCancellationRequest(String from, String message) {
        Pattern pattern = Pattern.compile("CANCEL\\s+(\\d{4})");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String lastFourDigits = matcher.group(1);
            return cancelCardByLastFour(from, lastFourDigits);
        } else {
            return "To cancel your card, please send: CANCEL <last-4-digits>\n" +
                   "Example: CANCEL 1234";
        }
    }

    private String cancelCardByLastFour(String phoneNumber, String lastFour) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElse(null);

        if (user == null) {
            return "No account found with this phone number. Please register first.";
        }

        Card card = cardRepository.findByUserId(user.getId()).stream()
                .filter(c -> c.getCardNumber().endsWith(lastFour))
                .findFirst()
                .orElse(null);

        if (card == null) {
            return "No card found ending with " + lastFour + ".";
        }

        if (card.getStatus() == Card.CardStatus.BLOCKED) {
            return "Card ending with " + lastFour + " is already cancelled.";
        }

        card.setStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);

        return "Your card ending with " + lastFour + " has been successfully cancelled.\n" +
               "Card Number: " + card.getCardNumber() + "\n" +
               "Status: CANCELLED\n\n" +
               "If you did not request this, please contact support immediately.";
    }

    private String getWelcomeMessage() {
        return "Welcome to Easy Card! 🏦\n\n" +
               "Available commands:\n" +
               "- CANCEL <last-4-digits> : Cancel your card\n" +
               "- HELP : Show this message\n\n" +
               "Example: CANCEL 1234";
    }

    private String getHelpMessage() {
        return "Easy Card Help 📱\n\n" +
               "Commands:\n" +
               "• CANCEL <last-4-digits> - Cancel your card subscription\n" +
               "• HELP - Show this message\n\n" +
               "To cancel, send your command to this number.\n" +
               "You will receive a confirmation once processed.";
    }

    public void sendWhatsAppMessage(String to, String message) {
        try {
            String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", to);
            body.put("type", "text");

            Map<String, String> textBody = new HashMap<>();
            textBody.put("body", message);
            body.put("text", textBody);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            log.info("WhatsApp message sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
        }
    }

    public String generateCancellationLink(Long cardId, String cardLastFour) {
        return "https://wa.me/" + businessPhone + "?text=CANCEL+" + cardLastFour;
    }

    public Card getCardForCancellationLink(Long cardId) {
        return cardRepository.findById(cardId).orElse(null);
    }

    public String getBusinessPhone() {
        return businessPhone;
    }
}
