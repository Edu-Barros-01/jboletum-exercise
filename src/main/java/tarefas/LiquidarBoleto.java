package tarefas;

import lombok.extern.slf4j.Slf4j;
import models.BankCustomer;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class LiquidarBoleto implements Runnable{

    @Override
    public void run() {

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(4));

        Path diretorio = Paths.get("");

        executaLiquidacaoDeBoletos(diretorio);
    }

    private static void executaLiquidacaoDeBoletos(Path diretorio) {

        try (var files = Files.list(diretorio)) {

            Instant startTime = Instant.now();

            AtomicInteger arquivosProcessados = new AtomicInteger();

            files.toList()
                    .parallelStream()
                    .filter(LiquidarBoleto::filtraArquivosRetorno)
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

        } catch (IOException e) {
            System.out.println("Deu erro!!");
        }
    }

    private static void extrairLinhasDoArquivo(Path file) {
        try (var linhas = Files.lines(file)) {
            var resultadoLiquidacao = linhas.map(LiquidarBoleto::processarBoleto)
                    .collect(Collectors.toList());
            gerarArquivoLiquidacao(file.getFileName().toString(), resultadoLiquidacao);
        } catch (IOException e) {
            System.out.println("Deu erro ao ler linhas do arquivo");
        }
    }

    private static void gerarArquivoLiquidacao(String nomeArquivoRetorno, List<String> codigosDeBarra) throws IOException {
        String nomeArquivoLiquidacao = nomeArquivoRetorno.replace("ret","liq");
        String pathArquivoRetorno = "" + nomeArquivoLiquidacao;
        FileWriter escritor = new FileWriter(pathArquivoRetorno);

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

    }

            // TODO: aqui no transformarLinhaEmBoleto implementar a transferencia entre contas
            // TODO: e tratar quando o valor do boleto for maior que o saldo em conta do Payer !!!!
    private static String processarBoleto(String linha) {
        String[] valores = linha.split(";");

        String linhaLiquidacao;

        if (new BigDecimal(valores[5]).compareTo(new BigDecimal(valores[10])) == -1){
            return "Boleto: " + valores[0]
                    + " -> Saldo insuficiente do pagador";
        }else if (valores[11].equalsIgnoreCase("OK")){

            BankCustomer payer = BankCustomer.builder()
                    .name(valores[2])
                    .agencyNumber(valores[3])
                    .accountNumber(valores[4])
                    .balance(new BigDecimal(valores[5])
                            .subtract(new BigDecimal(valores[10])))
                    .build();
            BankCustomer payee = BankCustomer.builder()
                    .name(valores[6])
                    .agencyNumber(valores[7])
                    .accountNumber(valores[8])
                    .balance(new BigDecimal(valores[9])
                            .add(new BigDecimal(valores[10])))
                    .build();

            linhaLiquidacao = "Boleto: " + valores[0]
                    + " -> Processado com sucesso. Valor do boleto: "
                    + new BigDecimal(valores[10]) + " debitado do pagador. Novo saldo pagador: "
                    + payer.getBalance()
                    + " Novo saldo recebedor: "
                    + payee.getBalance();

        }else{
            linhaLiquidacao = "Boleto: " + valores[0]
                    + " -> Vencido. Solicitar novo boleto.";
        }

        return linhaLiquidacao;

    }

    private static boolean filtraArquivosRetorno(Path file)
    {
        return file.getFileName().toString().endsWith(".ret");
    }

}
