package tw.edu.ncku.csie.selab.jojs.rule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleParser {

    public static synchronized Map<String, List<MethodRule>> parse(JSONArray rules) {
        Map<String, List<MethodRule>> ruleMap = new HashMap<>();

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

        return ruleMap;
    }

    public static synchronized String getDescription(Map<String, List<MethodRule>> ruleMap) {
        StringBuilder builder = new StringBuilder("Your code should contain at least: \n\n");
        for (Map.Entry<String, List<MethodRule>> rule : ruleMap.entrySet()) {
            builder.append("class ").append(rule.getKey()).append(" { \n");
            for (MethodRule methodRule : rule.getValue()) {
                builder.append("    ");
                builder.append(methodRule.returnType).append(" ");
                builder.append(methodRule.name).append(" (");
                for (String type : methodRule.parameterTypes)
                    builder.append(type).append(", ");
                builder.delete(builder.lastIndexOf(","), builder.length()).append("); \n");
            }
            builder.append("} \n\n");
        }
        return builder.toString().trim();
    }

}
