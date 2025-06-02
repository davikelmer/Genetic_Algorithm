package org.algorithm

import java.io.File
import java.util.Random

const val POPULATION_SIZE = 100000
const val DNA_LENGTH = 20
const val MAX_GENERATIONS = 1000
const val TARGET = "CACCTTGCGGCTATTCAGGT"
const val GENES = "ACGT"

object GeneticDNAFinderSerialKotlin {

    fun run() {
        println("Alvo: $TARGET")

        val reader = File("sequencias.txt").bufferedReader()
        val buffer = mutableListOf<String>()

        reader.useLines { lines ->
            for (line in lines) {
                if (line.length >= DNA_LENGTH) {
                    buffer.add(line.substring(0, DNA_LENGTH))
                }
                if (buffer.size == POPULATION_SIZE) {
                    executar(buffer)
                    buffer.clear()
                }
            }
        }
    }

    private fun executar(blocos: List<String>) {
        val population = mutableListOf<IndividualKotlin>()
        for (i in 0 until POPULATION_SIZE) {
            population.add(IndividualKotlin(blocos[i], TARGET))
        }

        var generation = 0
        var found = false
        val rand = Random(50)

        while (!found && generation < MAX_GENERATIONS) {
            population.sort()
            val best = population.first()

            if (best.fitness == DNA_LENGTH) break

            val newGeneration = mutableListOf<IndividualKotlin>()
            val eliteCount = (10 * POPULATION_SIZE) / 100
            newGeneration.addAll(population.take(eliteCount))

            val offspringCount = (90 * POPULATION_SIZE) / 100
            repeat(offspringCount) {
                val parent1 = population[rand.nextInt(POPULATION_SIZE / 2)]
                val parent2 = population[rand.nextInt(POPULATION_SIZE / 2)]
                newGeneration.add(parent1.mate(parent2, TARGET, rand))
            }

            population.clear()
            population.addAll(newGeneration)
            generation++
        }

        println("\nMelhor resultado encontrado após $generation gerações:")
        println("DNA: ${population[0].chromosome}")
        println("Fitness: ${population[0].fitness}")
        println("------------------------------")
    }
}

class IndividualKotlin(
    val chromosome: String,
    target: String
) : Comparable<IndividualKotlin> {

    val fitness: Int = chromosome.zip(target).count { it.first == it.second }

    fun mate(partner: IndividualKotlin, target: String, rand: Random): IndividualKotlin {
        val childChromosome = buildString {
            for (i in chromosome.indices) {
                val prob = rand.nextFloat()
                append(
                    when {
                        prob < 0.45 -> chromosome[i]
                        prob < 0.90 -> partner.chromosome[i]
                        else -> GENES[rand.nextInt(GENES.length)]
                    }
                )
            }
        }
        return IndividualKotlin(childChromosome, target)
    }

    override fun compareTo(other: IndividualKotlin): Int =
        other.fitness.compareTo(this.fitness)
}
fun main(args: Array<String>) {
    GeneticDNAFinderSerialKotlin.run()
}


