package com.chess.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Render ক্লাউডের HTTPS রিকোয়েস্ট সিকিউরলি পাস করার গ্যারান্টিড মেথড
        registry.addEndpoint("/ws-chess")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setClientLibraryUrl("https://jsdelivr.net");
    }
}
