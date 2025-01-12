package dev.subscripted.utils;

import dev.subscripted.Main;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Lorenz E.
 * @Date 09.01.2025
 * @Description This class manages YAML configuration files in a format suitable for Discord bots using JDA API.
 */
public class SmartConfig {

    private final File file;
    private final Yaml yaml;
    private Map<String, Object> config;

    /**
     * Constructor for SmartConfig.
     *
     * @param fileName Name of the YAML configuration file.
     */
    public SmartConfig(String fileName) {
        this.file = new File("./config", fileName);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("New configuration file created: " + fileName);
                }
            } catch (IOException e) {
                System.err.println("File could not be created: " + fileName);
                e.printStackTrace();
            }
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yaml = new Yaml(new Constructor(), new Representer(), options);
        this.config = loadFromFile();
    }

    /**
     * Loads the YAML configuration from the file.
     *
     * @return A map of the loaded configuration.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadFromFile() {
        try (Reader reader = new FileReader(file)) {
            Object data = yaml.load(reader);
            return data instanceof Map ? (Map<String, Object>) data : new HashMap<>();
        } catch (IOException e) {
            System.err.println("Error reading file: " + file.getName());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Saves the current configuration to the file.
     */
    @SneakyThrows
    private void saveToFile() {
        try (Writer writer = new FileWriter(file)) {
            yaml.dump(config, writer);
        }
    }

    public static SmartConfig load(String fileName) {
        return new SmartConfig(fileName);
    }

    private boolean pathHasValidValue(String path) {
        return get(path) != null;
    }

    public void setString(String path, String value) {
        if (!pathHasValidValue(path)) {
            set(path, value);
        } else {
            System.out.println("Path already exists: " + path + ". Value not overwritten.");
        }
    }

    public void setInt(String path, int value) {
        if (!pathHasValidValue(path)) {
            set(path, value);
        } else {
            System.out.println("Path already exists: " + path + ". Value not overwritten.");
        }
    }

    public void setBool(String path, boolean value) {
        if (!pathHasValidValue(path)) {
            set(path, value);
        } else {
            System.out.println("Path already exists: " + path + ". Value not overwritten.");
        }
    }

    public void addList(String path, List<?> list) {
        if (!pathHasValidValue(path)) {
            set(path, list);
        } else {
            System.out.println("Path already exists: " + path + ". Value not overwritten.");
        }
    }

    public void overwrite(String path, Object value) {
        set(path, value);
    }

    public void clearPath(String path) {
        set(path, null);
    }

    public void save() {
        saveToFile();
    }

    public String getString(String path) {
        Object value = get(path);
        return value instanceof String ? (String) value : null;
    }

    public int getInt(String path) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    public boolean getBool(String path) {
        Object value = get(path);
        return value instanceof Boolean && (Boolean) value;
    }

    @SuppressWarnings("unchecked")
    public List<?> getList(String path) {
        Object value = get(path);
        return value instanceof List ? (List<?>) value : null;
    }

    public <T> T get(String path, Class<T> type) {
        Object value = get(path);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException("Value at path \"" + path + "\" is not of type " + type.getName());
    }

    @SuppressWarnings("unchecked")
    private Object get(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return null;
            }
        }

        return current.get(keys[keys.length - 1]);
    }

    private void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < keys.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new HashMap<>());
        }

        current.put(keys[keys.length - 1], value);
        saveToFile();
    }

    public ArrayList<?> getKeys(String path) {
        Object section = get(path);
        if (section instanceof Map) {
            return new ArrayList<>(((Map<?, ?>) section).keySet());
        }
        return (ArrayList<?>) Collections.emptyList();
    }

    public void debugConfig(String fileName) {
        System.out.println(config);
    }

    @SneakyThrows
    public String toJSON(String fileName) {
        return yaml.dump(config);
    }

    /**
     * @param colorValue
     * @return
     * @Disclaimer This Method is only for EmbedColor settings and will not work for other methods
     */
    public Color getColorFromConfig(String colorValue) {
        try {
            if (colorValue.startsWith("#")) {
                return Color.decode(colorValue);
            } else {
                try {
                    java.lang.reflect.Field field = Color.class.getField(colorValue.toLowerCase());
                    return (Color) field.get(null);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Ungültiger Farbwert: " + colorValue);
                }
            }
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    /**
     * Fügt Felder aus einer Konfiguration zum EmbedBuilder hinzu und ersetzt Platzhalter.
     *
     * @param dmembed      Der EmbedBuilder, zu dem die Felder hinzugefügt werden sollen.
     * @param fields       Die Liste der Felder aus der Konfiguration.
     * @param placeholders Eine Map mit Platzhaltern und deren Werten.
     */
    public void addFieldsToEmbed(EmbedBuilder dmembed, List<Map<String, Object>> fields, Map<String, String> placeholders) {
        if (fields == null || fields.isEmpty()) {
            System.err.println("Die Liste der Felder ist leer oder null.");
            return;
        }

        for (Map<String, Object> field : fields) {
            if (field == null) {
                System.err.println("Ein Feld in der Liste ist null. Überspringe es.");
                continue;
            }

            String title = (String) field.get("title");
            String content = (String) field.get("content");
            boolean inline = (boolean) field.getOrDefault("inline", false);

            if (title == null || content == null) {
                System.err.println("Ein Feld hat entweder keinen Titel oder keinen Inhalt: " + field);
                continue;
            }

            // Platzhalter ersetzen, nur wenn vorhanden und valide
            if (placeholders != null && !placeholders.isEmpty()) {
                for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
                    String key = placeholder.getKey();
                    String value = placeholder.getValue();

                    // Null-Werte überspringen
                    if (key != null && value != null) {
                        content = content.replace(key, value);
                    }
                }
            }

            dmembed.addField(title, content, inline);
        }
    }


    /**
     * Erstellt eine Beschreibung, indem Platzhalter in einer Liste von Strings ersetzt werden.
     *
     * @param descriptionLines Die Liste der Beschreibungslinien.
     * @param placeholders     Eine Map mit Platzhaltern und deren Werten.
     * @return Die vollständige Beschreibung mit ersetzten Platzhaltern.
     */
    public String makeDescription(List<String> descriptionLines, Map<String, String> placeholders) {
        if (descriptionLines == null || descriptionLines.isEmpty()) {
            return "";
        }
        // Platzhalter für jede Linie ersetzen
        return descriptionLines.stream()
                .map(line -> replacePlaceholders(line, placeholders))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Ersetzt Platzhalter in einem Text durch die entsprechenden Werte aus einer Map.
     *
     * @param text         Der Text mit Platzhaltern.
     * @param placeholders Eine Map mit Platzhaltern und deren Werten.
     * @return Der Text mit ersetzten Platzhaltern.
     */
    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null) {
            return "";
        }

        if (placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            String key = placeholder.getKey();
            String value = placeholder.getValue();

            if (key != null && value != null) {
                text = text.replace(key, value);
            }
        }

        return text;
    }


    public String makeFooter(String text, Map<String, String> placeholders) {
        return replacePlaceholders(text, placeholders);
    }

}
