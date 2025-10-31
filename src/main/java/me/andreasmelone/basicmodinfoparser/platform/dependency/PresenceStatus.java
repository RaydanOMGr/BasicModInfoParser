package me.andreasmelone.basicmodinfoparser.platform.dependency;

/**
 * The presence status of a dependency
 */
public enum PresenceStatus {
    /**
     * The dependency is present and the version bounds match
     */
    PRESENT(true),
    /**
     * The dependency is present but its version doesn't match the range
     */
    VERSION_MISMATCH(false),
    /**
     * The dependency is not present
     */
    NOT_PRESENT(false);

    private final boolean success;

    PresenceStatus(boolean success) {
        this.success = success;
    }

    /**
     * @return if this constant represents a success
     */
    public boolean isSuccess() {
        return success;
    }
}
