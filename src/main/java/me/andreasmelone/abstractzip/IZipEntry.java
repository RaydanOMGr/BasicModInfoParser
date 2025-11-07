package me.andreasmelone.abstractzip;

import org.jetbrains.annotations.NotNull;

public interface IZipEntry {
    @NotNull String getName();
    long getTime();
    long getSize();
    long getCompressedSize();
    long getCrc();
    String getComment();
}
