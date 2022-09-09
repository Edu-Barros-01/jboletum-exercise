package tarefas;

import lombok.extern.slf4j.Slf4j;
import models.BankCustomer;
import models.PaymentSlip;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class EmitirBoleto implements Runnable {

    @Override
    public void run() {

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(4));

        Path diretorio = Paths.get("");

        executaEmissaoDeBoletos(diretorio);

    }

    private static void executaEmissaoDeBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {

            Instant startTime = Instant.now();

            AtomicInteger arquivosProcessados = new AtomicInteger();

            files.toList()
                    .parallelStream()
                    .filter(EmitirBoleto::filtraArquivosRemessa)
                    .forEach(file -> {
                        log.info("Executando for para thread {}", Thread.currentThread().getName());
                        log.info("Arquivo filtrado: {}", file.getFileName().toString());
                        arquivosProcessados.getAndIncrement();
                        extrairLinhasDoArquivo(file);
                    });

            Instant endTime = Instant.now();
            long duration  = Duration.between(startTime, endTime).toMillis();
            log.info("Tempo processamento: {}ms", duration);
            log.info("Total de arquivos processados: {}", arquivosProcessados);
            log.info("Total de boletos: {}", Relatorio.getList().get(0));
            log.info("Total de boletos com sucesso: {}", Relatorio.getList().get(1));
            log.info("Total de boletos vencidos: {}", Relatorio.getList().get(3));

        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
    }

    private static void extrairLinhasDoArquivo(Path file) {
        try (var linhas = Files.lines(file)) {
            var codigosDeBarra = linhas.map(EmitirBoleto::transformaLinhaEmBoleto)
                    .map(EmitirBoleto::gerarCodigoDeBarras)
                    .collect(Collectors.toList());
            gerarArquivoRetorno(file.getFileName().toString(), codigosDeBarra);
        } catch (IOException e) {
            System.out.println("Deu erro ao ler linhas do arquivo");
        }
    }

    private static void gerarArquivoRetorno(String nomeArquivoRemessa, List<String> codigosDeBarra) throws IOException {
        String nomeArquivoRetorno = nomeArquivoRemessa.replace("rem","ret");
        String pathArquivoRetorno = "" + nomeArquivoRetorno;
        FileWriter escritor = new FileWriter(pathArquivoRetorno);

        Relatorio.armazenaTotalBoletos(Relatorio.getList().get(0) + codigosDeBarra.size());

    codigosDeBarra.forEach(
        linha -> {
          try {
            escritor.write(linha);
            escritor.write("\n");

          } catch (IOException e) {
            e.printStackTrace();
          }
        });
        escritor.close();

        // TODO -> Gerar arquivo com códigos de barras ([mesmo nome do arquivo remessa].ret
    }

    private static String gerarCodigoDeBarras(PaymentSlip paymentSlip) {
        // Tirei o "final" do atributo barcode da classe PaymentSlip
        // para poder setar o valor do codigo de barras

        paymentSlip.setBarcode(paymentSlip.getId() + ";"
                + paymentSlip.getDueDate() + ";"
                + paymentSlip.getPayer().getName() + ";"
                + paymentSlip.getPayer().getAgencyNumber() + ";"
                + paymentSlip.getPayer().getAccountNumber() + ";"
                + paymentSlip.getPayer().getBalance() + ";"
                + paymentSlip.getPayee().getName() + ";"
                + paymentSlip.getPayee().getAgencyNumber() + ";"
                + paymentSlip.getPayee().getAccountNumber() + ";"
                + paymentSlip.getPayee().getBalance() + ";"
                + paymentSlip.getValue() + ";"
                + paymentSlip.getStatusDueDate());

        return paymentSlip.getBarcode(); // TODO -> criar codigo a partir das informações do boleto
    }

    private static PaymentSlip transformaLinhaEmBoleto(String linha) {
        String[] valores = linha.split(";");

        return PaymentSlip.builder()
                .id(UUID.randomUUID().toString())
                .dueDate(validaData(LocalDate.parse(valores[0])))
//                .statusDueDate(LocalDate.parse(valores[0]).isBefore(LocalDate.now())
//                        ? "Data vencimento inválida"
//                        : "OK")
                .statusDueDate(validaStatusData(LocalDate.parse(valores[0])))
                .value(new BigDecimal(valores[1]))
                .payer(BankCustomer.builder()
                        .name(valores[2])
                        .agencyNumber(valores[3])
                        .accountNumber(valores[4])
                        .balance(new BigDecimal("1000.00"))
                        .build())
                .payee(BankCustomer.builder()
                        .name(valores[5])
                        .agencyNumber(valores[6])
                        .accountNumber(valores[7])
                        .balance(new BigDecimal("1000.00"))
                        .build())
                .build();
    }

    private static String validaStatusData (LocalDate data){
        if (!data.isBefore(LocalDate.now())){
            Relatorio.armazenaBoletosSucesso(Relatorio.getList().get(1) + 1);
            return "OK";
        }
        Relatorio.armazenaBoletosVencidos(Relatorio.getList().get(3) + 1);
        return "Data vencimento inválida";
    }

    private static LocalDate validaData (LocalDate data){
        if (!data.isBefore(LocalDate.now())){

            if ((data.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                    || data.getDayOfWeek().equals(DayOfWeek.SUNDAY))) {
                return data.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            }
        }
        return data;
    }

    private static boolean filtraArquivosRemessa(Path file)
    {
        return file.getFileName().toString().endsWith(".rem");
    }
}
