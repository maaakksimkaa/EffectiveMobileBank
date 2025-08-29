package bank.effectivemobilebank.controller;

import bank.effectivemobilebank.dto.CardDto;
import bank.effectivemobilebank.dto.CreateCardRequest;
import bank.effectivemobilebank.dto.TopUpRequest;
import bank.effectivemobilebank.mapper.CardMapper;
import bank.effectivemobilebank.model.Card;
import bank.effectivemobilebank.model.CardStatus;
import bank.effectivemobilebank.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    private final CardService cardService;
    private final CardMapper cardMapper;

    public AdminCardController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        try {
            Card card = cardService.createCardForOwnerId(
                request.getOwnerId(), 
                request.getPlainPan(), 
                request.getExpiry()
            );
            return ResponseEntity.ok(cardMapper.toDto(card));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Ошибка создания карты: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.findAll(username, status, pageable);
        Page<CardDto> cardDtos = cards.map(cardMapper::toDto);
        return ResponseEntity.ok(cardDtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable String id) {
        try {
            cardService.deleteById(java.util.UUID.fromString(id));
            return ResponseEntity.ok().body(Map.of("message", "Карта удалена"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка удаления карты"));
        }
    }

    @PostMapping("/{id}/topup")
    public ResponseEntity<?> topUpCard(
            @PathVariable String id,
            @Valid @RequestBody TopUpRequest request) {
        try {
            cardService.adminTopUp(java.util.UUID.fromString(id), request.getAmount());
            return ResponseEntity.ok().body(Map.of("message", "Карта пополнена"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateCard(@PathVariable String id) {
        try {
            cardService.changeStatus(java.util.UUID.fromString(id), null, CardStatus.ACTIVE);
            return ResponseEntity.ok().body(Map.of("message", "Карта активирована"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка активации карты"));
        }
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<?> blockCard(@PathVariable String id) {
        try {
            cardService.changeStatus(java.util.UUID.fromString(id), null, CardStatus.BLOCKED);
            return ResponseEntity.ok().body(Map.of("message", "Карта заблокирована"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка блокировки карты"));
        }
    }
}


