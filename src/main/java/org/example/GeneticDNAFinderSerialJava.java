package org.example;

import java.io.*;
import java.util.*;

public class GeneticDNAFinderSerialJava {

    private static final int POPULATION_SIZE = 100;
    private static final int DNA_LENGTH = 20;
    private static final String GENES = "ACGT";
    private static final int MAX_GENERATIONS = 1000;

    private static String generateRandomDNASequence() {
        Random rand = new Random(42);
        StringBuilder dna = new StringBuilder();
        for (int i = 0; i < GeneticDNAFinderSerialJava.DNA_LENGTH; i++) {
            dna.append(GENES.charAt(rand.nextInt(GENES.length())));
        }
        return dna.toString();
    }

    public static void main(String[] args) throws IOException {
        String target = generateRandomDNASequence();
        System.out.println("Alvo: " + target);

        // Gerar população inicial a partir do arquivo
        List<IndividualJava> population = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("sequencias.txt"))) {
            String line;
            while ((line = reader.readLine()) != null && population.size() < POPULATION_SIZE) {
                if (line.length() >= DNA_LENGTH) {
                    population.add(new IndividualJava(line.substring(0, DNA_LENGTH), target));
                }
            }
        }

        Random rand = new Random();
        int generation = 0;
        boolean found = false;

        while (!found && generation < MAX_GENERATIONS) {
            Collections.sort(population);
            IndividualJava best = population.get(0);
            System.out.println("Geração: " + generation + "\tDNA: " + best.chromosome + "\tFitness: " + best.fitness);

            if (best.fitness == DNA_LENGTH) {
                found = true;
                break;
            }

            List<IndividualJava> newGeneration = new ArrayList<>();
            int s = (10 * POPULATION_SIZE) / 100;
            newGeneration.addAll(population.subList(0, s));

            s = (90 * POPULATION_SIZE) / 100;
            for (int i = 0; i < s; i++) {
                IndividualJava parent1 = population.get(rand.nextInt(POPULATION_SIZE / 2));
                IndividualJava parent2 = population.get(rand.nextInt(POPULATION_SIZE / 2));
                newGeneration.add(parent1.mate(parent2, target));
            }

            population = newGeneration;
            generation++;
        }

        System.out.println("\nMelhor resultado encontrado após " + generation + " gerações:");
        System.out.println("DNA: " + population.get(0).chromosome);
        System.out.println("Fitness: " + population.get(0).fitness);
    }
}

