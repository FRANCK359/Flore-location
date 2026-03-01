package com.location.evenement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsResponse {
    private long pendingPayments;
    private long paidAtPickup;
    private long cancelledPayments;
    private BigDecimal totalPendingAmount;
    private BigDecimal totalCollectedAmount;
}