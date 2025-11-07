package me.andreasmelone.abstractzip.jvm;

import me.andreasmelone.abstractzip.IZipEntry;
import org.jetbrains.annotations.NotNull;

import java.util.zip.ZipEntry;

public class JvmZipEntry implements IZipEntry {
    final ZipEntry jvmZipEntry;

    public JvmZipEntry(ZipEntry jvmZipEntry) {
        this.jvmZipEntry = jvmZipEntry;
    }

    @Override
    public @NotNull String getName() {
        return this.jvmZipEntry.getName();
    }

    @Override
    public long getTime() {
        return this.jvmZipEntry.getTime();
    }

    @Override
    public long getSize() {
        return this.jvmZipEntry.getSize();
    }

    @Override
    public long getCompressedSize() {
        return this.jvmZipEntry.getCompressedSize();
    }

    @Override
    public long getCrc() {
        return this.jvmZipEntry.getCrc();
    }

    @Override
    public String getComment() {
        return this.jvmZipEntry.getComment();
    }
}
