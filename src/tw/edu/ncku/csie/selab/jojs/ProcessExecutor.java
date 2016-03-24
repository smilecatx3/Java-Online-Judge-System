package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ProcessExecutor {
    public class Result {
        public String output;
        public long runtime;
        public boolean isTimeout = false;

        public Result(String output, long runtime, boolean isTimeout) {
            this.output = output;
            this.runtime = runtime;
            this.isTimeout = isTimeout;
        }
    }

    private long timeout;

    public ProcessExecutor() {
        this(JOJS.CONFIG.getLong("timeout"));
    }

    public ProcessExecutor(long timeout) {
        this.timeout = timeout;
    }

    public Result execute(String[] command, File inputFile) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        if (inputFile != null)
            processBuilder.redirectInput(inputFile);

        long start = System.currentTimeMillis();
        Process process = processBuilder.start();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> IOUtils.toString(process.getInputStream(), "UTF-8"));

        try {
            boolean hasExisted = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            if (hasExisted) {
                return new Result(future.get(), System.currentTimeMillis() - start, false);
            } else {
                future.cancel(true);
                process.destroyForcibly();
                return new Result(null, timeout, true);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new Result(ExceptionUtils.getMessage(e), 0, false);
        } finally {
            if (process.isAlive())
                process.destroyForcibly();
            executorService.shutdownNow();
        }
    }

}
