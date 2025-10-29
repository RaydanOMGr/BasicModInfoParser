/*
 * MIT License
 *
 * Copyright (c) 2024 RaydanOMGr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.andreasmelone.basicmodinfoparser.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.andreasmelone.basicmodinfoparser.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.dependency.StandardDependency;
import me.andreasmelone.basicmodinfoparser.dependency.fabric.FabricVersionRange;
import me.andreasmelone.basicmodinfoparser.dependency.forge.DependencySide;
import me.andreasmelone.basicmodinfoparser.dependency.forge.ForgeDependency;
import me.andreasmelone.basicmodinfoparser.dependency.forge.MavenVersionRange;
import me.andreasmelone.basicmodinfoparser.dependency.forge.Ordering;
import me.andreasmelone.basicmodinfoparser.dependency.version.*;
import org.jetbrains.annotations.NotNull;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ParserUtils {
    public static final Gson GSON = new Gson();

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
        for (String path : paths) {
            Path normalizedPath1 = Paths.get(path).normalize();
            Path normalizedPath2 = Paths.get(path2).normalize();
            if (normalizedPath1.equals(normalizedPath2)) return true;
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
        while ((readBytes = in.read(buffer)) > 0) {
            sb.append(new String(buffer, 0, readBytes));
        }
        return sb.toString();
    }

    /**
     * Finds a value by key in a {@link JsonObject} and checks it against a predicate.
     *
     * @param jsonObject The {@link JsonObject} in which to search for the value.
     * @param key        The key for which the value needs to be found.
     * @param predicate  The predicate that the value of the key must satisfy.
     * @return An {@link java.util.Optional} containing the {@link JsonElement} if it exists and matches the predicate,
     * or a {@link Optional#empty()} if the value was not found or did not match.
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
     * @param obj       The {@link JsonObject} from which to retrieve the value.
     * @param key       The key for the value to be retrieved.
     * @param predicate The predicate that the value must satisfy.
     * @return The string value if found and valid, or {@code null} if not found
     * or invalid.
     */
    public static String getValidString(JsonObject obj, String key, Predicate<JsonElement> predicate) {
        return findValidValue(obj, key, predicate)
                .map(JsonElement::getAsString)
                .orElse(null);
    }

    /**
     * Helper method to fetch a valid string value from a {@link JsonObject}
     * by key.
     *
     * @param obj       The {@link JsonObject} from which to retrieve the value.
     * @param key       The key for the value to be retrieved.
     * @return The string value if found and valid, or {@code null} if not found
     * or invalid.
     */
    public static String getValidString(JsonObject obj, String key) {
        return findValidValue(obj, key, (element) -> true)
                .map(JsonElement::getAsString)
                .orElse(null);
    }

    /**
     * Parses a dependency string into a {@link ForgeDependency} object.
     * <p>
     * This method parses a dependency string following the legacy Forge dependency format and returns
     * a {@link ForgeDependency} object. The dependency string may include an ordering prefix and a
     * version suffix, and these are parsed accordingly.
     * </p>
     *
     * @param dependencyString The string representation of the dependency. The format is usually
     *                         "ordering:modId@version".
     * @return The parsed {@link ForgeDependency} object wrapped inside a {@link Optional}, can be {@link Optional#empty()} if invalid. Returns an {@link Optional#empty()} if the modId
     *         is empty or invalid.
     */
    @NotNull
    public static Optional<Dependency> parseLegacyForgeDependency(String dependencyString) {
        String modId;
        String version = null;
        Ordering ordering = Ordering.NONE;

        String[] splitPrefix = dependencyString.split(":", 2);
        if (splitPrefix.length > 1) {
            ordering = Ordering.getFromString(splitPrefix[0]);
            dependencyString = splitPrefix[1];
        }

        String[] splitVersion = dependencyString.split("@");
        if (splitVersion.length > 1) {
            version = splitVersion[1];
            dependencyString = splitVersion[0];
        }

        modId = dependencyString;

        if (modId == null || modId.isEmpty()) {
            return Optional.empty();
        }

        Optional<MavenVersionRange> range = MavenVersionRange.parse(version);
        return Optional.of(new ForgeDependency(modId, range.isPresent() ? range.get() : UnknownVersionRange.parse(version), true, ordering, DependencySide.BOTH));
    }

    /**
     * Parses a {@link TomlTable} into a {@link ForgeDependency} object.
     * <p>
     * This method extracts the necessary fields from a TomlTable (a parsed TOML configuration) and
     * returns a corresponding {@link ForgeDependency} object.
     * </p>
     *
     * @param dependencyTable The TomlTable containing the dependency's data.
     * @return A {@link ForgeDependency} object constructed from the values in the given TomlTable.
     */
    @NotNull
    public static Dependency parseForgeDependency(TomlTable dependencyTable) {
        String depModId = dependencyTable.getString("modId");
        boolean mandatory = dependencyTable.getBoolean("mandatory", () -> true);
        String versionRange = dependencyTable.getString("versionRange");
        String ordering = dependencyTable.getString("ordering");
        String side = dependencyTable.getString("side");

        Optional<MavenVersionRange> range = MavenVersionRange.parse(versionRange);
        return new ForgeDependency(
                depModId, range.isPresent() ? range.get() : UnknownVersionRange.parse(versionRange), mandatory,
                Ordering.getFromString(ordering), DependencySide.getFromString(side)
        );
    }


    /**
     * Parses Fabric dependencies from a {@link JsonObject} and adds them to a given list of dependencies.
     * <p>
     * This method processes Fabric dependency entries within the given JSON object and adds them to the
     * provided list. The dependencies may be either a single string or an array of strings.
     * </p>
     *
     * @param dependencyList The list where parsed dependencies will be added.
     * @param jsonObject     The {@link JsonObject} containing the dependency data.
     * @param key            The key within the JSON object to retrieve the dependencies.
     * @param mandatory      Whether the dependency is mandatory or optional.
     */
    public static void parseFabricDependencies(List<Dependency> dependencyList, JsonObject jsonObject, String key, boolean mandatory) {
        if (jsonObject.has(key) && jsonObject.get(key).isJsonObject()) {
            JsonObject depends = jsonObject.getAsJsonObject(key);

            depends.entrySet().forEach(entry -> {
                String dependencyKey = entry.getKey();
                JsonElement dependency = entry.getValue();

                if (dependency.isJsonPrimitive() && dependency.getAsJsonPrimitive().isString()) {
                    Optional<FabricVersionRange> range = FabricVersionRange.parse(dependency.getAsString());
                    if(!range.isPresent()) return;
                    dependencyList.add(new StandardDependency(dependencyKey, mandatory, range.get()));
                } else if (dependency.isJsonArray()) {
                    String[] resultingVersion = StreamSupport.stream(dependency.getAsJsonArray().spliterator(), false)
                            .filter((el) -> el.isJsonPrimitive() && el.getAsJsonPrimitive().isString())
                            .map(JsonElement::getAsString)
                            .toArray(String[]::new);
                    Optional<FabricVersionRange> range = FabricVersionRange.parse(resultingVersion);
                    if(!range.isPresent()) return;
                    dependencyList.add(new StandardDependency(dependencyKey, mandatory, range.get()));
                }
            });
        }
    }

    /**
     * Creates a {@link BasicModInfo} object from a {@link JsonObject}.
     * <p>
     * This method parses a JSON object, extracts the required fields (modId, displayName, version,
     * and description), and creates a new {@link BasicModInfo} object along with any given dependencies.
     * </p>
     *
     * @param jsonObject      The {@link JsonObject} containing the mod information.
     * @param modIdKey        The key used to retrieve the mod ID from the JSON object.
     * @param displayNameKey  The key used to retrieve the mod display name from the JSON object.
     * @param versionKey      The key used to retrieve the mod version from the JSON object.
     * @param descriptionKey  The key used to retrieve the mod description from the JSON object.
     * @param dependencies    The list of {@link Dependency} objects that should be associated with the mod.
     * @return A {@link BasicModInfo} object containing the mod information and its dependencies.
     */
    public static BasicModInfo createModInfoFromJsonObject(JsonObject jsonObject, String modIdKey,
                                                           String displayNameKey, String versionKey,
                                                           String descriptionKey, Dependency[] dependencies) {
        Predicate<JsonElement> isStringPredicate = element ->
                element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();

        String modId = getValidString(jsonObject, modIdKey, isStringPredicate);
        String name = getValidString(jsonObject, displayNameKey, isStringPredicate);
        String description = getValidString(jsonObject, descriptionKey, isStringPredicate);
        String version = getValidString(jsonObject, versionKey, isStringPredicate);

        Optional<LooseSemanticVersion> mavenVersion = LooseSemanticVersion.parse(version);
        return new BasicModInfo(modId, name, mavenVersion.isPresent() ? mavenVersion.get() : UnknownVersion.parse(version), description, dependencies);
    }
}
