package bank.effectivemobilebank.dto;

import bank.effectivemobilebank.model.CardStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class CardDto {
    private UUID id;
    private String maskedNumber;
    private LocalDate expiry;
    private CardStatus status;
    private BigDecimal balance;
    private UUID ownerId;
}


