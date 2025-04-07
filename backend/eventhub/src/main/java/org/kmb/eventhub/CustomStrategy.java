package org.kmb.eventhub;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.codegen.GeneratorStrategy.Mode;
import org.jooq.meta.Definition;

public class CustomStrategy extends DefaultGeneratorStrategy {

    @Override
    public String getJavaMemberName(Definition definition, Mode mode) {
        return toCamelCase(definition.getOutputName());
    }

    private String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char ch : input.toCharArray()) {
            if (ch == '_') {
                upperNext = true;
            } else if (upperNext) {
                result.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
