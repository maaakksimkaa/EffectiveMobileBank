package bank.effectivemobilebank.repository;

import bank.effectivemobilebank.model.Card;
import bank.effectivemobilebank.model.CardStatus;
import bank.effectivemobilebank.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Page<Card> findAllByOwner(User owner, Pageable pageable);
    Page<Card> findAllByOwnerAndStatus(User owner, CardStatus status, Pageable pageable);
    Optional<Card> findByIdAndOwner(UUID id, User owner);
    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);
    Page<Card> findAllByOwnerUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<Card> findAllByOwnerUsernameContainingIgnoreCaseAndStatus(String username, CardStatus status, Pageable pageable);
}


