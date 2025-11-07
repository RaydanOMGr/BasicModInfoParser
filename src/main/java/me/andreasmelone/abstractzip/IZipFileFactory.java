package me.andreasmelone.abstractzip;

import me.andreasmelone.abstractzip.jvm.JvmZipFileFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public interface IZipFileFactory {
    IZipFile create(File path) throws IOException;

    class Provider {
        public static final IZipFileFactory DEFAULT_FACTORY = new JvmZipFileFactory();
        private static IZipFileFactory FACTORY = DEFAULT_FACTORY;

        // if somebody calls this with null as the factoryImpl I will die
        public static void setFactoryImpl(@NotNull IZipFileFactory factoryImpl) {
            FACTORY = factoryImpl;
        }

        public static IZipFile create(File path) throws IOException {
            return FACTORY.create(path);
        }
    }
}
