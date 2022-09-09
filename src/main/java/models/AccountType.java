package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountType {

    CONTA_CORRENTE(3), POUPANCA(2);

    private final Integer taxaDeJuros;
}
