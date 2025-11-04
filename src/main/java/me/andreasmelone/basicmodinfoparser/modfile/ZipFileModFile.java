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

import me.andreasmelone.basicmodinfoparser.jarinjar.JarInJarPlatform;
import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import me.andreasmelone.basicmodinfoparser.util.ParserUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileModFile implements ModFile {
    private BasicModInfo[] infos;
    private List<ModFile> jarInJars;

    private final Platform[] platforms;
    private transient final ZipFile zipFile;

    private ZipFileModFile(ZipFile zipFile, Platform[] platforms) {
        this.zipFile = zipFile;
        this.platforms = platforms;
    }

    @Override
    public @NotNull BasicModInfo[] getInfo() throws ModInfoParseException {
        if (infos == null) {
            List<BasicModInfo> infos = new ArrayList<>();
            try {
                for (Platform platform : this.platforms) {
                    Optional<String> content = platform.getInfoFileContent(zipFile);
                    if (!content.isPresent()) continue;
                    infos.addAll(Arrays.asList(platform.parse(content.get())));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new BasicModInfo[0];
            }
            this.infos = infos.toArray(new BasicModInfo[0]);
        }
        return infos;
    }

    @Override
    public @NotNull BasicModInfo[] getInfo(Platform platform) throws ModInfoParseException {
        BasicModInfo[] infos = getInfo();
        List<BasicModInfo> filtered = new ArrayList<>();

        for (BasicModInfo info : infos) {
            if (info.getPlatform() == platform) filtered.add(info);
        }

        return filtered.toArray(new BasicModInfo[0]);
    }

    @Override
    public @Nullable InputStream getIconStream() throws IOException {
        String iconPath = null;
        for (BasicModInfo modInfo : this.getInfo()) {
            if (modInfo.getIconPath() != null) {
                iconPath = modInfo.getIconPath();
                break;
            }
        }
        if (iconPath == null) return null;

        ZipEntry iconEntry = zipFile.getEntry(iconPath);
        if (iconEntry != null) return zipFile.getInputStream(iconEntry);
        return null;
    }

    @Override
    public @NotNull Platform[] getPlatforms() {
        return platforms;
    }

    @Override
    public @NotNull List<ModFile> getJarInJars() {
        if (jarInJars == null) {
            List<ModFile> jars = new ArrayList<>();

            try {
                for (JarInJarPlatform jarInJar : JarInJarPlatform.findJarInJarPlatforms(zipFile)) {
                    String content = jarInJar.getMetadataFileContent(zipFile).orElse("");
                    List<String> innerJars = jarInJar.getJarsInJar(content);

                    for (String inJarPath : innerJars) {
                        ZipEntry entry = zipFile.getEntry(inJarPath);
                        if (entry == null) {
                            continue;
                        }

                        File tmpFile = ParserUtils.createTempFile(UUID.randomUUID() + ".jar");
                        tmpFile.deleteOnExit();

                        try (InputStream in = zipFile.getInputStream(entry);
                             OutputStream out = new FileOutputStream(tmpFile)) {
                            int readBytes;
                            byte[] buffer = new byte[65536]; // we're in 2025, we can allow ourselves an acceptably big buffer
                            while ((readBytes = in.read(buffer)) != -1) {
                                out.write(buffer, 0, readBytes);
                            }
                        }

                        try {
                            ModFile modFile = ModFile.create(tmpFile);
                            jars.add(modFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ModInfoParseException ignored) {
            }

            this.jarInJars = jars;
        }
        return jarInJars;
    }

    @Override
    public void init() {
        getInfo();
        getJarInJars();
    }

    @Override
    public void close() throws Exception {
        zipFile.close();
    }

    @Override
    public String toString() {
        return "ZipFileModFile{" +
                "infos=" + Arrays.toString(infos) +
                ", jarInJars=" + jarInJars +
                ", zipFile=" + zipFile +
                ", platforms=" + Arrays.toString(platforms) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ZipFileModFile that = (ZipFileModFile) o;
        return Objects.deepEquals(infos, that.infos) && Objects.equals(jarInJars, that.jarInJars) && Objects.equals(zipFile, that.zipFile) && Objects.deepEquals(platforms, that.platforms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(infos), jarInJars, zipFile, Arrays.hashCode(platforms));
    }

    public static ModFile create(ZipFile zipFile) throws IOException {
        Platform[] platforms = Platform.findModPlatform(zipFile);
        return new ZipFileModFile(zipFile, platforms);
    }

    public static ModFile create(File path) throws IOException {
        return create(new ZipFile(path));
    }
}
