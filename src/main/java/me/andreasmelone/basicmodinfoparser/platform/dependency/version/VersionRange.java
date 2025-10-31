package me.andreasmelone.basicmodinfoparser.platform.dependency.version;

public interface VersionRange<T extends Version<T>> {
    /**
     * @return the string representation of the version range
     */
    String getStringRepresentation();

    /**
     * @param version a version of type {@code T}
     * @return whether the version is contained in this version range
     * @see VersionRange#getType()
     */
    boolean contains(T version);

    /**
     * @return the type of {@link Version} this range takes
     */
    Class<T> getType();
}
