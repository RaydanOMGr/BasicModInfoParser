package me.andreasmelone.abstractzip.jvm;

import me.andreasmelone.abstractzip.IZipFile;
import me.andreasmelone.abstractzip.IZipFileFactory;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class JvmZipFileFactory implements IZipFileFactory {
    @Override
    public IZipFile create(File path) throws IOException {
        return new JvmZipFile(new ZipFile(path));
    }
}
