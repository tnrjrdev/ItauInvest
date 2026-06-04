package com.itau.invest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Ponto de entrada da aplicacao ItauInvest.
 *
 * <p>Plataforma de investimentos: gestao de carteira de caixa, catalogo de
 * produtos, aplicacoes/resgates e posicao consolidada com valorizacao.</p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class InvestApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestApplication.class, args);
    }
}
