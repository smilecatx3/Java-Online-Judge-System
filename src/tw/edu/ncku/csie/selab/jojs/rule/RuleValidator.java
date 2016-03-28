package tw.edu.ncku.csie.selab.jojs.rule;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleValidator {
    private File binFolder;
    private Map<String, List<MethodRule>> ruleMap = new HashMap<>();

    public RuleValidator(File binFolder, JSONArray rules) {
        this.binFolder = binFolder;

        for (int i=0; i<rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String className = rule.getString("class");
            JSONArray methodRules = rule.getJSONArray("method");

            // Parse method rules
            List<MethodRule> methodRuleList = new ArrayList<>();
            for (int j=0; j<methodRules.length(); j++) {
                JSONObject methodInfo = methodRules.getJSONObject(j);
                JSONArray methodParams = methodInfo.getJSONArray("params");
                MethodRule methodRule = new MethodRule(methodInfo.getString("name"), methodInfo.getString("return"));
                for (int k=0; k<methodParams.length(); k++)
                    methodRule.addParameterType(methodParams.getString(k));
                methodRuleList.add(methodRule);
            }

            ruleMap.put(className, methodRuleList);
        }
    }

    public boolean isValid() throws MalformedURLException, ClassNotFoundException {
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

    public String getRule() {
        StringBuilder builder = new StringBuilder("Your code should contain at least the following classes and methods: \n\n");
        for (Map.Entry<String, List<MethodRule>> rule : ruleMap.entrySet()) {
            builder.append("class ").append(rule.getKey()).append(" { \n");
            for (MethodRule methodRule : rule.getValue()) {
                builder.append("    ");
                builder.append(methodRule.returnType).append(" ");
                builder.append(methodRule.name).append(" (");
                if (methodRule.parameterTypes.size() > 0) {
                    for (String type : methodRule.parameterTypes)
                        builder.append(type).append(", ");
                    builder.delete(builder.lastIndexOf(","), builder.length());
                }
                builder.append("); \n");
            }
            builder.append("} \n\n");
        }
        return builder.toString().trim();
    }

}
