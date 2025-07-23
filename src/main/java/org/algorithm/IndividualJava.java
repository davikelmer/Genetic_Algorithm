package org.algorithm;

import java.util.Random;


public class IndividualJava implements Comparable<IndividualJava>, java.io.Serializable {
    public final String chromosome;
    public final int fitness;

    public IndividualJava(String chromosome, String target) {
        this.chromosome = chromosome;
        this.fitness = calculateFitness(target);
    }

    private int calculateFitness(String target) {
        int fitness = 0;
        for (int i = 0; i < chromosome.length(); i++) {
            if (chromosome.charAt(i) == target.charAt(i)) fitness++;
        }
        return fitness;
    }

    public IndividualJava mate(IndividualJava partner, String target, Random rand) {
        StringBuilder childChromosome = new StringBuilder(chromosome.length());
        for (int i = 0; i < chromosome.length(); i++) {
            float prob = rand.nextFloat();
            if (prob < 0.45) {
                childChromosome.append(this.chromosome.charAt(i));
            } else if (prob < 0.90) {
                childChromosome.append(partner.chromosome.charAt(i));
            } else {
                childChromosome.append(GeneticDNAFinderPlatformJava.GENES.
                        charAt(rand.nextInt(GeneticDNAFinderPlatformJava.GENES.length())));
            }
        }
        return new IndividualJava(childChromosome.toString(), target);
    }

    @Override
    public int compareTo(IndividualJava o) {
        return Integer.compare(o.fitness, this.fitness);
    }
}
