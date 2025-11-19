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
package me.andreasmelone.basicmodinfoparser.platform.dependency.fabric;

import me.andreasmelone.basicmodinfoparser.platform.dependency.forge.MavenVersion;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a looser version of the SemVer 2.0, which is accepted by fabric
 */
public class LooseSemanticVersion extends Version {
    private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9_\\-.+*]+");
    private static final Pattern REGEX = Pattern.compile("^([0-9xX*]+(?:\\.[0-9xX*]+)*)(-.*?)?(\\+.+)?$", Pattern.MULTILINE);

    private int[] versionParts;
    private List<Integer> wildcardPositions;
    private String preReleaseSuffix;
    private Integer preReleaseNumber;
    private String buildMetadata;
    private boolean usesWildcards;

    public LooseSemanticVersion() { /* Empty constructor */ }

    private LooseSemanticVersion(String stringRepresentation, int[] versionParts, List<Integer> wildcardPositions, String preReleaseSuffix, Integer preReleaseNumber, String buildMetadata, boolean usesWildcards) {
        super(stringRepresentation);
        this.versionParts = versionParts;
        this.wildcardPositions = wildcardPositions;
        this.preReleaseSuffix = preReleaseSuffix;
        this.preReleaseNumber = preReleaseNumber;
        this.buildMetadata = buildMetadata;
        this.usesWildcards = usesWildcards;
    }

    public int[] getVersionParts() {
        return versionParts.clone();
    }

    public List<Integer> getWildcardPositions() {
        if (!usesWildcards) return new ArrayList<>();
        return new ArrayList<>(wildcardPositions);
    }

    public String getPreReleaseSuffix() {
        return preReleaseSuffix;
    }

    public Integer getPreReleaseNumber() {
        return preReleaseNumber;
    }

    public String getBuildMetadata() {
        return buildMetadata;
    }

    public boolean isUsesWildcards() {
        return usesWildcards;
    }

    private LooseSemanticVersion increasePart(int index, int amount) {
        int defaultLength = index + 1;
        int newLength = versionParts.length > index ? versionParts.length : defaultLength;
        int newAmount = versionParts.length > index ? versionParts[index] : amount;

        if (usesWildcards && !wildcardPositions.contains(index)) {
            newAmount += amount;
        }

        int[] newVersionParts = new int[newLength];
        System.arraycopy(versionParts, 0, newVersionParts, 0, Math.min(versionParts.length, newLength));
        newVersionParts[index] = newAmount;

        return new LooseSemanticVersion(stringRepresentation, newVersionParts, wildcardPositions, preReleaseSuffix, preReleaseNumber, buildMetadata, usesWildcards);
    }

    public LooseSemanticVersion increaseMajor(int amount) {
        return increasePart(0, amount);
    }

    public LooseSemanticVersion increaseMinor(int amount) {
        return increasePart(1, amount);
    }

    public LooseSemanticVersion increasePatch(int amount) {
        return increasePart(2, amount);
    }

    @Override
    public int compareTo(@NotNull Version other) {
        if (!(other instanceof LooseSemanticVersion)) {
            return -1;
        }

        LooseSemanticVersion castOther = (LooseSemanticVersion) other;
        int suffixless = partComparison(castOther);

        if (isNull(preReleaseSuffix) && isNull(castOther.preReleaseSuffix)) {
            return suffixless;
        } else if (suffixless == 0) {
            if (isNull(preReleaseSuffix)) return 1;
            if (isNull(castOther.preReleaseSuffix)) return -1;

            int suffix = suffixComparison(castOther);

            if (this.preReleaseNumber == null && castOther.preReleaseNumber != null) return -1;
            if (this.preReleaseNumber != null && castOther.preReleaseNumber == null) return 1;
            if (this.preReleaseNumber != null && castOther.preReleaseNumber != null && suffix == 0)
                return Integer.compare(this.preReleaseNumber, castOther.preReleaseNumber);
            return suffix;
        }

        return suffixless;
    }

    private int partComparison(LooseSemanticVersion other) {
        int maxLength = Math.max(this.versionParts.length, other.versionParts.length);

        for (int i = 0; i < maxLength; i++) {

            if ((usesWildcards && wildcardPositions.contains(i)) || other.usesWildcards && other.wildcardPositions.contains(i)) {
                if (i == this.versionParts.length - 1 || i == other.versionParts.length - 1) {
                    break;
                }
                continue;
            }

            int thisPart = i < this.versionParts.length ? this.versionParts[i] : 0;
            int otherPart = i < other.versionParts.length ? other.versionParts[i] : 0;

            if (thisPart > otherPart) return 1;
            if (thisPart < otherPart) return -1;
        }

        return 0;
    }

    private int suffixComparison(LooseSemanticVersion other) {
        int partComparison = partComparison(other);
        if (partComparison != 0) return partComparison;

        boolean isThisNumeric = isNumeric(this.preReleaseSuffix);
        boolean isOtherNumeric = isNumeric(other.preReleaseSuffix);

        if (isThisNumeric && isOtherNumeric) {
            return Integer.compare(Integer.parseInt(this.preReleaseSuffix), Integer.parseInt(other.preReleaseSuffix));
        } else if (!isThisNumeric && !isOtherNumeric) {
            return this.preReleaseSuffix.compareTo(other.preReleaseSuffix);
        }

        return isThisNumeric ? -1 : 1;
    }

    private boolean isNumeric(String string) {
        if (isNull(string)) return false;
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private boolean isNull(String str) {
        return str == null || str.isEmpty();
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LooseSemanticVersion that = (LooseSemanticVersion) o;
        return usesWildcards == that.usesWildcards && Objects.deepEquals(versionParts, that.versionParts) && Objects.equals(wildcardPositions, that.wildcardPositions) && Objects.equals(preReleaseSuffix, that.preReleaseSuffix) && Objects.equals(preReleaseNumber, that.preReleaseNumber) && Objects.equals(buildMetadata, that.buildMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(versionParts), wildcardPositions, preReleaseSuffix, preReleaseNumber, buildMetadata, usesWildcards);
    }

    @Override
    public Optional<Version> parse(String versionString) {
        return parse(versionString, false);
    }

    public Optional<Version> parse(String ver, boolean wildcards) {
        if (ver == null || ver.isEmpty() || !ALPHANUMERIC.matcher(ver).matches()) return Optional.empty();

        Matcher matcher = REGEX.matcher(ver);
        if (!matcher.matches()) return Optional.empty();
        String numbers = matcher.group(1);
        String prerelease = matcher.group(2);
        String metadata = matcher.group(3);

        if (prerelease != null && !prerelease.isEmpty()) {
            prerelease = prerelease.substring(1);
        }

        if (metadata != null && !metadata.isEmpty()) {
            metadata = metadata.substring(1);
        }

        String[] splitNumbers = numbers.split("\\.");
        int[] versionInts = new int[splitNumbers.length];
        List<Integer> wildcardPositions = new ArrayList<>();

        for (int i = 0; i < splitNumbers.length; i++) {
            String num = splitNumbers[i];
            if (num.equalsIgnoreCase("x") || num.equals("*")) {
                if (!wildcards) return Optional.empty();
                versionInts[i] = 0;
                wildcardPositions.add(i);
                continue;
            }
            try {
                versionInts[i] = Integer.parseUnsignedInt(num);
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }

        Integer prereleaseNumber = null;
        if (prerelease != null) {
            String[] prereleaseSplit = prerelease.split("\\.", 2);
            if (prereleaseSplit.length > 1) {
                try {
                    prereleaseNumber = Integer.parseInt(prereleaseSplit[1]);
                    prerelease = prereleaseSplit[0];
                } catch (NumberFormatException ignored) {
                }
            }
        }

        this.wildcardPositions = wildcardPositions;
        this.preReleaseSuffix = prerelease;
        this.preReleaseNumber = prereleaseNumber;
        this.buildMetadata = metadata;
        this.versionParts = versionInts;
        this.usesWildcards = wildcards;

        return Optional.of(this);
    }

    @Override
    public String getStringRepresentation() {
        return this.stringRepresentation;
    }
}
