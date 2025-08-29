package bank.effectivemobilebank.mapper;

import bank.effectivemobilebank.dto.CardDto;
import bank.effectivemobilebank.model.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardDto toDto(Card card) {
        if (card == null) {
            throw new NullPointerException("Card cannot be null");
        }
        if (card.getPanLast4() == null) {
            throw new NullPointerException("Card panLast4 cannot be null");
        }
        if (card.getOwner() == null) {
            throw new NullPointerException("Card owner cannot be null");
        }
        if (card.getOwner().getId() == null) {
            throw new NullPointerException("Card owner ID cannot be null");
        }

        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber("**** **** **** " + card.getPanLast4());
        dto.setExpiry(card.getExpiry());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setOwnerId(card.getOwner().getId());
        return dto;
    }
}


