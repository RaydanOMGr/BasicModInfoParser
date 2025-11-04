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

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// TODO clean this up and move all this garbage to Tests
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        File[] currentFiles = new File(".").listFiles();
        assert currentFiles != null;
        List<BasicModInfo> mods = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        List<ModFile> modFiles = new ArrayList<>();
        Arrays.stream(currentFiles).forEach(file -> {
            if(!file.isFile() || !file.getName().endsWith(".jar")) return;
            try {
                ModFile modFile = ModFile.create(file);
                modFiles.add(modFile);
                System.out.println("Loaded " + file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ModInfoParseException e) {
                System.out.println("Failed to parse jarfile: " + file);
                e.printStackTrace();
            }
        });
        System.out.println("Collecting took " + (System.currentTimeMillis() - startTime) + "ms");

        startTime = System.currentTimeMillis();
        modFiles.forEach(Main::modFileConsumer);
        System.out.println("Opening took " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println();

        startTime = System.currentTimeMillis();
        Pair<Boolean, Map<Dependency, PresenceStatus>> result = DependencyChecker.checkDependencies("21", "1.21.1", Platform.NEOFORGE.createLoaderInfo("21.1.211").get(), modFiles);
        System.out.println("Dependency check took " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Passed: " + result.getFirst());
        result.getSecond().forEach((k,v) -> {
            String versionRep = k.getVersionRange() != null ? " (" + k.getVersionRange().getStringRepresentation() + ")" : "";
            System.out.println(k.getModId() + versionRep + ": " + v);
        });

        modFiles.parallelStream().forEach((file) -> {
            try {
                file.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
//        try {
//            InputStream iconStream = modFile.getIconStream();
//            if(iconStream != null) ImageIO.write(ImageIO.read(iconStream), "png", new File(modFile.getInfo()[0].getName() + ".png"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        modFile.init();
        System.out.println("Read icon for modfile: " + modFile);
    }
}