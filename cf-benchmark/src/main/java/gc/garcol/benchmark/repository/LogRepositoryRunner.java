package gc.garcol.benchmark.repository;

import gc.garcol.journal.core.LogRepository;
import gc.garcol.journal.core.global.Env;
import gc.garcol.journal.core.global.FileUtil;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author thaivc
 * @since 2024
 */
public class LogRepositoryRunner
{

    public static void main(String[] args) throws RunnerException
    {
        FileUtil.createDirectoryNX(Env.LOG_DIR);
        FileUtil.createDirectoryNX("benchmark-result");
        Options options = new OptionsBuilder()
            .include(LogRepository.class.getSimpleName())
            .resultFormat(ResultFormatType.JSON)
            .result("benchmark-result/log-repository-jmh-result.json")
            .build();
        new Runner(options).run();
    }

}
