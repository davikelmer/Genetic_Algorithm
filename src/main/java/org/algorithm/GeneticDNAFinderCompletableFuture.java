package org.algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class GeneticDNAFinderCompletableFuture {
    private static final int POPULATION_SIZE = 50000;
    private static final int DNA_LENGTH = 20;
    private static final int MAX_GENERATIONS = 1000;
    private static final String TARGET = "CACCTTGCGGCTATTCAGGT";
    private static final int QUEUE_CAPACITY = 4;

    public static void run() throws Exception {
        int numCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Alvo: " + TARGET);

        BlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        ExecutorService executor = Executors.newFixedThreadPool(numCores);
        List<CompletableFuture<IndividualJava>> results = new ArrayList<>();

        Thread producer = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\davik\\Downloads\\apache-jmeter-5.6.3\\apache-jmeter-5.6.3\\bin\\sequencias.txt"))) {
                String line;
                List<String> buffer = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    if (line.length() >= DNA_LENGTH) {
                        buffer.add(line.substring(0, DNA_LENGTH));
                    }
                    if (buffer.size() == POPULATION_SIZE) {
                        queue.put(new ArrayList<>(buffer));
                        buffer.clear();
                    }
                }
                if (!buffer.isEmpty()) {
                    queue.put(new ArrayList<>(buffer));
                }
                for (int i = 0; i < numCores; i++) {
                    queue.put(Collections.emptyList());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        producer.start();

        for (int i = 0; i < numCores; i++) {
            CompletableFuture<IndividualJava> future = CompletableFuture.supplyAsync(() -> {
                IndividualJava melhor = null;
                try {
                    while (true) {
                        List<String> bloco = queue.take();
                        if (bloco.isEmpty()) break;
                        IndividualJava result = executar(bloco);
                        if (melhor == null || result.fitness > melhor.fitness) {
                            melhor = result;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return melhor;
            }, executor);
            results.add(future);
        }

        producer.join();

        CompletableFuture<Void> allDone = CompletableFuture.allOf(results.toArray(new CompletableFuture[0]));
        allDone.join();

        IndividualJava bestOverall = null;
        for (CompletableFuture<IndividualJava> future : results) {
            IndividualJava result = future.join();
            if (result != null && (bestOverall == null || result.fitness > bestOverall.fitness)) {
                bestOverall = result;
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        if (bestOverall != null) {
            System.out.println("=== MELHOR RESULTADO GLOBAL ===");
            System.out.println("DNA: " + bestOverall.chromosome);
            System.out.println("Fitness: " + bestOverall.fitness);
            System.out.println("===============================");
        }
    }

    private static IndividualJava executar(List<String> blocos) {
        List<IndividualJava> population = new ArrayList<>();
        for (String gene : blocos) {
            population.add(new IndividualJava(gene, TARGET));
        }
        int generation = 0;
        Random rand = new Random(50);

        while (generation < MAX_GENERATIONS) {
            Collections.sort(population);
            IndividualJava best = population.get(0);
            if (best.fitness == DNA_LENGTH) break;

            List<IndividualJava> newGeneration = new ArrayList<>();
            int realPopulationSize = population.size();
            int eliteCount = (10 * realPopulationSize) / 100;
            newGeneration.addAll(population.subList(0, eliteCount));

            int offspringCount = (90 * realPopulationSize) / 100;
            for (int i = 0; i < offspringCount; i++) {
                IndividualJava parent1 = population.get(rand.nextInt(realPopulationSize / 2));
                IndividualJava parent2 = population.get(rand.nextInt(realPopulationSize / 2));
                newGeneration.add(parent1.mate(parent2, TARGET, rand));
            }
            population = newGeneration;
            generation++;
        }

        return population.get(0);
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}
