package com.onarandombox.multiverseinventories.util;

import com.dumptruckman.minecraft.util.Logging;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.json.simple.JSONValue;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class JsonConfiguration extends FileConfiguration {

    protected static final String BLANK_CONFIG = "{}\n";
    //private static final JSONParser JSON_PARSER = ;

    @Override
    public String saveToString() {
        String dump = JSONValue.toJSONString(buildMap(getValues(false)));

        if (dump.equals(BLANK_CONFIG)) {
            dump = "";
        }

        return dump;
    }

    private Map<String, Object> buildMap(final Map<?, ?> map) {
        final Map<String, Object> result = new LinkedHashMap<String, Object>(map.size());
        try {
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection) {
                    result.put(entry.getKey().toString(), buildMap(((ConfigurationSection) entry.getValue()).getValues(false)));
                } else if (entry.getValue() instanceof Map) {
                    result.put(entry.getKey().toString(), buildMap(((Map) entry.getValue())));
                } else if (entry.getValue() instanceof List) {
                    result.put(entry.getKey().toString(), buildList((List) entry.getValue()));
                } else if (entry.getValue() instanceof ConfigurationSerializable) {
                    ConfigurationSerializable serializable = (ConfigurationSerializable) entry.getValue();
                    Map<String, Object> values = new LinkedHashMap<String, Object>();
                    values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
                    values.putAll(serializable.serialize());
                    result.put(entry.getKey().toString(), buildMap(values));
                } else {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
            }
        } catch (Exception e) {
            Logging.getLogger().log(Level.WARNING, "Error while building configuration map.", e);
        }
        return result;
    }

    private List<Object> buildList(final List<?> list) {
        final List<Object> result = new ArrayList<Object>(list.size());
        try {
            for (final Object o : list) {
                if (o instanceof ConfigurationSection) {
                    result.add(buildMap(((ConfigurationSection) o).getValues(false)));
                } else if (o instanceof Map) {
                    result.add(buildMap(((Map) o)));
                } else if (o instanceof List) {
                    result.add(buildList((List) o));
                } else if (o instanceof ConfigurationSerializable) {
                    ConfigurationSerializable serializable = (ConfigurationSerializable) o;
                    Map<String, Object> values = new LinkedHashMap<String, Object>();
                    values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
                    values.putAll(serializable.serialize());
                    result.add(buildMap(values));
                } else {
                    result.add(o);
                }
            }
        } catch (Exception e) {
            Logging.getLogger().log(Level.WARNING, "Error while building configuration list.", e);
        }
        return result;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");
        if (contents.isEmpty()) {
            return;
        }

        Map<?, ?> input;
        try {
            input = (Map<?, ?>) new JSONParser(JSONParser.USE_INTEGER_STORAGE).parse(contents);
        } catch (ParseException e) {
            throw new InvalidConfigurationException("Invalid JSON detected.");
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
        Object result = dealWithSerializedObjects(input);
        if (result instanceof Map) {
            input = (Map<?, ?>) result;
            for (Map.Entry<?, ?> entry : input.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();

                if (value instanceof Map) {
                    convertMapsToSections((Map<?, ?>) value, section.createSection(key));
                } else {
                    section.set(key, value);
                }
            }
        } else {
            section.set("", result);
        }
    }

    protected Object dealWithSerializedObjects(final Map<?, ?> input) {
        final Map<String, Object> output = new LinkedHashMap<String, Object>(input.size());
        for (final Map.Entry<?, ?> e : input.entrySet()) {
            if (e.getValue() instanceof Map) {
                output.put(e.getKey().toString(), dealWithSerializedObjects((Map<?, ?>) e.getValue()));
            }  else if (e.getValue() instanceof List) {
                output.put(e.getKey().toString(), dealWithSerializedObjects((List<?>) e.getValue()));
            } else {
                output.put(e.getKey().toString(), e.getValue());
            }
        }
        if (output.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            try {
                return ConfigurationSerialization.deserializeObject(output);
            } catch (IllegalArgumentException ex) {
                throw new YAMLException("Could not deserialize object", ex);
            }
        }
        return output;
    }

    protected Object dealWithSerializedObjects(final List<?> input) {
        final List<Object> output = new ArrayList<Object>(input.size());
        for (final Object o : input) {
            if (o instanceof Map) {
                output.add(dealWithSerializedObjects((Map<?, ?>) o));
            } else if (o instanceof List) {
                output.add(dealWithSerializedObjects((List<?>) o));
            } else {
                output.add(o);
            }
        }
        return output;
    }

    @Override
    protected String buildHeader() {
        return "";
    }

    @Override
    public JsonConfigurationOptions options() {
        if (options == null) {
            options = new JsonConfigurationOptions(this);
        }

        return (JsonConfigurationOptions) options;
    }

    public JsonConfiguration(final File file) {
        try {
            load(file);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file , ex);
        }
    }
}
