package bank.effectivemobilebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class CreateCardRequest {
    @NotNull(message = "ID владельца обязателен")
    private UUID ownerId;
    
    @NotBlank(message = "Номер карты обязателен")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать 16 цифр")
    private String plainPan;
    
    @NotNull(message = "Срок действия обязателен")
    private LocalDate expiry;

}


