package tw.edu.ncku.csie.selab.jojs.rule;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodRule {
    public String name;
    public String returnType;
    public List<String> parameterTypes = new ArrayList<>();

    public MethodRule(String name, String returnType, String ... parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        Collections.addAll(this.parameterTypes, parameterTypes);
    }

    public MethodRule addParameterType(String type) {
        parameterTypes.add(type);
        return this;
    }

    public boolean equals(Method method) {
        boolean isEqual =
                method != null &&
                method.getName().equals(name) &&
                method.getReturnType().getName().equals(returnType) &&
                method.getParameterCount() == parameterTypes.size();

        if (isEqual) {
            Class[] params = method.getParameterTypes();
            for (int i=0; i<parameterTypes.size(); i++)
                isEqual = params[i].getName().equals(parameterTypes.get(i));
        }

        return isEqual;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(returnType).append(" ");
        builder.append(name).append("(");
        if (parameterTypes.size() > 0) {
            for (String type : parameterTypes)
                builder.append(type).append(", ");
            builder.delete(builder.lastIndexOf(","), builder.length()); // delete last comma
        }
        return builder.append("); \n").toString();
    }

    public String toString(String className) {
        return toString().replaceFirst(" ", " "+className+".");
    }
}
