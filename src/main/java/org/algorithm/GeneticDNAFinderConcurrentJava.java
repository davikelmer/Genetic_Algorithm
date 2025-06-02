package org.algorithm;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class GeneticDNAFinderConcurrentJava {
    static final int POPULATION_SIZE = 100000;
    static final int DNA_LENGTH = 20;
    static final int MAX_GENERATIONS = 1000;
    static final String TARGET = "CACCTTGCGGCTATTCAGGT";

    static final BlockingQueue<List<String>> queue = new LinkedBlockingQueue<>(50);

    public static void run() throws IOException, InterruptedException {
        System.out.println("Alvo: " + TARGET);

        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        Thread readerThread = Thread.ofPlatform().start(() -> {
            try (BufferedReader br = new BufferedReader(new FileReader("sequencias.txt"))) {
                List<String> bloco = new ArrayList<>(POPULATION_SIZE);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.length() >= DNA_LENGTH) {
                        bloco.add(line.substring(0, DNA_LENGTH));
                    }
                    if (bloco.size() == POPULATION_SIZE) {
                        List<String> taskData = new ArrayList<>(bloco);
                        virtualExecutor.submit(() -> executar(taskData));
                        bloco.clear();
                    }
                }
                if (!bloco.isEmpty()) {
                    List<String> taskData = new ArrayList<>(bloco);
                    virtualExecutor.submit(() -> executar(taskData));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        readerThread.join();

        virtualExecutor.shutdown();
        if (!virtualExecutor.awaitTermination(1, TimeUnit.HOURS)) {
            virtualExecutor.shutdownNow();
        }
    }

    private static void executar(List<String> blocos) {
        List<IndividualJava> population = new ArrayList<>();
        for (String seq : blocos) {
            population.add(new IndividualJava(seq, TARGET));
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

        System.out.println("\nMelhor resultado após " + generation + " gerações:");
        System.out.println("DNA: " + population.get(0).chromosome);
        System.out.println("Fitness: " + population.get(0).fitness);
        System.out.println("------------------------------");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        run();
    }
}
