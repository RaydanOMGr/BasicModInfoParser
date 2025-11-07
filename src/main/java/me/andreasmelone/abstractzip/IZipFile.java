package me.andreasmelone.abstractzip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface IZipFile extends Closeable {
    @Nullable InputStream openEntry(@NotNull IZipEntry entry) throws IOException;
    @Nullable IZipEntry findEntry(@NotNull String name);
    @Nullable String getComment();
}
