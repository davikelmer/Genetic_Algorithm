package org.algorithm;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class GeneticDNAFinderPlatformJava {
    private static final int POPULATION_SIZE = 50000;
    private static final int DNA_LENGTH = 20;
    private static final int MAX_GENERATIONS = 1000;
    private static final String TARGET = "CACCTTGCGGCTATTCAGGT";
    static final String GENES = "ACGT";
    private static final int QUEUE_CAPACITY = 4;

    public static void run() throws IOException, InterruptedException {
        int numCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Alvo: " + TARGET);

        BlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        ExecutorService executor = Executors.newFixedThreadPool(numCores);

        Thread producer = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader("sequencias.txt"))) {
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
            executor.submit(() -> {
                try {
                    while (true) {
                        List<String> bloco = queue.take();
                        if (bloco.isEmpty()) break;
                        executar(bloco);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        producer.join();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

    private static void executar(List<String> blocos) {
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
            int eliteCount = (10 * POPULATION_SIZE) / 100;
            newGeneration.addAll(population.subList(0, eliteCount));

            int offspringCount = (90 * POPULATION_SIZE) / 100;
            for (int i = 0; i < offspringCount; i++) {
                IndividualJava parent1 = population.get(rand.nextInt(POPULATION_SIZE / 2));
                IndividualJava parent2 = population.get(rand.nextInt(POPULATION_SIZE / 2));
                newGeneration.add(parent1.mate(parent2, TARGET, rand));
            }

            population = newGeneration;
            generation++;
        }

        IndividualJava best = population.get(0);
            System.out.println("\nMelhor resultado encontrado após " + generation + " gerações:");
            System.out.println("DNA: " + best.chromosome);
            System.out.println("Fitness: " + best.fitness);
            System.out.println("------------------------------");

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        run();
    }
}


