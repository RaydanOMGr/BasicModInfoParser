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
import java.util.zip.ZipInputStream;

public class StandardModFile implements ModFile {
    private BasicModInfo[] infos;
    private List<ModFile> jarInJars;

    private final File path;
    private final Platform[] platforms;

    private StandardModFile(File path, Platform[] platforms) {
        this.path = path;
        this.platforms = platforms;
    }

    @Override
    public @NotNull BasicModInfo[] getInfo() throws ModInfoParseException {
        if(infos == null) {
            List<BasicModInfo> infos = new ArrayList<>();
            try(ZipFile jar = new ZipFile(path)) {
                for (Platform platform : this.platforms) {
                    Optional<String> content = platform.getInfoFileContent(jar);
                    if(!content.isPresent()) continue;
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
            if(info.getPlatform() == platform) filtered.add(info);
        }

        return filtered.toArray(new BasicModInfo[0]);
    }

    @Override
    public @Nullable InputStream getIconStream() throws IOException {
        String iconPath = null;
        for (BasicModInfo modInfo : this.getInfo()) {
            if(modInfo.getIconPath() != null) {
                iconPath = modInfo.getIconPath();
                break;
            }
        }
        if(iconPath == null) return null;

        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(path));
        ZipEntry entry;
        while((entry = zipInput.getNextEntry()) != null) {
            if(entry.getName().equalsIgnoreCase(iconPath)) return zipInput;
        }
        zipInput.close();
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

            try (ZipFile file = new ZipFile(path)) {
                try {
                    for (JarInJarPlatform jarInJar : JarInJarPlatform.findJarInJarPlatforms(path)) {
                        String content = jarInJar.getMetadataFileContent(file).orElse("");
                        List<String> innerJars = jarInJar.getJarsInJar(content);

                        for (String inJarPath : innerJars) {
                            ZipEntry entry = file.getEntry(inJarPath);
                            if (entry == null) {
                                continue;
                            }

                            File tmpFile = ParserUtils.createTempFile(UUID.randomUUID() + ".jar");
                            tmpFile.deleteOnExit();

                            try (InputStream in = file.getInputStream(entry);
                                 OutputStream out = new FileOutputStream(tmpFile)) {
                                int readBytes;
                                byte[] buffer = new byte[2048];
                                while((readBytes = in.read(buffer)) != -1) {
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
                } catch (ModInfoParseException ignored) {}
            } catch (IOException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

            this.jarInJars = jars;
        }
        return jarInJars;
    }

    public static ModFile create(File path) throws IOException {
        Platform[] platforms = Platform.findModPlatform(path);
        return new StandardModFile(path, platforms);
    }

    @Override
    public String toString() {
        return "StandardModFile{" +
                "infos=" + Arrays.toString(infos) +
                ", jarInJars=" + jarInJars +
                ", path=" + path +
                ", platforms=" + Arrays.toString(platforms) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StandardModFile that = (StandardModFile) o;
        return Objects.equals(path, that.path) && Objects.deepEquals(platforms, that.platforms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, Arrays.hashCode(platforms));
    }
}
