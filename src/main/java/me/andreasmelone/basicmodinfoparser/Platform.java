package me.andreasmelone.basicmodinfoparser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public enum Platform {
    /**
     * Legacy Forge platform, which uses the {@code mcmod.info} file containing JSON data.
     */
    FORGE_LEGACY((str) -> {
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(str, JsonArray.class).get(0).getAsJsonObject();
        String modId = jsonObj.get("modid").getAsString();
        String name = jsonObj.get("name").getAsString();
        String description = jsonObj.get("description").getAsString();
        String version = jsonObj.get("version").getAsString();

        return new BasicModInfo(modId, name, version, description);
    }, "mcmod.info"),
    /**
     * Forge platform, which uses the {@code mods.toml} file with TOML data.
     * It can also be used to parse NeoForge mods.
     */

    FORGE((str) -> {
        TomlParseResult result = Toml.parse(str);
        TomlTable modInfo = result.getArray("mods").getTable(0);

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
        String modId = jsonObj.get("id").getAsString();
        String name = jsonObj.get("name").getAsString();
        String description = jsonObj.get("description").getAsString();
        String version = jsonObj.get("version").getAsString();

        return new BasicModInfo(modId, name, version, description);
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
     * @throws JsonSyntaxException If the string is invalid JSON (for platforms like FABRIC and FORGE_LEGACY).
     * @throws IllegalStateException If the string does not match the expected TOML format (for FORGE).
     */
    public BasicModInfo parse(String toParse) {
        return parserFunction.apply(toParse);
    }

    /**
     * Reads and returns the content of the platform-specific info file (e.g. `mcmod.info`, `mods.toml`, etc.) from a zip archive.
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

    /**
     * Compares an array of paths to a single path to check if any match after normalisation.<p>
     * This ensures that two paths are considered equal, even if their string representations differ
     * (e.g., {@code run/embeddium.jar} and {@code .\run\embeddium.jar}).
     *
     * @param paths An array of paths to compare.
     * @param path2 The path to compare against.
     * @return {@code true} if any of the provided paths are equal to {@code path2} after normalisation, otherwise {@code false}.
     */
    private static boolean comparePaths(String[] paths, String path2) {
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
    private static String readEverythingAsString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder(in.available());
        int readBytes;
        byte[] buffer = new byte[1024];
        while((readBytes = in.read(buffer)) > 0) {
            sb.append(new String(buffer, 0, readBytes));
        }
        return sb.toString();
    }
}
