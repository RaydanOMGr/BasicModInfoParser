package me.andreasmelone.basicmodinfoparser;

import com.google.gson.*;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import me.andreasmelone.basicmodinfoparser.util.ParserUtils;
import org.tomlj.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static me.andreasmelone.basicmodinfoparser.util.ParserUtils.*;

public enum Platform {
    /**
     * Legacy Forge platform, which uses the {@code mcmod.info} file containing JSON data.
     */
    FORGE_LEGACY((str) -> {
        Gson gson = new Gson();
        JsonArray topArray = gson.fromJson(str, JsonArray.class);
        if (topArray == null || topArray.size() == 0 || !topArray.get(0).isJsonObject()) {
            return BasicModInfo.empty();
        }
        return ParserUtils.createModInfoFromJsonObject(topArray.get(0).getAsJsonObject(),
                "modid", "name", "version", "description");
    }, "mcmod.info"),
    /**
     * Forge platform, which uses the {@code mods.toml} file with TOML data.
     * It can also be used to parse NeoForge mods.
     */
    FORGE((str) -> {
        TomlParseResult result = Toml.parse(str);
        TomlArray modsArray = result.getArray("mods");
        if(modsArray == null || modsArray.isEmpty()) return BasicModInfo.empty();
        TomlTable modInfo = modsArray.getTable(0);
        if(modInfo.isEmpty()) return BasicModInfo.empty();

        String modId = modInfo.getString("modId");
        String name = modInfo.getString("displayName");
        String description = modInfo.getString("description");
        String version = modInfo.getString("version");

        return new BasicModInfo(modId, name, version, description);
    }, "META-INF/mods.toml", "META-INF/neoforge.mods.toml"),
    /**
     * Fabric platform, which uses the {@code fabric.mod.json} file. As the extension suggests, it stores data in JSON format.
     */
    FABRIC((str) -> {
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(str, JsonObject.class);
        if(jsonObj == null) return BasicModInfo.empty();
        return ParserUtils.createModInfoFromJsonObject(jsonObj,
                "modId", "name", "version", "description");
    }, "fabric.mod.json");

    private final String[] infoFilePaths;
    private final Function<String, BasicModInfo> parserFunction;

    Platform(Function<String, BasicModInfo> parserFunction, String... infoFilePaths) {
        this.infoFilePaths = infoFilePaths;
        this.parserFunction = parserFunction;
    }

    /**
     * Parses a string into a {@link BasicModInfo}.
     *
     * @param toParse The string to parse, expected to be in the format of the specific platform (JSON, TOML, etc.).
     *                This string must match the format required by the platform; otherwise, parsing will fail.
     * @return The {@link BasicModInfo} object containing mod information.
     * @throws IllegalArgumentException If the input string is null.
     * @throws ModInfoParseException If an error occurs while parsing the TOML (for Forge) or JSON (for Legacy Forge and Fabric).
     */
    public BasicModInfo parse(String toParse) {
        if (toParse == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }

        try {
            return parserFunction.apply(toParse);
        } catch (TomlParseError | TomlInvalidTypeException | JsonParseException e) {
            throw new ModInfoParseException("Error parsing the mod info from the given string.", e);
        }
    }


    /**
     * Reads and returns the content of the platform-specific info file (e.g. {@code mcmod.info}, {@code mods.toml}, etc.) from a zip archive.
     *
     * @param zip The {@link ZipFile} to search for the platform-specific info file. It is allowed to be a derivative of such, e.g. {@link JarFile}
     * @return The content of the info file as a string, or {@code null} if the info file was not found.
     * @throws IOException If an error occurs while reading the zip file or its entries.
     */
    public String getInfoFileContent(ZipFile zip) throws IOException {
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip.getName()))) {
            ZipEntry next;
            while ((next = in.getNextEntry()) != null) {
                if (next.isDirectory()) continue;
                if (comparePaths(this.infoFilePaths, next.getName())) {
                    try (InputStream entry = zip.getInputStream(next)) {
                        return readEverythingAsString(entry);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the mod platform(s) (e.g. Forge, Fabric) by inspecting the files inside the provided mod archive.
     * A mod can support multiple platforms, so this method can return multiple platforms.
     *
     * @param file The mod file, which must be a zip archive (e.g., .jar, .zip) or a similar format.
     * @return An array of {@link Platform} values representing the platforms found in the file.
     *         If no platforms are found, returns an empty array.
     * @throws IOException If the file is not a valid zip archive or an I/O error occurs while reading the file.
     */
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
