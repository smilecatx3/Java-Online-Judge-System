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
    public static ExecutorService executor;

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
        executor = Executors.newFixedThreadPool(CONFIG.getInt("max_thread"));
    }

    public synchronized static Future<JudgeResult> judge(Judger judger, Judger.Mode mode) {
        return executor.submit(() -> {
            try {
                return judger.judge(mode);
            } finally {
                judger.clean();
            }
        });
    }

}
