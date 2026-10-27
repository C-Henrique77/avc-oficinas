package br.edu.avc.oficinas.algoritmos;

import br.edu.avc.oficinas.dominio.Oficina;

import java.util.List;

/**
 * Demonstracao executavel do modulo de algoritmos (nao e rota da API).
 * Rode o metodo main (botao "Run" na IDE) para mostrar no video.
 */
public class DemoAlgoritmos {

    public static void main(String[] args) {
        List<Oficina> oficinas = AlgoritmosOficina.gerarOficinasDeExemplo(10);
        System.out.println("Ids na ordem original (desordenada): " + ids(oficinas));

        int linear = AlgoritmosOficina.buscaLinearPorId(oficinas, 7);
        System.out.println("Busca linear pelo id 7 -> indice " + linear + " (funciona em qualquer ordem)");

        int binariaSemOrdenar = AlgoritmosOficina.buscaBinariaPorId(oficinas, 7);
        System.out.println("Busca binaria SEM ordenar, id 7 -> indice " + binariaSemOrdenar
                + " (resultado nao confiavel)");

        AlgoritmosOficina.ordenarPorId(oficinas);
        System.out.println("Ids depois de ordenar por id: " + ids(oficinas));

        int binaria = AlgoritmosOficina.buscaBinariaPorId(oficinas, 7);
        System.out.println("Busca binaria COM ordenacao, id 7 -> indice " + binaria);

        AlgoritmosOficina.ordenarPorVagasDisponiveis(oficinas);
        System.out.println("Oficinas por vagas (maior primeiro):");
        for (Oficina o : oficinas) {
            System.out.println("  id=" + o.getId() + " vagas=" + o.getVagasDisponiveis());
        }
    }

    private static String ids(List<Oficina> oficinas) {
        return oficinas.stream().map(o -> String.valueOf(o.getId())).toList().toString();
    }
}
