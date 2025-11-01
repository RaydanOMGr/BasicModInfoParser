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
package me.andreasmelone.basicmodinfoparser.platform;

import com.google.gson.*;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.ProvidedMod;
import me.andreasmelone.basicmodinfoparser.platform.dependency.StandardDependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.FabricVersionRange;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.LooseSemanticVersion;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.MavenVersion;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.FabricModInfo;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.StandardBasicModInfo;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import me.andreasmelone.basicmodinfoparser.util.ParserUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomlj.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
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
                return StandardBasicModInfo.emptyArray();
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
                parsedInfos.add(ParserUtils.createForgeModInfoFromJsonObject(modObject,
                        "modid", "name", "version", "description", "logoFile",
                        dependencyList, this
                ));
            }

            return parsedInfos.toArray(new BasicModInfo[0]);
        }

        @Override
        protected @Nullable BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            return null;
        }
    },
    /**
     * Forge platform, which uses the {@code mods.toml} file with TOML data.
     */
    FORGE("META-INF/mods.toml") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            return ParserUtils.parseForgelikeInfo(fileData, this);
        }

        @Override
        protected BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<MavenVersion> version = MavenVersion.parse(loaderVersion);
            return new StandardBasicModInfo(
                    "forge",
                    "Forge",
                    version.orElse(null),
                    "Modifications to the Minecraft base files to assist in compatibility between mods.",
                    new ArrayList<>(),
                    null,
                    this
            );
        }
    },
    /**
     * NeoForge platform, which uses the {@code neoforge.mods.toml} file with TOML data, similarly to {@link Platform#FORGE}.
     */
    NEOFORGE("META-INF/neoforge.mods.toml") {
        @Override
        protected @NotNull BasicModInfo[] parseFileData(String fileData) {
            return ParserUtils.parseForgelikeInfo(fileData, this);
        }

        @Override
        protected @NotNull BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<MavenVersion> version = MavenVersion.parse(loaderVersion);
            return new StandardBasicModInfo(
                    "neoforge",
                    "NeoForge",
                    version.orElse(null),
                    "NeoForge is a free, open-source, community-oriented modding API for Minecraft.",
                    new ArrayList<>(),
                    null,
                    this
            );
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
                return StandardBasicModInfo.emptyArray();
            }
            JsonArray jsonArray = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
            if (root.isJsonObject()) {
                jsonArray.add(root.getAsJsonObject());
            }

            List<BasicModInfo> parsedInfos = new ArrayList<>();
            for (JsonElement jsonArrayElement : jsonArray) {
                if(!jsonArrayElement.isJsonObject()) continue;
                JsonObject jsonObject = jsonArrayElement.getAsJsonObject();

                Optional<LooseSemanticVersion> version = LooseSemanticVersion.parse(getValidString(jsonObject, "version"));

                List<Dependency> dependencyList = new ArrayList<>();
                ParserUtils.parseFabricDependencies(dependencyList, jsonObject, "depends", true);
                ParserUtils.parseFabricDependencies(dependencyList, jsonObject, "recommends", false);
                List<Dependency> breaksList = new ArrayList<>();
                ParserUtils.parseFabricDependencies(breaksList, jsonObject, "breaks", true);
                List<ProvidedMod<LooseSemanticVersion>> provided = new ArrayList<>();
                if(jsonObject.has("provides") && jsonObject.get("provides").isJsonArray()) {
                    for (JsonElement dependency : jsonObject.getAsJsonArray("provides")) {
                        if (!dependency.isJsonPrimitive() || !dependency.getAsJsonPrimitive().isString()) continue;
                        provided.add(new ProvidedMod<>(dependency.getAsString(), version.orElse(null)));
                    }
                }

                parsedInfos.add(ParserUtils.createFabricModInfoFromJsonObject(jsonObject,
                        "id", "name", version.orElse(null), "description", "icon",
                        dependencyList, breaksList, provided, this));
            }

            return parsedInfos.toArray(new BasicModInfo[0]);
        }

        @Override
        protected @NotNull BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<LooseSemanticVersion> version = LooseSemanticVersion.parse(loaderVersion);
            return new StandardBasicModInfo(
                    "fabricloader",
                    "Fabric Loader",
                    version.orElse(null),
                    "A flexible platform-independent mod loader designed for Minecraft and other games and applications.",
                    new ArrayList<>(),
                    null,
                    this
            );
        }
    },
    /**
     * Quilt platform, which uses the {@code quilt.mod.json} file. As the extensions suggests, it stores data in the JSON format.
     */
    QUILT("quilt.mod.json") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            JsonObject jsonObj = GSON.fromJson(fileData, JsonObject.class);
            if(jsonObj == null) return StandardBasicModInfo.emptyArray();

            if(!jsonObj.has("quilt_loader") || !jsonObj.get("quilt_loader").isJsonObject()) return StandardBasicModInfo.emptyArray();
            JsonObject quiltLoader = jsonObj.getAsJsonObject("quilt_loader");
            String modId = getValidString(quiltLoader, "id");
            String version = getValidString(quiltLoader, "version");

            String name = null;
            String description = null;
            String iconPath = null;
            if(quiltLoader.has("metadata") && quiltLoader.get("metadata").isJsonObject()) {
                JsonObject metadata = quiltLoader.getAsJsonObject("metadata");
                name = getValidString(metadata, "name");
                description = getValidString(metadata, "description");
                iconPath = getValidString(metadata, "icon");
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
                    dependencies.add(new StandardDependency<>(dependencyId, isMandatory, fabricVersionRange.orElse(null)));
                }
            }

            List<Dependency> breaks = new ArrayList<>();
            if(quiltLoader.has("breaks") && quiltLoader.get("breaks").isJsonArray()) {
                for (JsonElement dependency : quiltLoader.getAsJsonArray("breaks")) {
                    if(!dependency.isJsonObject()) continue;
                    JsonObject dependencyObject = dependency.getAsJsonObject();
                    String dependencyId = getValidString(dependencyObject, "id");
                    boolean isMandatory = true;
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
                    breaks.add(new StandardDependency<>(dependencyId, isMandatory, fabricVersionRange.orElse(null)));
                }
            }

            List<ProvidedMod<LooseSemanticVersion>> provides = new ArrayList<>();
            if(quiltLoader.has("provides") && quiltLoader.get("provides").isJsonArray()) {
                for (JsonElement dependency : quiltLoader.getAsJsonArray("provides")) {
                    if(!dependency.isJsonObject()) continue;
                    JsonObject dependencyObject = dependency.getAsJsonObject();
                    String dependencyId = getValidString(dependencyObject, "id");
                    String providedVersion = version;

                    if(dependencyObject.has("versions")) {
                        JsonElement dependencyVersion = dependencyObject.get("versions");
                        if (dependencyVersion.isJsonPrimitive() && dependencyVersion.getAsJsonPrimitive().isString()) {
                            providedVersion = dependencyVersion.getAsString();
                        }
                    }

                    Optional<LooseSemanticVersion> semVer = LooseSemanticVersion.parse(providedVersion);
                    provides.add(new ProvidedMod<>(dependencyId, semVer.orElse(null)));
                }
            }

            Optional<LooseSemanticVersion> semanticVersion = LooseSemanticVersion.parse(version, false);
            return new BasicModInfo[] {
                    new FabricModInfo(modId, name,
                            semanticVersion.orElse(null),
                            description, dependencies, iconPath, this,
                            breaks, provides
                    )
            };
        }

        @Override
        protected @NotNull BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<LooseSemanticVersion> version = LooseSemanticVersion.parse(loaderVersion);
            return new StandardBasicModInfo(
                    "quilt_loader",
                    "Quilt Loader",
                    version.orElse(null),
                    "The loader for mods under Quilt. It provides mod loading facilities and useful abstractions for other mods to use.",
                    new ArrayList<>(),
                    null,
                    this
            );
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
        } catch (Exception e) {
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
     * Creates a {@link BasicModInfo} for the current loader, which is useful for dependency checking.
     * It may return {@link Optional#empty()} if the loader does not usually require to be defined as a dependency, like in case of {@link Platform#FORGE_LEGACY}
     *
     * @param version the version of the given loader
     * @return the created object wrapped in an optional
     */
    public Optional<BasicModInfo> createLoaderInfo(String version) {
        return Optional.ofNullable(createNullableLoaderInfo(version));
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
     * Internal method for creating a {@link BasicModInfo} object with loader info.
     * @param loaderVersion the version of the given loader
     * @return the created object
     */
    @Nullable
    protected abstract BasicModInfo createNullableLoaderInfo(String loaderVersion);

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
