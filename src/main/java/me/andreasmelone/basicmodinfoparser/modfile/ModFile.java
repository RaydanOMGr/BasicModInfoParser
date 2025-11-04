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
package me.andreasmelone.basicmodinfoparser.modfile;

import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ModFile extends AutoCloseable {
    /**
     * May be lazy initialized
     *
     * @return the {@link BasicModInfo} objects associated with this jar
     */
    @NotNull
    BasicModInfo[] getInfo() throws ModInfoParseException;

    /**
     * May be lazy initialized
     *
     * @param platform the given platform
     * @return the {@link BasicModInfo} objects from this jar of the given platform
     */
    @NotNull
    BasicModInfo[] getInfo(Platform platform) throws ModInfoParseException;

    /**
     * This may cause initialization of {@link ModFile#getInfo()}
     *
     * @return an input stream with the icons bytes
     */
    @Nullable
    InputStream getIconStream() throws IOException;

    /**
     * Retrieves the platforms, these are not lazily initialized (like {@link ModFile#getInfo()} may be) so this is safe to access
     *
     * @return the platforms
     */
    @NotNull
    Platform[] getPlatforms();

    /**
     * May be lazy initialized, as the data may depend on information from {@link ModFile#getInfo()}
     *
     * @return a list of jar-in-jar objects
     */
    @NotNull
    List<ModFile> getJarInJars();

    /**
     * This method initializes all the lazily initialized fields. May take a while as it usually performs heavy IO.
     */
    void init();

    static ModFile create(File file) throws IOException {
        return ZipFileModFile.create(file);
    }
}
