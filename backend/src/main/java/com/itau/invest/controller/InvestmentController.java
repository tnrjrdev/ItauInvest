package com.itau.invest.controller;

import com.itau.invest.dto.investment.ApplicationRequest;
import com.itau.invest.dto.investment.InvestmentPositionResponse;
import com.itau.invest.dto.investment.PortfolioResponse;
import com.itau.invest.dto.investment.RedemptionRequest;
import com.itau.invest.security.SecurityUtils;
import com.itau.invest.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
@Tag(name = "Investimentos", description = "Aplicacoes, resgates e carteira consolidada")
@SecurityRequirement(name = "bearerAuth")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping("/applications")
    @Operation(summary = "Aplica recursos da carteira em um produto")
    public ResponseEntity<InvestmentPositionResponse> apply(
            @Valid @RequestBody ApplicationRequest request) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(investmentService.applyAndGetPosition(userId, request));
    }

    @PostMapping("/{investmentId}/redemptions")
    @Operation(summary = "Resgata (parcial ou total) o valor bruto de uma posicao")
    public ResponseEntity<PortfolioResponse> redeem(
            @PathVariable UUID investmentId,
            @Valid @RequestBody RedemptionRequest request) {
        UUID userId = SecurityUtils.currentUserId();
        investmentService.redeem(userId, investmentId, request);
        return ResponseEntity.ok(investmentService.getPortfolio(userId));
    }

    @GetMapping("/positions")
    @Operation(summary = "Lista as posicoes ativas com valorizacao")
    public ResponseEntity<List<InvestmentPositionResponse>> positions() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(investmentService.listPositions(userId));
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Retorna a carteira consolidada (caixa + investimentos)")
    public ResponseEntity<PortfolioResponse> portfolio() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(investmentService.getPortfolio(userId));
    }
}
