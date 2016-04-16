package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tw.edu.ncku.csie.selab.jojs.judger.Judger;

public class JOJS {
    public static final JSONObject CONFIG;
    public static final String JAVA;
    private static final int MAX_THREADS; // Default = 4
    private static final boolean CLEAN_UP; // Defaut = true
    private static ExecutorService executor;
    private static int numRunningThreads = 0;

    static {
        JSONObject temp = null;
        try {
            temp = new JSONObject(IOUtils.toString(JOJS.class.getResourceAsStream("/data/config.json")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        CONFIG = temp;
        JAVA = CONFIG.getString("java");
        MAX_THREADS = CONFIG.has("max_threads") ? CONFIG.getInt("max_threads") : 4;
        CLEAN_UP = !CONFIG.has("cleanup") || CONFIG.getBoolean("cleanup");
    }

    public synchronized static Future<JudgeResult> judge(Judger judger, Judger.Mode mode) {
        if (numRunningThreads == 0)
            executor = Executors.newFixedThreadPool(CONFIG.getInt("max_thread"));

        return executor.submit(() -> {
            try {
                numRunningThreads++;
                return judger.judge(mode);
            } finally {
                if (CLEAN_UP)
                    judger.clean();
                numRunningThreads--;
                if (numRunningThreads == 0)
                    executor.shutdownNow();
            }
        });
    }

}
