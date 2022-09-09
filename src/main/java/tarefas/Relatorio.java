package tarefas;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode
public class Relatorio {

  private static List<Integer> list = Arrays.asList(0, 0, 0, 0);

    private Relatorio(){}


    public static void armazenaTotalBoletos(Integer valor0) {
        list.set(0, valor0);
    }

    public static void armazenaBoletosSucesso(Integer valor1) {
        list.set(1, valor1);
    }

    public static void armazenaBoletosErro(Integer valor2) {
        list.set(2, valor2);
    }

    public static void armazenaBoletosVencidos(Integer valor3) {
        list.set(3, valor3);
    }


    public static List<Integer> getList() {
        return list;
    }


}
