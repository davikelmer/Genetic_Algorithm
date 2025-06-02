package org.algorithm;

import java.io.*;
import java.util.*;

public class GeneticDNAFinderSerialJava {

    private static final int POPULATION_SIZE = 100000;
    private static final int DNA_LENGTH = 20;
    private static final int MAX_GENERATIONS = 1000;
    private static final String TARGET = "CACCTTGCGGCTATTCAGGT";
    static final String GENES = "ACGT";

    public static void run() throws IOException {
        System.out.println("Alvo: " + TARGET);

        try (BufferedReader reader = new BufferedReader(new FileReader("sequencias.txt"))) {
            String line;
            List<String> buffer = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.length() >= DNA_LENGTH) {
                    buffer.add(line.substring(0, DNA_LENGTH));
                }
                if (buffer.size() == POPULATION_SIZE) {
                    executar(buffer);
                    buffer.clear();
                }
            }
        }
    }

    private static void executar(List<String> blocos) {
        List<IndividualJava> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new IndividualJava(blocos.get(i), TARGET));
        }

        int generation = 0;
        boolean found = false;
        Random rand = new Random(50);

        while (!found && generation < MAX_GENERATIONS) {
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

        System.out.println("\nMelhor resultado encontrado após " + generation + " gerações:");
        System.out.println("DNA: " + population.get(0).chromosome);
        System.out.println("Fitness: " + population.get(0).fitness);
        System.out.println("------------------------------");
    }

    public static void main(String[] args) throws IOException {
        run();
    }
}
