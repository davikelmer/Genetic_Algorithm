package test;

import org.algorithm.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.profile.StackProfiler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class JavaBenchmark {

    @Benchmark
    public void testEvolutionKotlin(Blackhole bh) throws IOException, InterruptedException {
        GeneticDNAFinderPlatformJava.run();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(JavaBenchmark.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(0)
                .measurementIterations(3)
                .forks(1)
                .addProfiler(StackProfiler.class)
                .build();

        new Runner(opt).run();
    }
}
