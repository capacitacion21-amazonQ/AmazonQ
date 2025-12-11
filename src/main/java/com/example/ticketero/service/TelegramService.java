package com.example.ticketero.service;

import com.example.ticketero.model.entity.Mensaje;
import com.example.ticketero.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final MensajeRepository mensajeRepository;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.enabled:false}")
    private boolean telegramEnabled;

    @Scheduled(fixedDelay = 30000) // Cada 30 segundos
    @Transactional
    public void procesarMensajesPendientes() {
        if (!telegramEnabled) {
            log.debug("Telegram deshabilitado, saltando procesamiento");
            return;
        }

        List<Mensaje> mensajesPendientes = mensajeRepository.findByEnviadoFalseOrderByCreatedAtAsc();
        
        for (Mensaje mensaje : mensajesPendientes) {
            try {
                enviarMensaje(mensaje);
                mensaje.setEnviado(true);
                mensaje.setEnviadoAt(LocalDateTime.now());
                log.info("Mensaje enviado para ticket: {}", mensaje.getTicket().getCodigoReferencia());
            } catch (Exception e) {
                log.error("Error enviando mensaje para ticket {}: {}", 
                    mensaje.getTicket().getCodigoReferencia(), e.getMessage());
            }
        }
    }

    private void enviarMensaje(Mensaje mensaje) {
        String chatId = mensaje.getTicket().getChatId();
        if (chatId == null || chatId.isEmpty()) {
            // Simular envío por SMS si no hay chat_id
            log.info("Simulando SMS a {}: {}", 
                mensaje.getTicket().getTelefono(), mensaje.getContenido());
            return;
        }

        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        
        // Aquí iría la implementación real del envío a Telegram
        log.info("Enviando mensaje Telegram a {}: {}", chatId, mensaje.getContenido());
    }
}