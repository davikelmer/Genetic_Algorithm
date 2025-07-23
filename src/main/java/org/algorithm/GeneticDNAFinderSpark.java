package org.algorithm;

import org.apache.spark.SparkConf;

import java.util.*;
import org.apache.spark.api.java.function.FlatMapFunction;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;


public class GeneticDNAFinderSpark {

    public static final int POPULATION_SIZE = 50000;
    public static final int DNA_LENGTH = 20;
    public static final int MAX_GENERATIONS = 1000;
    public static final String TARGET = "CACCTTGCGGCTATTCAGGT";

    public static void run() {
        int cores = Runtime.getRuntime().availableProcessors();

        SparkConf conf = new SparkConf()
                .setAppName("GeneticDNAFinderSpark")
                .setMaster("local[" + cores + "]");
        try (JavaSparkContext sc = new JavaSparkContext(conf)) {
            sc.setLogLevel("ERROR");
            String filePath = "src/main/sequencias.txt";

            JavaRDD<String> linhas = sc.textFile(filePath, cores);

            JavaRDD<String> dna = linhas.filter(line -> line.length() >= DNA_LENGTH)
                    .map(line -> line.substring(0, DNA_LENGTH));

            JavaRDD<List<String>> blocos = dna.mapPartitions((FlatMapFunction<Iterator<String>, List<String>>) iter -> {
                List<List<String>> blocos1 = new ArrayList<>();
                List<String> buffer = new ArrayList<>(POPULATION_SIZE);
                while (iter.hasNext()) {
                    buffer.add(iter.next());
                    if (buffer.size() == POPULATION_SIZE) {
                        blocos1.add(new ArrayList<>(buffer));
                        buffer.clear();
                    }
                }
                if (!buffer.isEmpty()) {
                    blocos1.add(new ArrayList<>(buffer));
                }
                return blocos1.iterator();
            });
            JavaRDD<IndividualJava> melhores = blocos.map(GeneticDNAFinderSpark::executar);
            IndividualJava melhorGlobal = melhores.reduce((a, b) -> a.fitness >= b.fitness ? a : b);

            System.out.println("=== MELHOR RESULTADO GLOBAL ===");
            System.out.println("DNA: " + melhorGlobal.chromosome);
            System.out.println("Fitness: " + melhorGlobal.fitness);
            System.out.println("===============================");

            sc.stop();
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

    public static void main(String[] args) {
        run();
    }
}

