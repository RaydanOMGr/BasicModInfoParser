package me.andreasmelone.basicmodinfoparser.platform;

import me.andreasmelone.basicmodinfoparser.platform.dependency.Dependency;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BasicModInfo {
    /**
     * The mod id, which is usually required to be ^[a-z][a-z0-9_]{1,63}$
     * @return the mod id
     */
    @Nullable String getId();

    /**
     * The name of the mod
     * @return the name of the mod
     */
    @Nullable String getName();

    /**
     * The version of the mod. In difference to {@link Dependency}, this is not a version range
     * @return the version of the mod
     */
    @Nullable Version<?> getVersion();

    /**
     * The mods description
     * @return the mods description
     */
    @Nullable String getDescription();

    /**
     * The mods dependencies
     * @return the mods dependencies
     * @see Dependency
     */
    @NotNull List<Dependency> getDependencies();

    /**
     * @return the path to the icon in the jarfile
     */
    @Nullable String getIconPath();

    /**
     * @return the platform that this mod is for
     */
    @NotNull Platform getPlatform();
}
