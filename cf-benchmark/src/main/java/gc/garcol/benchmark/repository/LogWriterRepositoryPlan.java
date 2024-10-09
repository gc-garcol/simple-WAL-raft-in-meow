package gc.garcol.benchmark.repository;

import gc.garcol.journal.core.LogRepository;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;

/**
 * @author thaivc
 * @since 2024
 */
@State(Scope.Benchmark)
public class LogWriterRepositoryPlan
{

    LogRepository logRepository;

    @Setup(Level.Trial)
    public void setUp()
    {
        logRepository = new LogRepository();
    }

    @TearDown(Level.Trial)
    public void teardown() throws IOException
    {
        if (logRepository != null)
        {
            logRepository.close();
        }
    }

}
