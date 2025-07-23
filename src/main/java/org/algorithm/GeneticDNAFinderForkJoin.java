package org.algorithm;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class GeneticDNAFinderForkJoin {
    public static final int POPULATION_SIZE = 10000;
    public static final int DNA_LENGTH = 20;
    public static final int MAX_GENERATIONS = 1000;
    public static final String TARGET = "CACCTTGCGGCTATTCAGGT";
    public static final String GENES = "ACGT";
    public static final int numcores = Runtime.getRuntime().availableProcessors();


    public static void run() throws Exception {
        ForkJoinPool forkJoinPool = new ForkJoinPool(numcores);
        List<ForkJoinTask<IndividualJava>> tasks = new ArrayList<>();
        String filePath = "src/main/sequencias.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String> buffer = new ArrayList<>(POPULATION_SIZE);
            while ((line = reader.readLine()) != null) {
                if (line.length() >= DNA_LENGTH)
                    buffer.add(line.substring(0, DNA_LENGTH));
                if (buffer.size() == POPULATION_SIZE) {
                    tasks.add(forkJoinPool.submit(new GeneticTask(new ArrayList<>(buffer))));
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                tasks.add(forkJoinPool.submit(new GeneticTask(new ArrayList<>(buffer))));
            }
        }

        IndividualJava bestOverall = null;
        for (ForkJoinTask<IndividualJava> task : tasks) {
            IndividualJava result = task.get();
            if (result != null && (bestOverall == null || result.fitness > bestOverall.fitness)) {
                bestOverall = result;
            }
        }

        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);

        if (bestOverall != null) {
            System.out.println("=== MELHOR RESULTADO GLOBAL ===");
            System.out.println("DNA: " + bestOverall.chromosome);
            System.out.println("Fitness: " + bestOverall.fitness);
            System.out.println("===============================");
        }
    }

    static class GeneticTask extends RecursiveTask<IndividualJava> {
        private final List<String> blocos;
        public GeneticTask(List<String> blocos) { this.blocos = blocos; }

        @Override
        protected IndividualJava compute() {
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
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}


