package bank.effectivemobilebank.controller;

import bank.effectivemobilebank.config.CurrentUser;
import bank.effectivemobilebank.dto.CardDto;
import bank.effectivemobilebank.dto.TopUpRequest;
import bank.effectivemobilebank.dto.TransferRequest;
import bank.effectivemobilebank.mapper.CardMapper;
import bank.effectivemobilebank.model.Card;
import bank.effectivemobilebank.model.CardStatus;
import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@PreAuthorize("hasRole('USER')")
public class UserCardController {
    private final CardService cardService;
    private final CardMapper cardMapper;

    public UserCardController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getMyCards(
            @CurrentUser User currentUser,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.findUserCards(currentUser, status, pageable);
        Page<CardDto> cardDtos = cards.map(cardMapper::toDto);
        return ResponseEntity.ok(cardDtos);
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockCard(@CurrentUser User currentUser, @PathVariable String id) {
        try {
            cardService.changeStatus(java.util.UUID.fromString(id), currentUser, CardStatus.BLOCKED);
            return ResponseEntity.ok().body(java.util.Map.of("message", "Карта заблокирована"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferBetweenCards(
            @CurrentUser User currentUser,
            @Valid @RequestBody TransferRequest request) {
        try {
            cardService.transfer(currentUser, request.getFromCardId(), request.getToCardId(), request.getAmount());
            return ResponseEntity.ok().body(java.util.Map.of("message", "Перевод выполнен успешно"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/topup")
    public ResponseEntity<?> topUpCard(
            @CurrentUser User currentUser,
            @PathVariable String id,
            @Valid @RequestBody TopUpRequest request) {
        try {
            cardService.topUp(currentUser, java.util.UUID.fromString(id), request.getAmount());
            return ResponseEntity.ok().body(java.util.Map.of("message", "Карта пополнена"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}


