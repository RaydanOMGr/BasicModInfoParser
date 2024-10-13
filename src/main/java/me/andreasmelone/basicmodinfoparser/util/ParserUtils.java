package me.andreasmelone.basicmodinfoparser.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.andreasmelone.basicmodinfoparser.BasicModInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;

public class ParserUtils {
    /**
     * Compares an array of paths to a single path to check if any match after normalisation.<p>
     * This ensures that two paths are considered equal, even if their string representations differ
     * (e.g., {@code run/embeddium.jar} and {@code .\run\embeddium.jar}).
     *
     * @param paths An array of paths to compare.
     * @param path2 The path to compare against.
     * @return {@code true} if any of the provided paths are equal to {@code path2} after normalisation, otherwise {@code false}.
     */
    public static boolean comparePaths(String[] paths, String path2) {
        for(String path : paths) {
            Path normalizedPath1 = Paths.get(path).normalize();
            Path normalizedPath2 = Paths.get(path2).normalize();
            if(normalizedPath1.equals(normalizedPath2)) return true;
        }
        return false;
    }

    /**
     * Reads the entire content of an {@link InputStream} and returns it as a string.
     *
     * @param in The {@link InputStream} to read from.
     * @return The content of the InputStream as a string.
     * @throws IOException If an I/O error occurs during reading.
     */
    public static String readEverythingAsString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder(in.available());
        int readBytes;
        byte[] buffer = new byte[1024];
        while((readBytes = in.read(buffer)) > 0) {
            sb.append(new String(buffer, 0, readBytes));
        }
        return sb.toString();
    }

    /**
     * Finds a value by key in a {@link JsonObject} and checks it against a predicate.
     *
     * @param jsonObject The {@link JsonObject} in which to search for the value.
     * @param key The key for which the value needs to be found.
     * @param predicate The predicate that the value of the key must satisfy.
     * @return An {@link Optional} containing the {@link JsonElement} if it exists and matches the predicate,
     *         or an empty {@link Optional} if the value was not found or did not match.
     */
    public static Optional<JsonElement> findValidValue(JsonObject jsonObject, String key, Predicate<JsonElement> predicate) {
        if (!jsonObject.has(key)) {
            return Optional.empty();
        }

        JsonElement element = jsonObject.get(key);
        return predicate.test(element) ? Optional.of(element) : Optional.empty();
    }

    /**
     * Helper method to fetch a valid string value from a {@link JsonObject}
     * by key, ensuring it matches the specified predicate.
     *
     * @param obj The {@link JsonObject} from which to retrieve the value.
     * @param key The key for the value to be retrieved.
     * @param predicate The predicate that the value must satisfy.
     * @return The string value if found and valid, or {@code null} if not found
     *         or invalid.
     */
    public static String getValidString(JsonObject obj, String key, Predicate<JsonElement> predicate) {
        return findValidValue(obj, key, predicate)
                .map(JsonElement::getAsString)
                .orElse(null);
    }

    public static BasicModInfo createModInfoFromJsonObject(JsonObject jsonObject, String modIdKey,
                                                           String displayNameKey, String versionKey,
                                                           String descriptionKey) {
        Predicate<JsonElement> isStringPredicate = element ->
                element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();

        String modId = getValidString(jsonObject, modIdKey, isStringPredicate);
        String name = getValidString(jsonObject, displayNameKey, isStringPredicate);
        String description = getValidString(jsonObject, descriptionKey, isStringPredicate);
        String version = getValidString(jsonObject, versionKey, isStringPredicate);

        return new BasicModInfo(modId, name, version, description);
    }
}
