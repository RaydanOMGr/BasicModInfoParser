package me.andreasmelone.basicmodinfoparser.dependency.version;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class SimpleVersion implements Version<SimpleVersion> {
    private final int[] versionInts;

    public SimpleVersion(int[] versionInts) {
        this.versionInts = versionInts;
    }

    @Override
    public String toString() {
        return "SimpleVersion{" +
                "versionInts=" + Arrays.toString(versionInts) +
                '}';
    }

    @Override
    public int compareTo(@NotNull SimpleVersion o) {
        int lowestAmount = Math.min(this.versionInts.length, o.versionInts.length);
        for (int i = 0; i < lowestAmount; i++) {
            if (this.versionInts[i] != o.versionInts[i]) {
                return Integer.compare(this.versionInts[i], o.versionInts[i]);
            }
        }

        if (this.versionInts.length > lowestAmount) {
            for (int i = lowestAmount; i < this.versionInts.length; i++) {
                if (this.versionInts[i] > 0) return 1;
            }
        } else if (o.versionInts.length > lowestAmount) {
            for (int i = lowestAmount; i < o.versionInts.length; i++) {
                if (o.versionInts[i] > 0) return -1;
            }
        }

        return 0;
    }

    public static Optional<SimpleVersion> parse(String ver) {
        String[] split = ver.split("\\.");
        int[] ints = new int[split.length];

        try {
            for(int i = 0; i < ints.length; i++) {
                ints[i] = Integer.parseUnsignedInt(split[i]);
            }
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
        return new SimpleVersion(ints).optional();
    }
}
