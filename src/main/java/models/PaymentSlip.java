package models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

@Data
@Builder
public class PaymentSlip {

    private final String id;
    private final LocalDate dueDate;
    private final String statusDueDate;
    private final BankCustomer payer;
    private final BankCustomer payee;
    private final BigDecimal value;
    private String barcode;


}
