package models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BankCustomer {

    private final String name;
    private final String agencyNumber;
    private final String accountNumber;
    private final BigDecimal balance;
}
