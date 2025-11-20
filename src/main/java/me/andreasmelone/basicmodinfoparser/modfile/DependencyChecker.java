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
import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.PresenceStatus;
import me.andreasmelone.basicmodinfoparser.platform.dependency.fabric.LooseSemanticVersion;
import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.MavenVersion;
import me.andreasmelone.basicmodinfoparser.platform.modinfo.StandardBasicModInfo;
import me.andreasmelone.basicmodinfoparser.util.Pair;

import java.util.*;

public class DependencyChecker {
    /**
     * Checks whether all dependencies are present and match the version.
     * This method performs a lot of looping and may perform a lot of IO, which
     * can lead to a lot of CPU time. Due to that, it is recommended to run this method asynchronously if possible.
     *
     * @param javaVersion the version of java, may be null or an invalid string to not match against java. This is only useful on fabric or quilt, as they are the only
     *                    loaders that provide a java dependency.
     * @param gameVersion the version of the game, for example {@code 1.20.1}. May be null if you want to ignore checking against it.
     * @param loaderInfo  the info on the loader, usually created using {@link Platform#createLoaderInfo(String)}
     * @param modFiles    a List of {@link ModFile}, which represents all mods, including their dependencies and
     * @return a boolean and a Map of {@link Dependency} to {@link PresenceStatus}.
     * the boolean represents the status, true means that all dependencies are met, whereas false means that some aren't.
     * the Map shows exactly which dependencies were checked, which of them are present, not present or present of a wrong version.
     */
    public static Pair<Boolean, Map<Dependency, PresenceStatus>> checkDependencies(String javaVersion, String gameVersion, BasicModInfo loaderInfo, List<ModFile> modFiles) {
        BasicModInfo javaInfo = null;
        boolean isFabricBased = loaderInfo.getPlatform() == Platform.FABRIC || loaderInfo.getPlatform() == Platform.QUILT;
        if (isFabricBased) {
            javaInfo = new StandardBasicModInfo(
                    "java",
                    "Java",
                    new LooseSemanticVersion().parse(javaVersion).orElse(null),
                    "Java",
                    new ArrayList<>(),
                    null,
                    Platform.FABRIC,
                    null
            );
        }
        BasicModInfo gameInfo = new StandardBasicModInfo(
                "minecraft",
                "Minecraft",
                (isFabricBased ? new LooseSemanticVersion().parse(gameVersion) : new MavenVersion().parse(gameVersion)).orElse(null),
                "Minecraft",
                new ArrayList<>(),
                null,
                loaderInfo.getPlatform(),
                null
        );

        Map<Dependency, PresenceStatus> dependencyMap = new HashMap<>();

        List<BasicModInfo> infos = new ArrayList<>();
        List<Dependency> dependencies = new ArrayList<>();

        Deque<ModFile> stack = new ArrayDeque<>(modFiles);

        if (javaInfo != null) infos.add(javaInfo);
        infos.add(gameInfo);
        infos.add(loaderInfo);
        while (!stack.isEmpty()) {
            ModFile modFile = stack.removeFirst();
            stack.addAll(modFile.getJarInJars());

            BasicModInfo[] info = modFile.getInfo();
            for (BasicModInfo basicModInfo : info) {
                infos.add(basicModInfo);
                dependencies.addAll(basicModInfo.getDependencies());
            }
        }

        boolean isOkay = true;
        for (Dependency dependency : dependencies) {
            PresenceStatus present = dependency.isPresent(infos);
            dependencyMap.put(dependency, present);
            if (!present.isSuccess()) isOkay = false;
        }

        return Pair.of(isOkay, dependencyMap);
    }
}
