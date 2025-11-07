/*
 * MIT License
 *
 * Copyright (c) 2024-2025 RaydanOMGr
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
package me.andreasmelone.basicmodinfoparser.jarinjar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.andreasmelone.abstractzip.IZipEntry;
import me.andreasmelone.abstractzip.IZipFile;
import me.andreasmelone.abstractzip.IZipFileFactory;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;

import static me.andreasmelone.basicmodinfoparser.util.ParserUtils.GSON;
import static me.andreasmelone.basicmodinfoparser.util.ParserUtils.readEverythingAsString;

public enum JarInJarPlatform {
    FORGEGRADLE("META-INF/jarjar/metadata.json") {
        @Override
        protected @NotNull List<String> parseJarsInJar(String metadata) {
            JsonElement root = GSON.fromJson(metadata, JsonElement.class);
            if (root == null || !root.isJsonObject()) {
                return new ArrayList<>();
            }

            JsonElement jarsObject = root.getAsJsonObject().get("jars");
            if (jarsObject == null || !jarsObject.isJsonArray()) {
                return new ArrayList<>();
            }

            List<String> jars = new ArrayList<>();
            for (JsonElement el : jarsObject.getAsJsonArray()) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                JsonElement path = obj.get("path");
                if (!path.isJsonPrimitive() || !path.getAsJsonPrimitive().isString()) continue;
                jars.add(path.getAsString());
            }
            return jars;
        }
    },
    FABRIC_LOOM("fabric.mod.json") {
        @Override
        protected @NotNull List<String> parseJarsInJar(String metadata) {
            JsonElement root = GSON.fromJson(metadata, JsonElement.class);
            if (root == null || (!root.isJsonArray() && !root.isJsonObject())) {
                return new ArrayList<>();
            }
            JsonArray jsonArray = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
            if (root.isJsonObject()) {
                jsonArray.add(root.getAsJsonObject());
            }

            List<String> jars = new ArrayList<>();
            for (JsonElement jsonArrayElement : jsonArray) {
                if (!jsonArrayElement.isJsonObject()) continue;
                JsonObject jsonObject = jsonArrayElement.getAsJsonObject();

                JsonElement jarsArray = jsonObject.get("jars");
                if (jarsArray == null || !jarsArray.isJsonArray()) continue;
                for (JsonElement arrayElement : jarsArray.getAsJsonArray()) {
                    if (!arrayElement.isJsonObject()) continue;
                    JsonObject jarObject = arrayElement.getAsJsonObject();

                    JsonElement path = jarObject.get("file");
                    if (path == null || !path.isJsonPrimitive() || !path.getAsJsonPrimitive().isString()) continue;
                    jars.add(path.getAsString());
                }
            }
            return jars;
        }
    },
    QUILT("quilt.mod.json") {
        @Override
        protected @NotNull List<String> parseJarsInJar(String metadata) {
            JsonObject jsonObj = GSON.fromJson(metadata, JsonObject.class);
            if (jsonObj == null) return new ArrayList<>();

            if (!jsonObj.has("quilt_loader") || !jsonObj.get("quilt_loader").isJsonObject()) return new ArrayList<>();
            JsonObject quiltLoader = jsonObj.getAsJsonObject("quilt_loader");

            List<String> jars = new ArrayList<>();
            JsonElement jarsArray = quiltLoader.get("jars");
            if (jarsArray == null || !jarsArray.isJsonArray()) return new ArrayList<>();
            for (JsonElement str : jarsArray.getAsJsonArray()) {
                if (!str.isJsonPrimitive() || !str.getAsJsonPrimitive().isString()) continue;

                jars.add(str.getAsString());
            }

            return jars;
        }
    };

    private final String[] metadataFiles;

    private JarInJarPlatform(String... metadataFiles) {
        this.metadataFiles = metadataFiles;
    }

    @NotNull
    public List<String> getJarsInJar(String metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }

        try {
            return parseJarsInJar(metadata);
        } catch (Exception e) {
            throw new ModInfoParseException("Error parsing the JIJ metadata info from the given string.", e);
        }
    }

    /**
     * Reads and returns the content of the platform-specific metadata file (e.g. {@code metadata.json}, {@code fabric.mod.json}, etc.) from a zip archive.
     *
     * @param zip The {@link IZipFile} to search for the platform-specific info file. It is allowed to be a derivative of such, e.g. {@link JarFile}
     * @return The content of the first matching info file as a string, or {@code null} if none are found.
     * This method reads the ZIP file once and stops searching after the first match.
     * @throws IOException If an error occurs while reading the zip file or its entries.
     */
    @NotNull
    public Optional<String> getMetadataFileContent(IZipFile zip) throws IOException {
        for (String infoFilePath : this.metadataFiles) {
            IZipEntry infoFileEntry = zip.findEntry(infoFilePath);
            if (infoFileEntry == null) continue;
            try (InputStream entry = zip.openEntry(infoFileEntry)) {
                return Optional.of(readEverythingAsString(entry));
            }
        }
        return Optional.empty();
    }

    /**
     * Internal method that parses the metadata file that stores information on jar in jars
     *
     * @param metadata the metadata file content
     * @return the parsed jars
     */
    @NotNull
    protected abstract List<String> parseJarsInJar(String metadata);

    /**
     * Finds the mod JIJ-Platforms by inspecting the files inside the provided mod archive.
     *
     * @param file The mod file, which must be a zip archive (e.g., .jar, .zip) or a similar format.
     * @return An array of {@link JarInJarPlatform} values representing the platforms found in the file.
     * @throws IOException If the file is not a valid zip archive or an I/O error occurs while reading the file.
     */
    @NotNull
    public static JarInJarPlatform[] findJarInJarPlatforms(File file) throws IOException {
        JarInJarPlatform[] platforms;
        try (IZipFile zip = IZipFileFactory.Provider.create(file)) {
            platforms = findJarInJarPlatforms(zip);
        }
        return platforms;
    }

    public static JarInJarPlatform[] findJarInJarPlatforms(IZipFile zipFile) {
        List<JarInJarPlatform> platforms = new ArrayList<>();
        for (JarInJarPlatform platform : JarInJarPlatform.values()) {
            for (String infoFilePath : platform.metadataFiles) {
                IZipEntry infoFileEntry = zipFile.findEntry(infoFilePath);
                if (infoFileEntry == null) continue;
                platforms.add(platform);
                break;
            }
        }
        return platforms.toArray(new JarInJarPlatform[0]);
    }
}
