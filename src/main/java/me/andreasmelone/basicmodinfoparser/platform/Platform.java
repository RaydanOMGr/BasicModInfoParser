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
package me.andreasmelone.basicmodinfoparser.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.andreasmelone.abstractzip.IZipEntry;
import me.andreasmelone.abstractzip.IZipFile;
import me.andreasmelone.abstractzip.IZipFileFactory;
import me.andreasmelone.basicmodinfoparser.platform.dependency.ProvidedMod;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.LooseSemanticVersion;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.MavenVersion;
import me.andreasmelone.basicmodinfoparser.platform.dependency.parser.FabricDependencyParser;
import me.andreasmelone.basicmodinfoparser.platform.dependency.parser.ForgeDependencyParser;
import me.andreasmelone.basicmodinfoparser.platform.dependency.parser.LegacyForgeDependencyParser;
import me.andreasmelone.basicmodinfoparser.platform.dependency.parser.QuiltDependencyParser;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.FabricModInfo;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.StandardBasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.model.ModInfoKeys;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import me.andreasmelone.basicmodinfoparser.util.ParserUtils;
import me.andreasmelone.basicmodinfoparser.util.adapter.JsonAdapter;
import me.andreasmelone.basicmodinfoparser.util.adapter.TomlAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static me.andreasmelone.basicmodinfoparser.platform.modinfo.model.ModInfoKeys.forgeKeys;
import static me.andreasmelone.basicmodinfoparser.util.ParserUtils.GSON;
import static me.andreasmelone.basicmodinfoparser.util.ParserUtils.readEverythingAsString;

public enum Platform {
    /**
     * Legacy Forge platform, which uses the {@code mcmod.info} file containing JSON data.
     */
    FORGE_LEGACY(
            new ModInfoKeys(
                    "modid",
                    "name",
                    "version",
                    "description",
                    "logoFile",
                    "authorList",
                    new String[]{"dependencies"}
            ),
            "mcmod.info"
    ) {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            // In Legacy Forge, you can specify multiple mods per file (the root element is an array)
            JsonArray topArray = GSON.fromJson(fileData, JsonArray.class);

            if (topArray == null || topArray.size() == 0) {
                return StandardBasicModInfo.emptyArray();
            }

            // Since the mod list is not empty, we iterate over all mods and parse each one
            List<BasicModInfo> parsedInfos = new ArrayList<>();
            for (JsonElement topArrayElement : topArray) {
                if (!topArrayElement.isJsonObject()) continue;

                JsonObject modObject = topArrayElement.getAsJsonObject();
                parsedInfos.add(ParserUtils.createModInfoFrom(
                        new JsonAdapter(modObject),
                        this,
                        new LegacyForgeDependencyParser(),
                        new MavenVersion()
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
    FORGE(forgeKeys(), "META-INF/mods.toml") {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            final TomlParseResult parseResult = Toml.parse(fileData);

            final TomlArray mods = parseResult.getArray("mods");
            if (mods == null) return StandardBasicModInfo.emptyArray();

            final List<BasicModInfo> modInfos = new ArrayList<>();
            // I transferred Forge-specific logic to the Platform Enumeration
            // (the capacity to provide multiple mods per file)
            for (int i = 0; i < mods.size(); i++) {
                final TomlTable modInfo = mods.getTable(i);
                if (modInfo.isEmpty()) continue;

                final BasicModInfo info = ParserUtils.createModInfoFrom(
                        new TomlAdapter(modInfo),
                        this,
                        new ForgeDependencyParser(),
                        new MavenVersion()
                );

                modInfos.add(info);
            }

            return modInfos.toArray(new BasicModInfo[0]);
        }

        @Override
        protected BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<Version> version = new MavenVersion().parse(loaderVersion);
            return new StandardBasicModInfo(
                    "forge",
                    "Forge",
                    version.orElse(null),
                    "Modifications to the Minecraft base files to assist in compatibility between mods.",
                    new ArrayList<>(),
                    null,
                    this,
                    new ArrayList<>()
            );
        }
    },
    /**
     * NeoForge platform, which uses the {@code neoforge.mods.toml} file with TOML data, similarly to {@link Platform#FORGE}.
     */
    NEOFORGE(forgeKeys(), "META-INF/neoforge.mods.toml") {
        @Override
        protected @NotNull BasicModInfo[] parseFileData(String fileData) {
            final TomlTable modsTable = Toml.parse(fileData).getTable("mods");
            final BasicModInfo info = ParserUtils.createModInfoFrom(
                    new TomlAdapter(modsTable),
                    this,
                    new ForgeDependencyParser(),
                    new MavenVersion()
            );

            return new BasicModInfo[]{info};
        }

        @Override
        protected @NotNull BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<Version> version = new MavenVersion().parse(loaderVersion);
            return new StandardBasicModInfo(
                    "neoforge",
                    "NeoForge",
                    version.orElse(null),
                    "NeoForge is a free, open-source, community-oriented modding API for Minecraft.",
                    new ArrayList<>(),
                    null,
                    this,
                    new ArrayList<>()
            );
        }
    },

    /**
     * Fabric platform, which uses the {@code fabric.mod.json} file. As the extension suggests, it stores data in JSON format.
     */
    FABRIC(
            new ModInfoKeys(
                    "id",
                    "name",
                    "version",
                    "description",
                    "icon",
                    "authors",
                    new String[]{
                            "depends",
                            "recommends",
                            "suggests",
                            "breaks",
                            "conflicts"
                    }
            ),
            "fabric.mod.json"
    ) {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            JsonElement root = GSON.fromJson(fileData, JsonElement.class);
            if (root == null || (!root.isJsonArray() && !root.isJsonObject())) {
                return StandardBasicModInfo.emptyArray();
            }

            // Fabric 0.4.0 or older allowed the root object to be an array
            // (https://wiki.fabricmc.net/documentation:fabric_mod_json_spec#fabricmodjson_specification)
            JsonArray jsonArray = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
            // Treat everything as a Json Array so we can iterate over it (Even if there's only one element)
            if (root.isJsonObject()) {
                jsonArray.add(root.getAsJsonObject());
            }

            // Now that we have the mod list, we iterate over all mods and parse each one
            List<BasicModInfo> parsedInfos = new ArrayList<>();
            for (JsonElement jsonArrayElement : jsonArray) {
                if (!jsonArrayElement.isJsonObject()) continue;
                final JsonObject modObject = jsonArrayElement.getAsJsonObject();

                final BasicModInfo current = ParserUtils.createModInfoFrom(
                        new JsonAdapter(modObject),
                        this,
                        new FabricDependencyParser(),
                        new LooseSemanticVersion()
                );

                List<ProvidedMod> providedMods = new ArrayList<>();
                if (modObject.has("provides") && modObject.get("provides").isJsonArray()) {
                    for (JsonElement dependency : modObject.getAsJsonArray("provides")) {
                        if (!dependency.isJsonPrimitive() || !dependency.getAsJsonPrimitive().isString()) continue;
                        providedMods.add(new ProvidedMod(dependency.getAsString(), current.getVersion()));
                    }
                }

                // Create a FabricModInfo with the above parsed information
                // and add it to the list of parsed infos
                parsedInfos.add(
                        new FabricModInfo(
                                current.getId(),
                                current.getName(),
                                current.getVersion(),
                                current.getDescription(),
                                current.getDependencies(),
                                current.getIconPath(),
                                current.getPlatform(),
                                current.getAuthors(),
                                providedMods
                        )
                );
            }

            return parsedInfos.toArray(new BasicModInfo[0]);
        }

        @Override
        protected @NotNull BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<Version> version = new LooseSemanticVersion().parse(loaderVersion);
            return new StandardBasicModInfo(
                    "fabricloader",
                    "Fabric Loader",
                    version.orElse(null),
                    "A flexible platform-independent mod loader designed for Minecraft and other games and applications.",
                    new ArrayList<>(),
                    null,
                    this,
                    new ArrayList<>()
            );
        }
    },

    /**
     * Quilt platform, which uses the {@code quilt.mod.json} file. As the extensions suggests, it stores data in the JSON format.
     */
    QUILT(
            new ModInfoKeys(
                    "id",
                    "metadata.name",
                    "version",
                    "metadata.description",
                    "metadata.icon",
                    "metadata.contributors",
                    new String[]{
                            "depends", "breaks"
                    }
            ),
            "quilt.mod.json"
    ) {
        @Override
        protected BasicModInfo[] parseFileData(String fileData) {
            JsonObject jsonObj = GSON.fromJson(fileData, JsonObject.class);
            if (jsonObj == null) return StandardBasicModInfo.emptyArray();

            if (!jsonObj.has("quilt_loader") || !jsonObj.get("quilt_loader").isJsonObject())
                return StandardBasicModInfo.emptyArray();

            // Get the main quilt loader object
            JsonAdapter quiltLoader = new JsonAdapter(
                    jsonObj.getAsJsonObject("quilt_loader")
            );

            // NOTE: The "provides" logic had no effect ultimately. The provides list was being updated, but was not
            // being used anywhere else. Should we make it a property inside BasicModInfo so it's usable?

            //  List<ProvidedMod<LooseSemanticVersion>> provides = new ArrayList<>();
            //  if (quiltLoader.has("provides") && quiltLoader.get("provides").isJsonArray()) {
            //      for (JsonElement dependency : quiltLoader.getAsJsonArray("provides")) {
            //          if (!dependency.isJsonObject()) continue;
            //          JsonObject dependencyObject = dependency.getAsJsonObject();
            //          String dependencyId = getValidString(dependencyObject, "id");
            //          String providedVersion = version;
            //
            //          if (dependencyObject.has("versions")) {
            //              JsonElement dependencyVersion = dependencyObject.get("versions");
            //              if (dependencyVersion.isJsonPrimitive() && dependencyVersion.getAsJsonPrimitive().isString()) {
            //                  providedVersion = dependencyVersion.getAsString();
            //              }
            //          }
            //
            //          Optional<LooseSemanticVersion> semVer = LooseSemanticVersion.parse(providedVersion);
            //          provides.add(new ProvidedMod<>(dependencyId, semVer.orElse(null)));
            //      }
            //  }

            return new BasicModInfo[]{
                    ParserUtils.createModInfoFrom(
                            quiltLoader,
                            this,
                            new QuiltDependencyParser(),
                            new LooseSemanticVersion()
                    )
            };
        }

        @Override
        protected @NotNull BasicModInfo createNullableLoaderInfo(String loaderVersion) {
            Optional<Version> version = new LooseSemanticVersion().parse(loaderVersion);
            return new StandardBasicModInfo(
                    "quilt_loader",
                    "Quilt Loader",
                    version.orElse(null),
                    "The loader for mods under Quilt. It provides mod loading facilities and useful abstractions for other mods to use.",
                    new ArrayList<>(),
                    null,
                    this,
                    new ArrayList<>()
            );
        }
    };

    protected final ModInfoKeys modInfoKeys;

    private final String[] infoFilePaths;

    Platform(ModInfoKeys modInfoKeys, String... infoFilePaths) {
        this.modInfoKeys = modInfoKeys;
        this.infoFilePaths = infoFilePaths;
    }

    public String[] getInfoFilePaths() {
        return Arrays.copyOf(this.infoFilePaths, this.infoFilePaths.length);
    }

    /**
     * Parses a string into a {@link BasicModInfo}.
     *
     * @param toParse The string to parse, expected to be in the format of the specific platform (JSON, TOML, etc.).
     *                This string must match the format required by the platform; otherwise, parsing will fail.
     * @return An array of {@link BasicModInfo} objects containing mod information. Each object represents one specified mod namespace in the modinfo file.
     * @throws IllegalArgumentException If the input string is null.
     * @throws ModInfoParseException    If an error occurs while parsing the mod info file due to invalid formatting, missing fields, or an unexpected data structure.
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
     * @param zip The {@link IZipFile} to search for the platform-specific info file.
     * @return The content of the first matching info file as a string, or {@code null} if none are found.
     * This method reads the ZIP file once and stops searching after the first match.
     * @throws IOException If an error occurs while reading the zip file or its entries.
     */
    @NotNull
    public Optional<String> getInfoFileContent(IZipFile zip) throws IOException {
        for (String infoFilePath : this.infoFilePaths) {
            IZipEntry infoFileEntry = zip.findEntry(infoFilePath);
            if (infoFileEntry == null) continue;
            try (InputStream entry = zip.openEntry(infoFileEntry)) {
                if (entry == null) return Optional.empty();
                return Optional.of(readEverythingAsString(entry));
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
     *
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
     * If a mod supports multiple platforms (e.g., Forge and Fabric), both will be included.
     * @throws IOException If the file is not a valid zip archive or an I/O error occurs while reading the file.
     */
    @NotNull
    public static Platform[] findModPlatform(File file) throws IOException {
        Platform[] platforms;
        try (IZipFile zipFile = IZipFileFactory.Provider.create(file)) {
            platforms = findModPlatform(zipFile);
        }
        return platforms;
    }

    /**
     * Finds the mod platform(s) (e.g. Forge, Fabric) by inspecting the files inside the provided mod archive.
     * A mod can support multiple platforms, so this method can return multiple platforms.
     *
     * @param zip The ZipFile object of the mod jar you want to analyze
     * @return An array of {@link Platform} values representing the platforms found in the file.
     * If a mod supports multiple platforms (e.g., Forge and Fabric), both will be included.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public static Platform[] findModPlatform(IZipFile zip) throws IOException {
        List<Platform> platforms = new ArrayList<>();
        for (Platform platform : Platform.values()) {
            for (String infoFilePath : platform.infoFilePaths) {
                IZipEntry infoFileEntry = zip.findEntry(infoFilePath);
                if (infoFileEntry == null) continue;
                platforms.add(platform);
                break;
            }
        }
        return platforms.toArray(new Platform[0]);
    }

    public ModInfoKeys getModInfoKeys() {
        return modInfoKeys;
    }
}
