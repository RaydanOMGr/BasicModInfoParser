package me.andreasmelone.abstractzip.jvm;

import me.andreasmelone.abstractzip.IZipEntry;
import me.andreasmelone.abstractzip.IZipFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JvmZipFile implements IZipFile {
    final ZipFile jvmZipFile;

    public JvmZipFile(ZipFile jvmZipFile) {
        this.jvmZipFile = jvmZipFile;
    }

    @Override
    public @Nullable InputStream openEntry(@NotNull IZipEntry entry) throws IOException {
        if(!(entry instanceof JvmZipEntry)) {
            return null;
        }
        return this.jvmZipFile.getInputStream(((JvmZipEntry) entry).jvmZipEntry);
    }

    @Override
    public @Nullable IZipEntry findEntry(@NotNull String name) {
        ZipEntry jvmZipEntry = jvmZipFile.getEntry(name);
        if(jvmZipEntry == null) return null;
        return new JvmZipEntry(jvmZipEntry);
    }

    @Override
    public @Nullable String getComment() {
        return this.jvmZipFile.getComment();
    }

    @Override
    public void close() throws IOException {
        this.jvmZipFile.close();
    }
}
