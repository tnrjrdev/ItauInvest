package com.itau.invest.controller;

import com.itau.invest.dto.common.PageResponse;
import com.itau.invest.dto.transaction.TransactionResponse;
import com.itau.invest.dto.wallet.CashOperationRequest;
import com.itau.invest.dto.wallet.WalletResponse;
import com.itau.invest.security.SecurityUtils;
import com.itau.invest.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "Carteira", description = "Operacoes de caixa: saldo, deposito, saque e extrato")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "Consulta o saldo da carteira do cliente autenticado")
    public ResponseEntity<WalletResponse> getWallet() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(WalletResponse.from(walletService.getByUserId(userId)));
    }

    @PostMapping("/deposits")
    @Operation(summary = "Realiza um deposito na carteira")
    public ResponseEntity<WalletResponse> deposit(@Valid @RequestBody CashOperationRequest request) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(WalletResponse.from(walletService.deposit(userId, request)));
    }

    @PostMapping("/withdrawals")
    @Operation(summary = "Realiza um saque da carteira")
    public ResponseEntity<WalletResponse> withdraw(@Valid @RequestBody CashOperationRequest request) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(WalletResponse.from(walletService.withdraw(userId, request)));
    }

    @GetMapping("/statement")
    @Operation(summary = "Lista o extrato (ledger) paginado da carteira")
    public ResponseEntity<PageResponse<TransactionResponse>> statement(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(
                PageResponse.of(walletService.statement(userId, pageable), TransactionResponse::from));
    }
}
