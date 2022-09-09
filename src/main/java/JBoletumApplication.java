import models.Menu;
import tarefas.Relatorio;

import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JBoletumApplication {

    public static void main(String[] args) {

        System.out.println("A aplicação está em execução...");

        exibeMenu();

        escolherTarefa(
                JBoletumApplication::capturaEntradaScanner,
                JBoletumApplication::identificaMenu);
    }

    private static Optional<Menu> identificaMenu(String valorCapturado) {
        return Optional.ofNullable(valorCapturado)
                .filter(valor -> !valor.isBlank())
                .map(Integer::parseInt)
                .map(indice -> Menu.values()[indice-1]);
    }

    private static String capturaEntradaScanner() {
        return new Scanner(System.in).nextLine();
    }

    private static void exibeMenu() {
        System.out.printf("\nMENU - ESCOLHA UMA OPÇÃO\n");
        Stream.of(Menu.values())
                .map(JBoletumApplication::formataEntradaMenu)
                .forEach(System.out::println);
    }

    private static String formataEntradaMenu(Menu menu) {
        var formato = "[%d] - %s";
        return String.format(formato, menu.ordinal() + 1, menu.getNome());
    }

    private static void escolherTarefa(Supplier<String> funcaoCapturaEntrada,
                                       Function<String, Optional<Menu>> funcaoIdentificaMenu) {
        funcaoIdentificaMenu.apply(funcaoCapturaEntrada.get())
                .map(Menu::getTarefa)
/*                .ifPresentOrElse(Runnable::run, () -> {
                    System.out.println("Menu não encontrado para o valor de entrada!!");
                    escolherTarefa(funcaoCapturaEntrada, funcaoIdentificaMenu);
                });*/
                // TODO: Código para rodar tarefas de forma assincrona sem parar a main
                .ifPresentOrElse(tarefa -> {
                    CompletableFuture.runAsync(tarefa);
                    escolherTarefa(funcaoCapturaEntrada, funcaoIdentificaMenu);
                    },() -> {System.out.println("Menu não encontrado para o valor de entrada!!");
                    escolherTarefa(funcaoCapturaEntrada, funcaoIdentificaMenu);
                    });
    }
}
