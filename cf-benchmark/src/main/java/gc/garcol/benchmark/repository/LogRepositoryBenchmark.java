package gc.garcol.benchmark.repository;

import gc.garcol.counter.proto.CounterProto;
import gc.garcol.raft.proto.LogProto;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author thaivc
 * @since 2024
 */
public class LogRepositoryBenchmark
{

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations = 1)
    @Warmup(iterations = 1)
    @Fork(1)
    @Timeout(time = 10)
    @OperationsPerInvocation(10_000)  // This affects only the throughput measurement
    public void benchmarkWriteThroughput(LogWriterRepositoryPlan logRepositoryPlan, Blackhole blackhole) throws IOException
    {
        writeLog(logRepositoryPlan, blackhole);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Measurement(iterations = 1)
    @Warmup(iterations = 1)
    @Fork(1)
    @Timeout(time = 10)
    public void benchmarkWriteAverageTime(LogWriterRepositoryPlan logRepositoryPlan, Blackhole blackhole) throws IOException
    {
        writeLog(logRepositoryPlan, blackhole);
    }

    private void writeLog(LogWriterRepositoryPlan logRepositoryPlan, Blackhole blackhole) throws IOException
    {
        LogProto.LogEntry.Builder builder = LogProto.LogEntry.newBuilder();
        for (int j = 0; j < 10_000; j++)
        {
            var command = Math.random() > 0.5
                ? CounterProto.Command.newBuilder()
                .setIncrease(CounterProto.IncreaseCommand.newBuilder()
                    .setId(1)
                    .build()
                ).build()
                : CounterProto.Command.newBuilder()
                .setDecrease(CounterProto.DecreaseCommand.newBuilder()
                    .setId(1)
                    .build())
                .build();
            builder.addCommands(command.toByteString());
        }

        var raftLogs = builder.build();

        logRepositoryPlan.logRepository.writeLog(raftLogs);
        blackhole.consume(raftLogs);  // Prevent dead code elimination
    }
}
