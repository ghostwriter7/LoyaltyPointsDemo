package com.rozplochowski.loyaltypoints.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
class KafkaConfiguration {

    @Bean
    NewTopic orderSubmittedTopic() {
        return new NewTopic("order.submitted", 3, (short) 3);
    }

    @Bean
    NewTopic orderSubmittedTopicDlq() {
        return new NewTopic("order.submitted-dt", 3, (short) 3);
    }

    @Bean
    NewTopic userPointsChangeTopic() {
        return new NewTopic("user.points.change", 3, (short) 3);
    }

    @Bean
    NewTopic userPointsChangeDlq() {
        return new NewTopic("user.points.change-dlt", 3, (short) 3);
    }

    @Bean
    CommonErrorHandler errorHandler(KafkaOperations<String, String> kafkaOperations) {
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaOperations), new FixedBackOff(1000, 2));
    }

    @Bean
    RecordMessageConverter converter() {
        return new JacksonJsonMessageConverter();
    }
}
