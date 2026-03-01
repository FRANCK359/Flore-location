package com.location.evenement.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmailResponse {
    private String messageId;
    private String to;
    private String subject;
    private LocalDateTime sentAt;
    private boolean success;
    private String errorMessage;
}