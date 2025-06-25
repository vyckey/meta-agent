package org.metaagent.framework.core.model.prompt.formatter;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * {@link StringFormatter} manager
 *
 * @author vyckey
 */
public final class StringFormatterManager {
    private static volatile StringFormatterManager instance = null;
    private static volatile String defaultFormatter = "slf4j";

    private final Map<String, StringFormatter> formatterMap = Maps.newConcurrentMap();

    private StringFormatterManager() {
    }

    public static StringFormatterManager getInstance() {
        if (instance == null) {
            synchronized (StringFormatterManager.class) {
                if (instance == null) {
                    StringFormatterManager manager = new StringFormatterManager();
                    ServiceLoader.load(StringFormatter.class).forEach(manager::register);
                    instance = manager;
                }
            }
        }
        return instance;
    }

    public static void setDefaultFormatter(String formatterName) {
        StringFormatterManager.defaultFormatter = formatterName;
    }

    public static StringFormatter getDefaultFormatter() {
        return getInstance().getFormatter(defaultFormatter);
    }

    public Set<String> getFormatterNames() {
        return formatterMap.keySet();
    }

    public void register(StringFormatter formatter) {
        formatterMap.put(formatter.name(), formatter);
    }

    public StringFormatter getFormatter(String name) {
        return formatterMap.get(name);
    }

}
