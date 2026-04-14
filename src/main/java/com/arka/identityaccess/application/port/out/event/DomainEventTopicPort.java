package com.arka.identityaccess.application.port.out.event;

public interface DomainEventTopicPort {

    String topicFor(String eventType);
}
