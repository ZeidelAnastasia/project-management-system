package com.projectmanagement.intelligence.kafka;

import com.projectmanagement.intelligence.kafka.event.KafkaEvent;

public interface IntelligenceEventProcessor {

    void processProjectEvent(KafkaEvent event);

    void processTaskEvent(KafkaEvent event);
}