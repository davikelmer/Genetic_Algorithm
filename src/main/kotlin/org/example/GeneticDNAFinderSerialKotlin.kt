package org.example

import java.io.BufferedReader
import java.io.FileReader
import kotlin.random.Random

const val POPULATION_SIZE = 100
const val DNA_LENGTH = 20
const val GENES = "ACGT"
const val MAX_GENERATIONS = 1000

fun generateRandomDNASequence(): String =
    (1..DNA_LENGTH).map { GENES.random() }.joinToString("")

class Individual(val chromosome: String, target: String) : Comparable<Individual> {
    val fitness: Int = calculateFitness(target)

    private fun calculateFitness(target: String): Int =
        chromosome.zip(target).count { it.first == it.second }

    fun mate(partner: Individual, target: String): Individual {
        val childChromosome = buildString {
            for (i in chromosome.indices) {
                val prob = Random.nextFloat()
                append(
                    when {
                        prob < 0.45 -> chromosome[i]
                        prob < 0.90 -> partner.chromosome[i]
                        else -> GENES.random()
                    }
                )
            }
        }
        return Individual(childChromosome, target)
    }

    override fun compareTo(other: Individual): Int = other.fitness.compareTo(this.fitness)
}

fun main() {
    val target = generateRandomDNASequence()
    println("Alvo: $target")

    val population = mutableListOf<Individual>()
    BufferedReader(FileReader("sequencias.txt")).use { reader ->
        var line: String?
        while (reader.readLine().also { line = it } != null && population.size < POPULATION_SIZE) {
            if (line!!.length >= DNA_LENGTH) {
                population.add(Individual(line!!.substring(0, DNA_LENGTH), target))
            }
        }
    }

    var generation = 0
    var found = false

    while (!found && generation < MAX_GENERATIONS) {
        population.sort()
        val best = population.first()
        println("Geração: $generation\tDNA: ${best.chromosome}\tFitness: ${best.fitness}")

        if (best.fitness == DNA_LENGTH) {
            found = true
            break
        }

        val newGeneration = mutableListOf<Individual>()
        val eliteSize = (0.10 * POPULATION_SIZE).toInt()
        newGeneration.addAll(population.take(eliteSize))

        val offspringCount = (0.90 * POPULATION_SIZE).toInt()
        repeat(offspringCount) {
            val parent1 = population.random()
            val parent2 = population.random()
            newGeneration.add(parent1.mate(parent2, target))
        }

        population.clear()
        population.addAll(newGeneration)
        generation++
    }

    println("\nMelhor resultado encontrado após $generation gerações:")
    println("DNA: ${population[0].chromosome}")
    println("Fitness: ${population[0].fitness}")
}
