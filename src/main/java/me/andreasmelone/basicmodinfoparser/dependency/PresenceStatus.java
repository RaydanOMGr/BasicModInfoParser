package me.andreasmelone.basicmodinfoparser.dependency;

/**
 * The presence status of a dependency
 */
public enum PresenceStatus {
    /**
     * The dependency is present and the version bounds match
     */
    PRESENT(true),
    /**
     * The dependency is present but its version is too low
     */
    VERSION_TOO_LOW(false),
    /**
     * The dependency is present but its version is too high
     */
    VERSION_TOO_HIGH(false),
    /**
     * The dependency is not present
     */
    NOT_PRESENT(false);

    private final boolean success;

    PresenceStatus(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
