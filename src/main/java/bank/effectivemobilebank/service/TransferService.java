package bank.effectivemobilebank.service;

import bank.effectivemobilebank.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferService {
    private final CardService cardService;

    public TransferService(CardService cardService) {
        this.cardService = cardService;
    }

    @Transactional
    public void transferBetweenOwn(User owner, UUID fromCardId, UUID toCardId, BigDecimal amount) {
        cardService.transfer(owner, fromCardId, toCardId, amount);
    }
}


