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
package me.andreasmelone.basicmodinfoparser.standalone;

import me.andreasmelone.basicmodinfoparser.modfile.DependencyChecker;
import me.andreasmelone.basicmodinfoparser.modfile.ModFile;
import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.PresenceStatus;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;
import me.andreasmelone.basicmodinfoparser.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;

// TODO clean this up and move all this garbage to Tests
public class Main {
    public static void main(String[] args) {
        File[] currentFiles = new File(".").listFiles();
        assert currentFiles != null;
        List<BasicModInfo> mods = new ArrayList<>();

        List<ModFile> modFiles = new ArrayList<>();
        for(File file : currentFiles) {
            if (!file.getName().endsWith(".jar") || !file.isFile()) continue;

            try {
//                JarInJarPlatform[] platforms = JarInJarPlatform.findJarInJarPlatforms(file);
//                for(JarInJarPlatform platform : platforms) {
//                    String infoContent = platform.getMetadataFileContent(jar).orElse("");
//                    System.out.print(file.getName() + ", " + platform + ": ");
//                    for (String modInfo : platform.getJarsInJar(infoContent)) {
//                        System.out.println(modInfo);
//                    }
//                    System.out.println();
//                }

//                Platform[] platforms = Platform.findModPlatform(file);
//                for(Platform platform : platforms) {
//                    String infoContent = platform.getInfoFileContent(jar).orElse("");
//                    System.out.print(file.getName() + ", " + platform + ": ");
//                    for (BasicModInfo modInfo : platform.parse(infoContent)) {
//                        System.out.println(modInfo.toString());
//                        mods.add(modInfo);
//                    }
//                    System.out.println();
//                }
//                ArrayDeque<ModFile> stack = new ArrayDeque<>();

                ModFile modFile = ModFile.create(file);
                modFiles.add(modFile);
//                System.out.println("File: " + file);
//                System.out.println("Platform: " + Arrays.asList(modFile.getPlatforms()));
//                System.out.println("Infos: " + Arrays.toString(modFile.getInfo()));
//                System.out.println("JIJ: " + modFile.getJarInJars());
//                System.out.println("ModFile: " + modFile);
//
//                InputStream iconStream = modFile.getIconStream();
//                if(iconStream != null) {
//                    BufferedImage image = ImageIO.read(iconStream);
//                    ImageIO.write(image, "png", new File(file.getAbsolutePath() + ".png"));
//                    iconStream.close();
//                }

//                modFile.getJarInJars().forEach(stack::addLast);
//
//                while(!stack.isEmpty()) {
//                    ModFile f = stack.removeFirst();
//                    f.getJarInJars().forEach(stack::addLast);
//                    System.out.println("|\tPlatform: " + Arrays.asList(f.getPlatforms()));
//                    System.out.println("|\tInfos: " + Arrays.toString(f.getInfo()));
//                    System.out.println("|\tJIJ: " + f.getJarInJars());
//                    System.out.println("|\tModFile: " + f);
//                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ModInfoParseException e) {
                System.out.println("Failed to parse jarfile: " + file);
                e.printStackTrace();
            }
        }

        long startTime = System.currentTimeMillis();
        modFiles.parallelStream().forEach(Main::modFileConsumer);
        System.out.println("Opening took " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println();

        Pair<Boolean, Map<Dependency, PresenceStatus>> result = DependencyChecker.checkDependencies("21", "1.21.4", Platform.FABRIC.createLoaderInfo("0.17.3").get(), modFiles);
        System.out.println("Passed: " + result.getFirst());
        result.getSecond().forEach((k,v) -> {
            System.out.println(k.getModId() + " (" + k.getVersionRange().getStringRepresentation() + "): " + v);
        });

        for (BasicModInfo mod : mods) {
            for (Dependency dependency : mod.getDependencies()) {
                List<BasicModInfo> newModsList = new ArrayList<>(mods);
                mod.getPlatform().createLoaderInfo(null).ifPresent(newModsList::add);

                System.out.println("Dependency " + dependency.getModId() + " for " + mod.getId() + ": ");
                if(dependency.getVersionRange() != null) System.out.println("\tRequired version: " + dependency.getVersionRange().getStringRepresentation());
                System.out.println("\tMandatory: " + dependency.isMandatory());
                System.out.println("\tPresent: " + dependency.isPresent(newModsList));
            }
        }
    }

    private static void modFileConsumer(ModFile modFile) {
        modFile.getInfo();
        modFile.getJarInJars().parallelStream().forEach(Main::modFileConsumer);
        System.out.println("Opened modfile: " + modFile);
    }
}