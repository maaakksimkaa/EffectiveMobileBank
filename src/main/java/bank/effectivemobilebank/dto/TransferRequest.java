package bank.effectivemobilebank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class TransferRequest {
    @NotNull(message = "ID карты-отправителя обязателен")
    private UUID fromCardId;
    
    @NotNull(message = "ID карты-получателя обязателен")
    private UUID toCardId;
    
    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

}


