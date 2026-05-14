package org.ulpgc.starlink.spacex.control;

import java.util.List;

public interface EventSender {
    void sendMessages(List<String> messages);
}
