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

import me.andreasmelone.basicmodinfoparser.BasicModInfo;
import me.andreasmelone.basicmodinfoparser.Platform;
import me.andreasmelone.basicmodinfoparser.dependency.version.MavenVersion;
import me.andreasmelone.basicmodinfoparser.dependency.version.SemanticVersion;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.jar.JarFile;

public class Main {
    public static void main(String[] args) {
        File[] currentFiles = new File(".").listFiles();
        assert currentFiles != null;
        for(File file : currentFiles) {
            if (!file.getName().endsWith(".jar") || !file.isFile()) return;
            JarFile jar;
            try {
                jar = new JarFile(file);
            } catch (IOException ignored) {
                return;
            }

            try {
                Platform[] platforms = Platform.findModPlatform(file);
                for(Platform platform : platforms) {
                    String infoContent = platform.getInfoFileContent(jar).orElse("");
                    System.out.print(file.getName() + ", " + platform + ": ");
                    for (BasicModInfo modInfo : platform.parse(infoContent)) {
                        System.out.println(modInfo.toString());
                    }
                    System.out.println();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

//        Optional<MavenVersion> ver = MavenVersion.parse("1.0.0-alpha.1+metadata");
//        System.out.println("1 parsed?: " + ver.isPresent());
//        System.out.println("1 result: " + ver.orElse(null));
//
//        ver = MavenVersion.parse("1.0.0");
//        System.out.println("2 parsed?: " + ver.isPresent());
//        System.out.println("2 result: " + ver.orElse(null));
//
//        ver = MavenVersion.parse("1.0.0+metadata");
//        System.out.println("3 parsed?: " + ver.isPresent());
//        System.out.println("3 result: " + ver.orElse(null));
//
//        ver = MavenVersion.parse("1.0.0-alpha");
//        System.out.println("4 parsed?: " + ver.isPresent());
//        System.out.println("4 result: " + ver.orElse(null));
//
//        ver = MavenVersion.parse("1.0.0-alpha.1");
//        System.out.println("5 parsed?: " + ver.isPresent());
//        System.out.println("5 result: " + ver.orElse(null));
//
//        ver = MavenVersion.parse("1.0-alpha.1+metadata");
//        System.out.println("6 parsed?: " + ver.isPresent());
//        System.out.println("6 result: " + ver.orElse(null));

//        Optional<SemanticVersion> ver = SemanticVersion.parse("11.0.0-alpha.3+0.102.0-1.21");
//        System.out.println("parsed: " + ver.isPresent());
//        System.out.println("result: " + ver.orElse(null));
    }
}