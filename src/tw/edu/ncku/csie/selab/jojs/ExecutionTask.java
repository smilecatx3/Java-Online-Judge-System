package tw.edu.ncku.csie.selab.jojs;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.edu.ncku.csie.selab.jojs.judger.Judger;
import tw.edu.ncku.csie.selab.jojs.judger.ProgressReporter;
import tw.edu.ncku.csie.selab.jojs.rule.MethodRule;
import tw.edu.ncku.csie.selab.jojs.rule.RuleParser;

public class ExecutionTask {
    private JSONObject testcase;
    private File binFolder;
    private String entryPoint;
    private Judger.Mode mode;
    private ProgressReporter reporter;


    public ExecutionTask(File testcase, File binFolder, String entryPoint, Judger.Mode mode, ProgressReporter reporter) throws IOException {
        this.testcase = new JSONObject(FileUtils.readFileToString(testcase));
        this.binFolder = binFolder;
        this.entryPoint = entryPoint;
        this.mode = mode;
        this.reporter = reporter;
    }

    public JudgeResult execute() throws Exception {
        ProcessExecutor executor = testcase.has("timeout") ?
                new ProcessExecutor(testcase.getLong("timeout")) :
                new ProcessExecutor();
        JSONArray inputs = testcase.getJSONArray("inputs");
        JSONArray outputs = testcase.getJSONArray("outputs");

        if (testcase.has("rules")) {
            Map<String, List<MethodRule>> ruleMap = RuleParser.parse(testcase.getJSONArray("rules"));
            if (!validateClass(ruleMap))
                throw new JudgeException(RuleParser.getDescription(ruleMap), JudgeException.ErrorCode.INVALID_CLASS);
        }

        double elapsedTime = 0;
        ExecutionResult[] results = new ExecutionResult[outputs.length()];
        for (int i=0; i<outputs.length(); i++) {
            reporter.reportProgress(0.5+((i+1.0)/outputs.length())*0.5, "Executing");

            // Construct command
            File inputFile = null; // stdin
            List<String> command = new ArrayList<>();
            Collections.addAll(command, JOJS.JAVA, "-Dfile.encoding=UTF-8", "-Duser.language=en", "-Djava.security.manager", "-cp", binFolder.getAbsolutePath(), entryPoint);
            if (inputs.length() > 0) {
                JSONArray input = inputs.getJSONArray(i);
                if (mode == Judger.Mode.STANDARD) {
                    for (int j = 0; j < input.length(); j++)
                        command.add(input.getString(j).replace("\"", "\\\""));
                } else if (mode == Judger.Mode.STDIN) {
                    inputFile = createInputFile(input);
                }
            }

            // Execute the program
            ProcessExecutor.Result result = executor.execute(command.toArray(new String[command.size()]), inputFile);
            if (result.isTimeout) {
                results[i] = new ExecutionResult(false, "Time limit exceeded");
            } else {
                String answer = result.output.replace("\r", "").trim(); // Your output
                String output = outputs.getString(i).replace("\r", "").trim(); // Expected output
                boolean passed = answer.equals(output);
                results[i] = new ExecutionResult(passed, answer);
            }
            elapsedTime += result.runtime;
        }
        return new JudgeResult(testcase, results, elapsedTime/results.length);
    }

    // For stdin
    private File createInputFile(JSONArray input) throws IOException {
        StringBuilder data = new StringBuilder();
        for (int i=0; i<input.length(); i++)
            data.append(input.getString(i)).append("\n");
        File file = new File(binFolder, String.valueOf(System.currentTimeMillis()));
        FileUtils.writeStringToFile(file, data.toString());
        return file;
    }

    private boolean validateClass(Map<String, List<MethodRule>> ruleMap) throws MalformedURLException, ClassNotFoundException {
        // Load all classes under the bin folder
        ClassLoader classLoader = new URLClassLoader(new URL[]{binFolder.toURI().toURL()});
        Map<String, Map<String, Method>> classMap = new HashMap<>(); // [class_name, [method_name, method]]
        for (File file : FileUtils.listFiles(binFolder, new String[] {"class"}, true)) {
            String className = file.getAbsolutePath()
                    .replace(binFolder.getAbsolutePath(), "")
                    .replace(File.separator, ".")
                    .replace(".class", "");
            Class c = classLoader.loadClass(className.startsWith(".") ? className.substring(1) : className);
            Method[] methods = c.getDeclaredMethods();
            Map<String, Method> methodMap = new HashMap<>();
            for (Method method : methods)
                methodMap.put(method.getName(), method);
            classMap.put(c.getSimpleName(), methodMap);
        }

        // Validate
        for (Map.Entry<String, List<MethodRule>> rule : ruleMap.entrySet()) {
            String className = rule.getKey();
            List<MethodRule> methodRules = rule.getValue();

            if (classMap.containsKey(className)) {
                Map<String, Method> methodMap = classMap.get(className);
                for (MethodRule methodRule : methodRules)
                    if (!methodRule.equals(methodMap.get(methodRule.name)))
                        return false;
            } else {
                return false;
            }
        }
        return true;
    }


}
