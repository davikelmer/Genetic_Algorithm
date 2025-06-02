package org.algorithm

import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlinx.coroutines.channels.Channel


object GeneticDNAFinderCorroutineKotlin {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run() = coroutineScope {
        val numCores = Runtime.getRuntime().availableProcessors()
        val numConsumers = numCores * 4
        println("Alvo: $TARGET")
        val channel = Channel<List<String>>(capacity = numConsumers * 2)
        val producer = launch(Dispatchers.IO) {
            File("sequencias.txt").bufferedReader().use { reader ->
                val buffer = mutableListOf<String>()
                var line = reader.readLine()
                while (line != null) {
                    if (line.length >= DNA_LENGTH) {
                        buffer.add(line.substring(0, DNA_LENGTH))
                    }
                    if (buffer.size == POPULATION_SIZE) {
                        channel.send(ArrayList(buffer))
                        buffer.clear()
                    }
                    line = reader.readLine()
                }
                if (buffer.isNotEmpty()) {
                    channel.send(ArrayList(buffer))
                }
                repeat(numConsumers) {
                    channel.send(emptyList())
                }
            }
        }
        val consumers = List(numConsumers) {
            launch(Dispatchers.Default) {
                for (bloco in channel) {
                    if (bloco.isEmpty()) break
                    executar(bloco)
                }
            }
        }
        producer.join()
        consumers.forEach { it.join() }
    }


    fun executar(blocos: List<String>) {
        val population = blocos.map { IndividualKotlin(it, TARGET) }.toMutableList()

        var generation = 0
        val rand = Random(50)
        val realPopulationSize = population.size

        if (realPopulationSize == 0) return

        while (generation < MAX_GENERATIONS) {
            population.sort()
            val best = population.first()

            if (best.fitness == DNA_LENGTH) break

            val newGeneration = mutableListOf<IndividualKotlin>()

            var eliteCount = (10 * realPopulationSize) / 100
            eliteCount = eliteCount.coerceAtLeast(1)
            newGeneration.addAll(population.take(eliteCount))

            var offspringCount = (90 * realPopulationSize) / 100
            offspringCount = offspringCount.coerceAtLeast(1)

            repeat(offspringCount) {
                val parent1 = population[rand.nextInt(realPopulationSize / 2)]
                val parent2 = population[rand.nextInt(realPopulationSize / 2)]
                newGeneration.add(parent1.mate(parent2, TARGET, rand))
            }

            population.clear()
            population.addAll(newGeneration)

            generation++
        }


        val best = population.first()
            println("\nMelhor resultado encontrado após $generation gerações:")
            println("DNA: ${best.chromosome}")
            println("Fitness: ${best.fitness}")
            println("------------------------------")
    }
    fun runGeneticDNAFinderKotlin() = runBlocking {
        GeneticDNAFinderCorroutineKotlin.run()
    }
}



fun main() = runBlocking {
    GeneticDNAFinderCorroutineKotlin.run()
}
