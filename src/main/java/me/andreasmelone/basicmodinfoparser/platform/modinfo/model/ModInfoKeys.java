package me.andreasmelone.basicmodinfoparser.platform.modinfo.model;

import java.util.Objects;

/**
 * Holds information about the available keys inside a mod's information file.
 *
 * @see me.andreasmelone.basicmodinfoparser.platform.Platform
 */
public class ModInfoKeys {
    /**
     * The key name to access a mod's ID.
     */
    public final String modIdKey;

    /**
     * The key name to access a mod's display name.
     */
    public final String displayNameKey;

    /**
     * The key name to access a mod's version.
     */
    public final String versionKey;

    /**
     * The key name to access a mod's description.
     */
    public final String descriptionKey;

    /**
     * The key name to access a mod's logo.
     */
    public final String logoFileKey;

    public final String authorsKey;

    /**
     * The key names to access a mod's dependencies. Loaders such as Fabric may provide multiple
     * keys to declare dependencies and compatibility, so an array is necessary.
     *
     * @see <a href="https://wiki.fabricmc.net/documentation:fabric_mod_json">
     * Fabric's documentation on dependency management
     * </a>
     */
    public final String[] dependencyKeys;

    public ModInfoKeys(
            String modIdKey,
            String displayNameKey,
            String versionKey,
            String descriptionKey,
            String logoFileKey,
            String authorsKey,
            String[] dependencyKeys
    ) {
        this.modIdKey = modIdKey;
        this.displayNameKey = displayNameKey;
        this.versionKey = versionKey;
        this.descriptionKey = descriptionKey;
        this.logoFileKey = logoFileKey;
        this.authorsKey = authorsKey;
        this.dependencyKeys = dependencyKeys;
    }

    /**
     * Helper method to create a forge-and-neoforge-compliant {@link ModInfoKeys}
     *
     * @return a forge-and-neoforge-compliant {@link ModInfoKeys}
     */
    public static ModInfoKeys forgeKeys() {
        return new ModInfoKeys(
                "modId",
                "displayName",
                "version",
                "description",
                "logoFile",
                "authors",
                new String[]{"dependencies"}
        );
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this == other) return true;
        if (this.getClass() != other.getClass()) return false;

        ModInfoKeys castOther = (ModInfoKeys) other;

        return castOther.modIdKey.equals(modIdKey)
                && castOther.displayNameKey.equals(displayNameKey)
                && castOther.versionKey.equals(versionKey)
                && castOther.descriptionKey.equals(descriptionKey)
                && castOther.logoFileKey.equals(logoFileKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modIdKey, displayNameKey, versionKey, descriptionKey, logoFileKey);
    }

    @Override
    public String toString() {
        return "ModInfoKeys{"
                + "modIdKey=" + modIdKey
                + ", displayNameKey=" + displayNameKey
                + ", versionKey=" + versionKey
                + ", descriptionKey=" + descriptionKey
                + ", logoFileKey=" + logoFileKey
                + "}";
    }
}
