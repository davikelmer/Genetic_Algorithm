package org.example;

import java.util.Random;

import static org.example.GeneticDNAFinderSerialKotlinKt.GENES;

public class IndividualJava implements Comparable<IndividualJava> {
    String chromosome;
    int fitness;

    IndividualJava(String chromosome, String target) {
        this.chromosome = chromosome;
        this.fitness = calculateFitness(target);
    }

    int calculateFitness(String target) {
        int fitness = 0;
        for (int i = 0; i < chromosome.length(); i++) {
            if (chromosome.charAt(i) == target.charAt(i)) fitness++;
        }
        return fitness;
    }

    IndividualJava mate(IndividualJava partner, String target) {
        Random rand = new Random();
        StringBuilder childChromosome = new StringBuilder();
        for (int i = 0; i < chromosome.length(); i++) {
            float prob = rand.nextFloat();
            if (prob < 0.45) {
                childChromosome.append(this.chromosome.charAt(i));
            } else if (prob < 0.90) {
                childChromosome.append(partner.chromosome.charAt(i));
            } else {
                childChromosome.append(GENES.charAt(rand.nextInt(GENES.length())));
            }
        }
        return new IndividualJava(childChromosome.toString(), target);
    }

    @Override
    public int compareTo(IndividualJava o) {
        return Integer.compare(o.fitness, this.fitness); // maior fitness primeiro
    }
}
