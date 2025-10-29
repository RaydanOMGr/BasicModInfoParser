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
package me.andreasmelone.basicmodinfoparser;

import com.google.gson.*;
import me.andreasmelone.basicmodinfoparser.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.dependency.StandardDependency;
import me.andreasmelone.basicmodinfoparser.dependency.fabric.FabricVersionRange;
import me.andreasmelone.basicmodinfoparser.dependency.version.*;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import me.andreasmelone.basicmodinfoparser.util.ParserUtils;
import org.jetbrains.annotations.NotNull;
import org.tomlj.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static me.andreasmelone.basicmodinfoparser.util.ParserUtils.*;

public enum Platform {
    /**
     * Legacy Forge platform, which uses the {@code mcmod.info} file containing JSON data.
     */
    FORGE_LEGACY("mcmod.info") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            JsonArray topArray = GSON.fromJson(fileData, JsonArray.class);
            if (topArray == null || topArray.size() == 0) {
                return BasicModInfo.emptyArray();
            }

            List<BasicModInfo> parsedInfos = new ArrayList<>();
            for (JsonElement topArrayElement : topArray) {
                if(!topArrayElement.isJsonObject()) continue;
                JsonObject modObject = topArrayElement.getAsJsonObject();
                List<Dependency> dependencyList = new ArrayList<>();
                if (modObject.has("dependencies") && modObject.get("dependencies").isJsonArray()) {
                    JsonArray dependenciesArray = modObject.getAsJsonArray("dependencies");
                    for (JsonElement element : dependenciesArray) {
                        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                            continue;
                        }
                        parseLegacyForgeDependency(element.getAsString()).ifPresent(dependencyList::add);
                    }
                }
                parsedInfos.add(ParserUtils.createModInfoFromJsonObject(modObject,
                        "modid", "name", "version", "description",
                        dependencyList.toArray(new Dependency[0])
                ));
            }

            return parsedInfos.toArray(new BasicModInfo[0]);
        }
    },
    /**
     * Forge platform, which uses the {@code mods.toml} file with TOML data.
     * It can also be used to parse NeoForge mods.
     */
    FORGE("META-INF/mods.toml", "META-INF/neoforge.mods.toml") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            TomlParseResult result = Toml.parse(fileData);
            TomlArray modsArray = result.getArray("mods");
            if(modsArray == null || modsArray.isEmpty()) return BasicModInfo.emptyArray();

            List<BasicModInfo> parsedInfos = new ArrayList<>();
            for (int index = 0; index < modsArray.size(); index++) {
                TomlTable modInfo = modsArray.getTable(index);
                if(modInfo.isEmpty()) continue;

                String modId = modInfo.getString("modId");
                String name = modInfo.getString("displayName");
                String description = modInfo.getString("description");
                String version = modInfo.getString("version");

                List<Dependency> dependencies = new ArrayList<>();
                TomlArray dependenciesArray = result.getArray("dependencies." + modId);

                if (dependenciesArray != null && !dependenciesArray.isEmpty()) {
                    for(int i = 0; i < dependenciesArray.size(); i++) {
                        TomlTable dependencyTable = dependenciesArray.getTable(i);
                        if (dependencyTable != null && !dependencyTable.isEmpty()) {
                            dependencies.add(ParserUtils.parseForgeDependency(dependencyTable));
                        }
                    }
                }

                Optional<MavenVersion> mavenVersion = MavenVersion.parse(version);
                parsedInfos.add(new BasicModInfo(
                        modId, name, mavenVersion.isPresent() ? mavenVersion.get() : UnknownVersion.parse(version), description,
                        dependencies.toArray(new Dependency[0])
                ));
            }
            return parsedInfos.toArray(new BasicModInfo[0]);
        }
    },
    /**
     * Fabric platform, which uses the {@code fabric.mod.json} file. As the extension suggests, it stores data in JSON format.
     */
    FABRIC("fabric.mod.json") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            JsonElement root = GSON.fromJson(fileData, JsonElement.class);
            if (root == null || (!root.isJsonArray() && !root.isJsonObject())) {
                return BasicModInfo.emptyArray();
            }
            JsonArray jsonArray = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
            if (root.isJsonObject()) {
                jsonArray.add(root.getAsJsonObject());
            }

            List<BasicModInfo> parsedInfos = new ArrayList<>();
            for (JsonElement jsonArrayElement : jsonArray) {
                if(!jsonArrayElement.isJsonObject()) continue;
                JsonObject jsonObject = jsonArrayElement.getAsJsonObject();

                List<Dependency> dependencyList = new ArrayList<>();
                ParserUtils.parseFabricDependencies(dependencyList, jsonObject, "depends", true);

                parsedInfos.add(ParserUtils.createModInfoFromJsonObject(jsonObject,
                        "id", "name", "version", "description",
                        dependencyList.toArray(new Dependency[0])));
            }

            return parsedInfos.toArray(new BasicModInfo[0]);
        }
    },
    /**
     * Quilt platform, which uses the {@code quilt.mod.json} file. As the extensions suggests, it stores data in the JSON format.
     */
    QUILT("quilt.mod.json") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            JsonObject jsonObj = GSON.fromJson(fileData, JsonObject.class);
            if(jsonObj == null) return BasicModInfo.emptyArray();

            if(!jsonObj.has("quilt_loader") || !jsonObj.get("quilt_loader").isJsonObject()) return BasicModInfo.emptyArray();
            JsonObject quiltLoader = jsonObj.getAsJsonObject("quilt_loader");
            String modId = getValidString(quiltLoader, "id");
            String version = getValidString(quiltLoader, "version");

            String name = null;
            String description = null;
            if(quiltLoader.has("metadata") && quiltLoader.get("metadata").isJsonObject()) {
                JsonObject metadata = quiltLoader.getAsJsonObject("metadata");
                name = getValidString(metadata, "name");
                description = getValidString(metadata, "description");
            }

            List<Dependency> dependencies = new ArrayList<>();
            if(quiltLoader.has("depends") && quiltLoader.get("depends").isJsonArray()) {
                for (JsonElement dependency : quiltLoader.getAsJsonArray("depends")) {
                    if(!dependency.isJsonObject()) continue;
                    JsonObject dependencyObject = dependency.getAsJsonObject();
                    String dependencyId = getValidString(dependencyObject, "id");
                    boolean isMandatory = true;
                    if(dependencyObject.has("optional")
                            && dependencyObject.get("optional").isJsonPrimitive()
                            && dependencyObject.get("optional").getAsJsonPrimitive().isString()) {
                        isMandatory = !dependencyObject.get("optional").getAsBoolean();
                    }
                    String[] versions = new String[] { "*" };

                    if(dependencyObject.has("versions")) {
                        JsonElement dependencyVersion = dependencyObject.get("versions");
                        if (dependencyVersion.isJsonPrimitive() && dependencyVersion.getAsJsonPrimitive().isString()) {
                            versions[0] = dependencyVersion.getAsString();
                        } else if (dependencyVersion.isJsonArray()) {
                            versions = StreamSupport.stream(dependencyVersion.getAsJsonArray().spliterator(), false)
                                    .filter((el) -> el.isJsonPrimitive() && el.getAsJsonPrimitive().isString())
                                    .map(JsonElement::getAsString)
                                    .toArray(String[]::new);
                        }
                    }

                    Optional<FabricVersionRange> fabricVersionRange = FabricVersionRange.parse(versions);
                    dependencies.add(new StandardDependency(dependencyId, isMandatory, fabricVersionRange.isPresent() ?
                            fabricVersionRange.get() : UnknownVersionRange.parse(version)));
                }
            }

            Optional<LooseSemanticVersion> semanticVersion = LooseSemanticVersion.parse(version, false);
            return new BasicModInfo[] {
                    new BasicModInfo(modId, name,
                            semanticVersion.isPresent() ? semanticVersion.get() : UnknownVersion.parse(version),
                            description, dependencies.toArray(new Dependency[0])
                    )
            };
        }
    };

    private final String[] infoFilePaths;

    private Platform(String... infoFilePaths) {
        this.infoFilePaths = infoFilePaths;
    }

    /**
     * Parses a string into a {@link BasicModInfo}.
     *
     * @param toParse The string to parse, expected to be in the format of the specific platform (JSON, TOML, etc.).
     *                This string must match the format required by the platform; otherwise, parsing will fail.
     * @return An array of {@link BasicModInfo} objects containing mod information. Each object represents one specified mod namespace in the modinfo file.
     * @throws IllegalArgumentException If the input string is null.
     * @throws ModInfoParseException If an error occurs while parsing the mod info file due to invalid formatting, missing fields, or an unexpected data structure.
     */
    @NotNull
    public BasicModInfo[] parse(String toParse) {
        if (toParse == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }

        try {
            return parseFileData(toParse);
        } catch (TomlParseError | TomlInvalidTypeException | JsonParseException e) {
            throw new ModInfoParseException("Error parsing the mod info from the given string.", e);
        }
    }


    /**
     * Reads and returns the content of the platform-specific info file (e.g. {@code mcmod.info}, {@code mods.toml}, etc.) from a zip archive.
     *
     * @param zip The {@link ZipFile} to search for the platform-specific info file. It is allowed to be a derivative of such, e.g. {@link JarFile}
     * @return The content of the first matching info file as a string, or {@code null} if none are found.
     *         This method reads the ZIP file once and stops searching after the first match.
     * @throws IOException If an error occurs while reading the zip file or its entries.
     */
    @NotNull
    public Optional<String> getInfoFileContent(ZipFile zip) throws IOException {
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip.getName()))) {
            ZipEntry next;
            while ((next = in.getNextEntry()) != null) {
                if (next.isDirectory()) continue;
                if (comparePaths(this.infoFilePaths, next.getName())) {
                    try (InputStream entry = zip.getInputStream(next)) {
                        return Optional.of(readEverythingAsString(entry));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Internal method for parsing file data that should be implemented. Does not include safety checks like {@link Platform#parse(String)}
     *
     * @param fileData The data that was written into the mod info file
     * @return The parsed data
     */
    @NotNull
    protected abstract BasicModInfo[] parseFileData(String fileData);

    /**
     * Finds the mod platform(s) (e.g. Forge, Fabric) by inspecting the files inside the provided mod archive.
     * A mod can support multiple platforms, so this method can return multiple platforms.
     *
     * @param file The mod file, which must be a zip archive (e.g., .jar, .zip) or a similar format.
     * @return An array of {@link Platform} values representing the platforms found in the file.
     *         If a mod supports multiple platforms (e.g., Forge and Fabric), both will be included.
     * @throws IOException If the file is not a valid zip archive or an I/O error occurs while reading the file.
     */
    @NotNull
    public static Platform[] findModPlatform(File file) throws IOException {
        List<Platform> platforms = new ArrayList<>();

        try (ZipInputStream in = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry next;
            while ((next = in.getNextEntry()) != null) {
                if (next.isDirectory()) continue;
                for (Platform platform : Platform.values()) {
                    if (comparePaths(platform.infoFilePaths, next.getName()))
                        platforms.add(platform);
                }
            }
        }

        return platforms.toArray(new Platform[0]);
    }
}
