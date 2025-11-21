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
package me.andreasmelone.basicmodinfoparser.util;

import com.google.gson.Gson;
import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.parser.IDependencyParser;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.StandardBasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.model.ModInfoKeys;
import me.andreasmelone.basicmodinfoparser.util.adapter.DataAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class ParserUtils {
    public static final Gson GSON = new Gson();

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
     * Creates a {@link BasicModInfo} object from a {@link DataAdapter}.
     * <p>
     * This method parses a data adapter, extracts the required fields, and creates a new {@link BasicModInfo} object along with any given dependencies.
     * </p>
     *
     * @param modAdapter         The {@link DataAdapter} containing the mod information.
     * @param platform           The {@link Platform} this mod info belongs to.
     * @param dependenciesParser A {@link BiFunction} that takes an array of dependency keys and the
     *                           {@link DataAdapter} to parse dependencies from, returning a list of {@link Dependency}.
     * @param versionParser      A function to parse the version string.
     * @return A {@link BasicModInfo} object containing the mod information and its dependencies.
     */
    public static <T extends DataAdapter<?, ?>, D extends Dependency>
    @NotNull BasicModInfo createModInfoFrom(
            @NotNull T modAdapter,
            @NotNull Platform platform,
            @NotNull IDependencyParser<T, D> dependenciesParser,
            @NotNull Version versionParser
    ) {
        // Get miscellaneous information
        final ModInfoKeys modInfoKeys = platform.getModInfoKeys();
        String modId = modAdapter.getString(modInfoKeys.modIdKey).orElse(null);
        String name = modAdapter.getString(modInfoKeys.displayNameKey).orElse(null);
        String description = modAdapter.getString(modInfoKeys.descriptionKey).orElse(null);
        String version = modAdapter.getString(modInfoKeys.versionKey).orElse(null);
        String logo = modAdapter.getString(modInfoKeys.logoFileKey).orElse(null);

        // Parse Version
        Optional<Version> parsedVersion = versionParser.parse(version);

        // Get authors
        List<String> authorsList = modAdapter.getListOrString(modInfoKeys.authorsKey);

        // Get dependencies
        List<Dependency> dependencyList = new ArrayList<>(dependenciesParser.parse(
                modInfoKeys.dependencyKeys,
                modAdapter
        ));

        return new StandardBasicModInfo(
                modId,
                name,
                parsedVersion.orElse(null),
                description,
                dependencyList,
                logo,
                platform,
                authorsList
        );
    }

    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File createTempFile(String name) {
        return new File(getTempDir(), "basicmodinfoparser_" + name);
    }
}
