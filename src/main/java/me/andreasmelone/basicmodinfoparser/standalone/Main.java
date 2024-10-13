package me.andreasmelone.basicmodinfoparser.standalone;

import me.andreasmelone.basicmodinfoparser.Platform;

import java.io.File;
import java.io.IOException;
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
                    String infoContent = platform.getInfoFileContent(jar);
                    System.out.println(platform.parse(infoContent).toString());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}