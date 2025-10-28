package me.andreasmelone.basicmodinfoparser.dependency.version;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticVersion implements Version<SemanticVersion> {
    private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9_\\-.+]+");
    private static final Pattern REGEX = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$", Pattern.MULTILINE);

    private final int[] versionParts;
    private final String preReleaseSuffix;
    private final Integer preReleaseNumber;
    private final String buildMetadata;

    private SemanticVersion(int[] versionParts, String preReleaseSuffix, Integer preReleaseNumber, String buildMetadata) {
        this.versionParts = versionParts;
        this.preReleaseSuffix = preReleaseSuffix;
        this.preReleaseNumber = preReleaseNumber;
        this.buildMetadata = buildMetadata;
    }

    public int[] getVersionParts() {
        return versionParts.clone();
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

    @Override
    public int compareTo(@NotNull SemanticVersion other) {
        int suffixless = partComparison(other);

        if(isNull(preReleaseSuffix) && isNull(other.preReleaseSuffix)) {
            return suffixless;
        } else if(suffixless == 0) {
            if (isNull(preReleaseSuffix)) return 1;
            if (isNull(other.preReleaseSuffix)) return -1;

            int suffix = suffixComparison(other);

            if (this.preReleaseNumber == null && other.preReleaseNumber != null) return -1;
            if (this.preReleaseNumber != null && other.preReleaseNumber == null) return 1;
            if(this.preReleaseNumber != null && other.preReleaseNumber != null && suffix == 0)
                return Integer.compare(this.preReleaseNumber, other.preReleaseNumber);
            return suffix;
        }

        return suffixless;
    }

    private int partComparison(SemanticVersion other) {
        if (versionParts[0] > other.versionParts[0]) return 1;
        if (versionParts[0] < other.versionParts[0]) return -1;

        if (versionParts[1] > other.versionParts[1]) return 1;
        if (versionParts[1] < other.versionParts[1]) return -1;

        return Integer.compare(versionParts[2], other.versionParts[2]);
    }

    private int suffixComparison(SemanticVersion other) {
        int partComparison = partComparison(other);
        if(partComparison != 0) return partComparison;

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
        if(isNull(string)) return false;
        for(char c : string.toCharArray()) {
            if(!Character.isDigit(c)) return false;
        }
        return true;
    }

    private boolean isNull(String str) {
        return str == null || str.isEmpty();
    }

    @Override
    public String toString() {
        return "SemanticVersion{" +
                "versionParts=" + Arrays.toString(versionParts) +
                ", preReleaseSuffix='" + preReleaseSuffix + '\'' +
                ", preReleaseNumber=" + preReleaseNumber +
                ", buildMetadata='" + buildMetadata + '\'' +
                '}';
    }

    public static Optional<SemanticVersion> parse(String ver) {
        if(ver == null || ver.isEmpty() || !ALPHANUMERIC.matcher(ver).matches()) return Optional.empty();

//        String[] splitForAlpha = ver.split("-");
//        String[] splitForMetadata;
//
//        if(splitForAlpha.length <= 1) {
//            splitForMetadata = ver.split("\\+", 2);
//        } else {
//            splitForMetadata = splitForAlpha[1].split("\\+", 2);
//        }
//
//        String withoutSuffixes;
//
//        if(splitForAlpha.length > 1) {
//            withoutSuffixes = splitForAlpha[0];
//        } else if(splitForMetadata.length > 1) {
//            withoutSuffixes = splitForMetadata[0];
//        } else {
//            withoutSuffixes = ver;
//        }
//
//        String[] versionNumbers = withoutSuffixes.split("\\.");
//        if(versionNumbers.length < 1) return Optional.empty();
//        int[] versionInts = new int[] { 0, 0, 0 };
//        for (int i = 0; i < 2; i++) {
//            try {
//                versionInts[i] = Integer.parseInt(versionNumbers[i]);
//            } catch(NumberFormatException e) {
//                return Optional.empty();
//            }
//        }
//
//        String prereleaseSuffix = null;
//        if(splitForMetadata.length == 2) {
//            prereleaseSuffix = splitForMetadata[0];
//        } else if(splitForAlpha.length == 2) {
//            prereleaseSuffix = splitForAlpha[1];
//        }
//
//        String[] splitForPrereleaseNumber = new String[] { null };
//        Integer prereleaseNumber = null;
//        if(prereleaseSuffix != null) {
//            splitForPrereleaseNumber = prereleaseSuffix.split("\\.", 2);
//            if (splitForAlpha.length > 1 && splitForPrereleaseNumber.length > 1) {
//                try {
//                    prereleaseNumber = Integer.parseInt(splitForPrereleaseNumber[1]);
//                } catch (NumberFormatException ignored) {
//                }
//            }
//        }
//
//        String metadata = splitForMetadata.length > 1 ? splitForMetadata[1] : null;
//        return Optional.of(new SemanticVersion(
//                versionInts, splitForPrereleaseNumber[0], prereleaseNumber, metadata
//        ));
        Matcher matcher = REGEX.matcher(ver);
        if (!matcher.matches()) return Optional.empty();
        String major = matcher.group(1);
        String minor = matcher.group(2);
        String patch = matcher.group(3);
        String prerelease = matcher.group(4);
        String metadata = matcher.group(5);

        int majorInt;
        int minorInt;
        int patchInt;

        try {
            majorInt = Integer.parseInt(major);
            minorInt = Integer.parseInt(minor);
            patchInt = Integer.parseInt(patch);
        } catch (NumberFormatException ignored) {
            return Optional.empty(); // should never happen but still
        }

        Integer prereleaseNumber = null;
        if(prerelease != null) {
            String[] prereleaseSplit = prerelease.split("\\.", 2);
            if (prereleaseSplit.length > 1) {
                try {
                    prereleaseNumber = Integer.parseInt(prereleaseSplit[1]);
                    prerelease = prereleaseSplit[0];
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return new SemanticVersion(
                new int[] { majorInt, minorInt, patchInt },
                prerelease, prereleaseNumber,
                metadata
        ).optional();
    }

}
