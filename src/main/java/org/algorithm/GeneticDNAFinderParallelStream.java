package org.algorithm;

import java.io.*;
import java.util.*;


public class GeneticDNAFinderParallelStream {
    public static final int POPULATION_SIZE = 5000;
    public static final int DNA_LENGTH = 20;
    public static final int MAX_GENERATIONS = 1000;
    public static final String TARGET = "CACCTTGCGGCTATTCAGGT";
    public static final String GENES = "ACGT";

    public static void run() throws Exception {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(12));
        String filePath = "src/main/sequencias.txt";
        List<List<String>> blocos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String> buffer = new ArrayList<>(POPULATION_SIZE);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() >= DNA_LENGTH)
                    buffer.add(line.substring(0, DNA_LENGTH));
                if (buffer.size() == POPULATION_SIZE) {
                    blocos.add(new ArrayList<>(buffer));
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                blocos.add(new ArrayList<>(buffer));
            }
        }

        IndividualJava melhorGlobal = blocos.parallelStream()
                .map(GeneticDNAFinderParallelStream::executar)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (melhorGlobal != null) {
            System.out.println("=== MELHOR RESULTADO GLOBAL ===");
            System.out.println("DNA: " + melhorGlobal.chromosome);
            System.out.println("Fitness: " + melhorGlobal.fitness);
            System.out.println("===============================");
        }
    }

    private static IndividualJava executar(List<String> blocos) {
        List<IndividualJava> population = new ArrayList<>(blocos.size());
        for (String gene : blocos) population.add(new IndividualJava(gene, TARGET));
        List<IndividualJava> buffer = new ArrayList<>(blocos.size());

        int generation = 0;
        Random rand = new Random(50);

        while (generation < MAX_GENERATIONS) {
            Collections.sort(population);
            IndividualJava best = population.get(0);
            if (best.fitness == DNA_LENGTH) break;

            buffer.clear();

            int realPopulationSize = population.size();
            int eliteCount = Math.max((10 * realPopulationSize) / 100, 1);
            buffer.addAll(population.subList(0, eliteCount));

            int offspringCount = Math.max((90 * realPopulationSize) / 100, 1);
            for (int i = 0; i < offspringCount; i++) {
                IndividualJava parent1 = population.get(rand.nextInt(realPopulationSize / 2));
                IndividualJava parent2 = population.get(rand.nextInt(realPopulationSize / 2));
                buffer.add(parent1.mate(parent2, TARGET, rand));
            }

            List<IndividualJava> tmp = population;
            population = buffer;
            buffer = tmp;

            generation++;
        }
        return population.get(0);
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}