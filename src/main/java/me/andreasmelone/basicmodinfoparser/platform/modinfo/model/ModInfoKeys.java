package me.andreasmelone.basicmodinfoparser.platform.modinfo.model;

import java.util.Objects;

/**
 * Holds information about the available keys inside a mod's information file and their respective names.
 *
 * @see me.andreasmelone.basicmodinfoparser.platform.Platform
 */
public class ModInfoKeys {
    public final String modIdKey;
    public final String displayNameKey;
    public final String versionKey;
    public final String descriptionKey;
    public final String logoFileKey;
    public final String[] dependencyKeys;

    public ModInfoKeys(
            String modIdKey,
            String displayNameKey,
            String versionKey,
            String descriptionKey,
            String logoFileKey,
            String[] dependencyKeys
    ) {
        this.modIdKey = modIdKey;
        this.displayNameKey = displayNameKey;
        this.versionKey = versionKey;
        this.descriptionKey = descriptionKey;
        this.logoFileKey = logoFileKey;
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
                new String[]{"dependencies"}
        );
    }

    /**
     * Helper method to create a fabric-and-quilt-compliant {@link ModInfoKeys}
     *
     * @return a fabric-and-quilt-compliant {@link ModInfoKeys}
     */
    public static ModInfoKeys fabricKeys() {
        return new ModInfoKeys(
                "id",
                "name",
                "version",
                "description",
                "icon",
                new String[]{
                        "depends",
                        "breaks",
                        "provides"
                }
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
