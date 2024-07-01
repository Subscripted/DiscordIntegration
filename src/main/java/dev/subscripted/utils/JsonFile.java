package dev.subscripted.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Diese Klasse ist für das Laden und Speichern von Konfigurationsdaten im JSON-Format zuständig.
 * Sie enthält Methoden zum Erstellen und Laden der Datei, die den Bot-Token speichert.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JsonFile {

    // Die Datei, in der der Token gespeichert wird
    @Getter
    final File file = new File("config.json");

    /**
     * Lädt den Token aus der JSON-Konfigurationsdatei.
     * Wenn die Datei nicht existiert, wird sie erstellt und mit einem Platzhalter-Token gefüllt.
     *
     * @return Der Token als String.
     */
    @SneakyThrows
    public String loadTokenFile() {
        if (!file.exists()) {
            if (!file.createNewFile()) {
                System.out.println("Could not create a TokenConfig file.");
                return null;
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", "PASTE_HERE_YOUR_TOKEN");
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObject.toString());
            }
        }
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(file));
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        return jsonObject.get("token").getAsString();
    }
}
