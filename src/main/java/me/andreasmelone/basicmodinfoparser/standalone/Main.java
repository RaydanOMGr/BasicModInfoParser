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

import me.andreasmelone.basicmodinfoparser.jarinjar.JarInJarPlatform;
import me.andreasmelone.basicmodinfoparser.modfile.ModFile;
import me.andreasmelone.basicmodinfoparser.platform.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.platform.Platform;
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.util.ModInfoParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

// TODO clean this up and move all this garbage to Tests
public class Main {
    public static void main(String[] args) {
        File[] currentFiles = new File(".").listFiles();
        assert currentFiles != null;
        List<BasicModInfo> mods = new ArrayList<>();

        for(File file : currentFiles) {
            if (!file.getName().endsWith(".jar") || !file.isFile()) continue;
            JarFile jar;
            try {
                jar = new JarFile(file);
            } catch (IOException ignored) {
                return;
            }


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

                Platform[] platforms = Platform.findModPlatform(file);
                for(Platform platform : platforms) {
                    String infoContent = platform.getInfoFileContent(jar).orElse("");
                    System.out.print(file.getName() + ", " + platform + ": ");
                    for (BasicModInfo modInfo : platform.parse(infoContent)) {
                        System.out.println(modInfo.toString());
                        mods.add(modInfo);
                    }
                    System.out.println();
                }
//                ArrayDeque<ModFile> stack = new ArrayDeque<>();

//                ModFile modFile = ModFile.create(file);
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

//        Optional<LooseSemanticVersion> ver = LooseSemanticVersion.parse("1.0.0-alpha.1+metadata");
//        System.out.println("1 parsed?: " + ver.isPresent());
//        System.out.println("1 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.0.0");
//        System.out.println("2 parsed?: " + ver.isPresent());
//        System.out.println("2 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.0.0+metadata");
//        System.out.println("3 parsed?: " + ver.isPresent());
//        System.out.println("3 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.0.0-alpha");
//        System.out.println("4 parsed?: " + ver.isPresent());
//        System.out.println("4 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.0.0-alpha.1");
//        System.out.println("5 parsed?: " + ver.isPresent());
//        System.out.println("5 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.0-alpha.1+metadata");
//        System.out.println("6 parsed?: " + ver.isPresent());
//        System.out.println("6 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.0.0.0-alpha.1+metadata");
//        System.out.println("7 parsed?: " + ver.isPresent());
//        System.out.println("7 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.2-");
//        System.out.println("8 parsed?: " + ver.isPresent());
//        System.out.println("8 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.2.x", true);
//        System.out.println("9 parsed?: " + ver.isPresent());
//        System.out.println("9 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.2.*", true);
//        System.out.println("10 parsed?: " + ver.isPresent());
//        System.out.println("10 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.2.X", true);
//        System.out.println("11 parsed?: " + ver.isPresent());
//        System.out.println("11 result: " + ver.orElse(null));
//
//        ver = LooseSemanticVersion.parse("1.x.4", true);
//        System.out.println("12 parsed?: " + ver.isPresent());
//        System.out.println("12 result: " + ver.orElse(null));

//        Optional<SemanticVersion> ver = SemanticVersion.parse("11.0.0-alpha.3+0.102.0-1.21");
//        System.out.println("parsed: " + ver.isPresent());
//        System.out.println("result: " + ver.orElse(null));

//        Optional<FabricVersionRange> range = FabricVersionRange.parse("*");
//        System.out.println("1 parsed: " + range.isPresent());
//        System.out.println("1 result: " + range.orElse(null));
//        if(range.isPresent()) {
//            LooseSemanticVersion onetwentyone = LooseSemanticVersion.parse("1.20.1+otherbuildmetadata").get();
//            LooseSemanticVersion onetwentyoneone = LooseSemanticVersion.parse("1.21.1+42kemerovskayaoblast").get();
//            LooseSemanticVersion onetwentysix = LooseSemanticVersion.parse("1.20.6").get();
//            LooseSemanticVersion onetentwo = LooseSemanticVersion.parse("1.10.2").get();
//            LooseSemanticVersion onetwentyonealpha = LooseSemanticVersion.parse("1.20.1-alpha").get();
//            LooseSemanticVersion onetwentyonealpha2 = LooseSemanticVersion.parse("1.20.1-alpha.2").get();
//            LooseSemanticVersion onetwentytwonine = LooseSemanticVersion.parse("1.20.2.9").get();
//
//            System.out.println("1 tests: ");
//            System.out.println("\t1.20.1: " + range.get().contains(onetwentyone));
//            System.out.println("\t1.21.1: " + range.get().contains(onetwentyoneone));
//            System.out.println("\t1.20.6: " + range.get().contains(onetwentysix));
//            System.out.println("\t1.10.2: " + range.get().contains(onetentwo));
//            System.out.println("\t1.20.1-alpha: " + range.get().contains(onetwentyonealpha));
//            System.out.println("\t1.20.1-alpha.2: " + range.get().contains(onetwentyonealpha2));
//            System.out.println("\t1.20.2.9: " + range.get().contains(onetwentytwonine));
//        }
    }
}