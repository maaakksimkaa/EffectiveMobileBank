package bank.effectivemobilebank.service;

import bank.effectivemobilebank.config.CardNumberCipher;
import bank.effectivemobilebank.model.Card;
import bank.effectivemobilebank.model.CardStatus;
import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.repository.CardRepository;
import bank.effectivemobilebank.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberCipher cipher;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardNumberCipher cipher) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cipher = cipher;
    }

    @Transactional
    public Card createCard(User owner, String plainPan, LocalDate expiry) {
        Card card = new Card();
        card.setOwner(owner);
        card.setExpiry(expiry);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setPanLast4(plainPan.substring(plainPan.length() - 4));
        card.setPanEncrypted(cipher.encrypt(plainPan));
        return cardRepository.save(card);
    }

    @Transactional
    public Card createCardForOwnerId(UUID ownerId, String plainPan, LocalDate expiry) {
        var owner = userRepository.findById(ownerId).orElseThrow();
        return createCard(owner, plainPan, expiry);
    }

    public Page<Card> findUserCards(User owner, CardStatus status, Pageable pageable) {
        if (status == null) {
            return cardRepository.findAllByOwner(owner, pageable);
        }
        return cardRepository.findAllByOwnerAndStatus(owner, status, pageable);
    }

    @Transactional
    public void changeStatus(UUID cardId, User ownerOrNull, CardStatus status) {
        Card card = ownerOrNull == null
            ? cardRepository.findById(cardId).orElseThrow()
            : cardRepository.findByIdAndOwner(cardId, ownerOrNull).orElseThrow();
        card.setStatus(status);
        cardRepository.save(card);
    }

    @Transactional
    public void transfer(User owner, UUID fromCardId, UUID toCardId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Карты должны отличаться");
        }
        Card from = cardRepository.findByIdAndOwner(fromCardId, owner).orElseThrow();
        Card to = cardRepository.findByIdAndOwner(toCardId, owner).orElseThrow();
        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Карты должны быть активны");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств");
        }
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        cardRepository.save(from);
        cardRepository.save(to);
    }

    public Page<Card> findAll(String username, CardStatus status, Pageable pageable) {
        if (username != null && !username.isBlank()) {
            if (status != null) {
                return cardRepository.findAllByOwnerUsernameContainingIgnoreCaseAndStatus(username, status, pageable);
            }
            return cardRepository.findAllByOwnerUsernameContainingIgnoreCase(username, pageable);
        }
        if (status != null) {
            return cardRepository.findAllByStatus(status, pageable);
        }
        return cardRepository.findAll(pageable);
    }

    @Transactional
    public void deleteById(UUID cardId) {
        cardRepository.deleteById(cardId);
    }

    @Transactional
    public void topUp(User owner, UUID cardId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        Card card = cardRepository.findByIdAndOwner(cardId, owner).orElseThrow();
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Карта должна быть активна");
        }
        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);
    }

    @Transactional
    public void adminTopUp(UUID cardId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        Card card = cardRepository.findById(cardId).orElseThrow();
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Карта должна быть активна");
        }
        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);
    }
}


