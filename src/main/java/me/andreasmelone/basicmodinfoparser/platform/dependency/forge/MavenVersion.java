/*
 * MIT License
 *
 * Copyright (c) 2024-2025 RaydanOMGr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.andreasmelone.basicmodinfoparser.platform.dependency.forge;

import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class partially implements mavens version format: <a href="https://maven.apache.org/ref/3.5.2/maven-artifact/apidocs/org/apache/maven/artifact/versioning/ComparableVersion.html">ComparableVersion</a>
 * <p>
 * The format is quite complex, so this is not a fully compliant implementation.
 */
public class MavenVersion implements Version<MavenVersion> {
    private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9_\\-.]+");
    private static final Pattern STRING_PARSER = Pattern.compile("^(\\d*)?(\\D+)(\\d*)?$");

    private final String stringRepresentation;
    private final VersionSegment[] versionSegments;

    public MavenVersion(String stringRepresentation, VersionSegment[] versionSegments) {
        this.stringRepresentation = stringRepresentation;
        this.versionSegments = versionSegments;
    }

    @Override
    public int compareTo(@NotNull MavenVersion other) {
        int lowestAmount = Math.min(this.versionSegments.length, other.versionSegments.length);
        for (int i = 0; i < lowestAmount; i++) {
            int cmp = this.versionSegments[i].compareTo(other.versionSegments[i]);
            if (cmp != 0) {
                return cmp;
            }
        }

        if (this.versionSegments.length > lowestAmount) {
            for (int i = lowestAmount; i < this.versionSegments.length; i++) {
                if (this.versionSegments[i].isGreater(VersionSegment.NumberVersionSegment.ZERO)) return 1;
            }
        } else if (other.versionSegments.length > lowestAmount) {
            for (int i = lowestAmount; i < other.versionSegments.length; i++) {
                if (other.versionSegments[i].isGreater(VersionSegment.NumberVersionSegment.ZERO)) return -1;
            }
        }

        return 0;
    }

    public static Optional<MavenVersion> parse(String version) {
        if (version == null || version.isEmpty() || !ALPHANUMERIC.matcher(version).matches()) return Optional.empty();

        List<VersionSegment> segments = new ArrayList<>();

        String noHyphens = version.replace("-", ".");
        String[] splitByDot = noHyphens.split("\\.");
        for (String segment : splitByDot) {
            Matcher matcher = STRING_PARSER.matcher(segment);
            if (matcher.matches()) {
                String firstNumber = matcher.group(1);
                String string = matcher.group(2);
                String secondNumber = matcher.group(3);

                if (firstNumber != null && !firstNumber.isEmpty()) {
                    try {
                        segments.add(new VersionSegment.NumberVersionSegment(Integer.parseUnsignedInt(firstNumber)));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (string != null && !string.isEmpty()) {
                    VersionSegment.QualifierVersionSegment.Qualifier qualifier = VersionSegment.QualifierVersionSegment.Qualifier.getByName(string);
                    if (qualifier == null) {
                        segments.add(new VersionSegment.StringVersionSegment(string));
                    } else {
                        segments.add(new VersionSegment.QualifierVersionSegment(qualifier));
                    }
                }
                if (secondNumber != null && !secondNumber.isEmpty()) {
                    try {
                        segments.add(new VersionSegment.NumberVersionSegment(Integer.parseUnsignedInt(secondNumber)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else {
                try {
                    segments.add(new VersionSegment.NumberVersionSegment(Integer.parseUnsignedInt(segment)));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return Optional.of(new MavenVersion(version, segments.toArray(new VersionSegment[0])));
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MavenVersion that = (MavenVersion) o;
        return Objects.deepEquals(versionSegments, that.versionSegments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(versionSegments);
    }

    @Override
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }

    public interface VersionSegment extends Comparable<VersionSegment> {
        default boolean isEqual(@NotNull VersionSegment other) {
            return this.compareTo(other) == 0;
        }

        default boolean isGreater(@NotNull VersionSegment other) {
            return this.compareTo(other) > 0;
        }

        default boolean isLesser(@NotNull VersionSegment other) {
            return this.compareTo(other) < 0;
        }

        class NumberVersionSegment implements VersionSegment {
            public static final NumberVersionSegment ZERO = new NumberVersionSegment(0);

            private final int number;

            public NumberVersionSegment(int number) {
                this.number = number;
            }

            public int getNumber() {
                return number;
            }

            @Override
            public String toString() {
                return number + "";
            }

            @Override
            public int compareTo(@NotNull VersionSegment other) {
                if (other instanceof NumberVersionSegment) {
                    return Integer.compare(this.number, ((NumberVersionSegment) other).number);
                }

                return 1;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof VersionSegment)) return false;
                return compareTo((VersionSegment) o) == 0;
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(number);
            }
        }

        class QualifierVersionSegment implements VersionSegment {
            private final Qualifier qualifier;

            public QualifierVersionSegment(Qualifier qualifier) {
                this.qualifier = qualifier;
            }

            public Qualifier getQualifier() {
                return qualifier;
            }

            @Override
            public String toString() {
                return qualifier.name();
            }

            @Override
            public int compareTo(@NotNull VersionSegment other) {
                if (other instanceof NumberVersionSegment) return -1;
                if (other instanceof QualifierVersionSegment) {
                    return Integer.compare(this.qualifier.ordinal(), ((QualifierVersionSegment) other).qualifier.ordinal());
                }
                if (other instanceof StringVersionSegment) return 1;

                return -1;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof VersionSegment)) return false;
                return compareTo((VersionSegment) o) == 0;
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(qualifier);
            }

            public enum Qualifier {
                ALPHA("alpha", "a"),
                BETA("beta", "b"),
                MILESTONE("milestone", "m"),
                RELEASE_CANDIDATE("rc", "cr"),
                SNAPSHOT("snapshot"),
                FINAL("ga", "final"),
                SERVICE_PACK("sp");

                private final String[] names;

                Qualifier(String... names) {
                    this.names = names;
                }

                public String[] getNames() {
                    return names;
                }

                @Nullable
                public static Qualifier getByName(String name) {
                    for (Qualifier value : values()) {
                        for (String n : value.names) {
                            if (n.equalsIgnoreCase(name)) return value;
                        }
                    }
                    return null;
                }
            }
        }

        class StringVersionSegment implements VersionSegment {
            private final String string;

            public StringVersionSegment(String string) {
                this.string = string;
            }

            public String getString() {
                return string;
            }

            @Override
            public String toString() {
                return "'" + string + "'";
            }

            @Override
            public int compareTo(@NotNull VersionSegment other) {
                if (other instanceof StringVersionSegment) {
                    return this.string.compareToIgnoreCase(((StringVersionSegment) other).string);
                }

                return -1;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof VersionSegment)) return false;
                return compareTo((VersionSegment) o) == 0;
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(string);
            }
        }
    }
}
