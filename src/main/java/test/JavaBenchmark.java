package test;

import org.algorithm.GeneticDNAFinderSpark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.profile.StackProfiler;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class JavaBenchmark {

    @Benchmark
    public void testEvolutionJava(Blackhole bh) throws Exception {
        GeneticDNAFinderSpark.run();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JavaBenchmark.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(0)
                .measurementIterations(1)
                .forks(1)
                .addProfiler(StackProfiler.class)
                .build();

        new Runner(opt).run();
    }
}
