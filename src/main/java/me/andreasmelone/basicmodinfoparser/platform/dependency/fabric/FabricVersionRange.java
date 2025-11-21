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

import me.andreasmelone.basicmodinfoparser.platform.dependency.version.Version;
import me.andreasmelone.basicmodinfoparser.platform.dependency.version.VersionRange;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FabricVersionRange implements VersionRange<LooseSemanticVersion> {
    private static final Pattern MODIFIER_REGEX = Pattern.compile("^(>=|<=|>|<|=|\\^|~)(.+)$", Pattern.MULTILINE);

    private final String stringRepresentation;
    private final List<List<VersionCondition>> conditions;

    public FabricVersionRange(String stringRepresentation, List<List<VersionCondition>> conditions) {
        this.stringRepresentation = stringRepresentation;
        this.conditions = conditions;
    }

    @Override
    public String getStringRepresentation() {
        return stringRepresentation;
    }

    @Override
    public boolean contains(LooseSemanticVersion version) {
        if (conditions.isEmpty()) return true;

        for (List<VersionCondition> condition : conditions) {
            if (innerContains(condition, version)) return true;
        }
        return false;
    }

    @Override
    public Class<LooseSemanticVersion> getType() {
        return LooseSemanticVersion.class;
    }

    private boolean innerContains(List<VersionCondition> conditions, LooseSemanticVersion version) {
        for (VersionCondition condition : conditions) {
            if (!condition.matches(version)) return false;
        }
        return true;
    }

    public Optional<FabricVersionRange> optional() {
        return Optional.of(this);
    }

    @Override
    public String toString() {
        return "FabricVersionRange{" +
                "stringRepresentation='" + stringRepresentation + '\'' +
                ", conditions=" + conditions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FabricVersionRange that = (FabricVersionRange) o;
        return Objects.equals(conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditions);
    }

    public static Optional<FabricVersionRange> parse(String... version) {
        List<List<VersionCondition>> allConditions = new ArrayList<>();
        for (String manyVersions : version) {
            List<VersionCondition> versionConditions = new ArrayList<>();

            for (String range : manyVersions.split(" ")) {
                Matcher matcher = MODIFIER_REGEX.matcher(range);
                List<Operator> operators = new ArrayList<>();
                String versionString = range;
                if (matcher.matches()) {
                    operators = Operator.getBySymbol(matcher.group(1));
                    versionString = matcher.group(2);

                    if (operators == null || versionString == null || versionString.isEmpty()) continue;
                }
                Optional<Version> parsedVersion = new LooseSemanticVersion().parse(versionString, true);
                if (!parsedVersion.isPresent()) continue;
                if (operators.isEmpty()) operators.add(Operator.EQUALS);

                versionConditions.add(new VersionCondition(operators, (LooseSemanticVersion) parsedVersion.get()));
            }

            allConditions.add(versionConditions);
        }
        return new FabricVersionRange(Arrays.toString(version), allConditions).optional();
    }

    public static class VersionCondition {
        private final List<Operator> operators;
        private final LooseSemanticVersion version;

        public VersionCondition(List<Operator> operators, LooseSemanticVersion version) {
            this.operators = operators;
            this.version = version;
        }

        public LooseSemanticVersion getVersion() {
            return version;
        }

        public List<Operator> getOperators() {
            return operators;
        }

        @Override
        public String toString() {
            return "VersionCondition{" +
                    "operators=" + operators +
                    ", version=" + version +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            VersionCondition that = (VersionCondition) o;
            return Objects.equals(operators, that.operators) && Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operators, version);
        }

        public boolean matches(LooseSemanticVersion version) {
            for (Operator operator : operators) {
                if (operator == Operator.GREATER && this.version.compareTo(version) < 0) {
                    return true;
                }
                if (operator == Operator.EQUALS && this.version.compareTo(version) == 0) {
                    return true;
                }
                if (operator == Operator.LESSER && this.version.compareTo(version) > 0) {
                    return true;
                }

                if (operator == Operator.CARET || (operator == Operator.TILDE && this.version.getWildcardPositions()
                        .contains(1))) {
                    boolean isAboveLower = this.version.compareTo(version) <= 0;

                    LooseSemanticVersion upperBound = this.version.increaseMajor(1);
                    boolean isBelowUpper = version.compareTo(upperBound) < 0;

                    return isAboveLower && isBelowUpper;
                }

                if (operator == Operator.TILDE) {
                    boolean isAboveLower = this.version.compareTo(version) <= 0;

                    LooseSemanticVersion upperBound = this.version.increaseMinor(1);
                    boolean isBelowUpper = version.compareTo(upperBound) < 0;

                    return isAboveLower && isBelowUpper;
                }
            }
            return false;
        }
    }

    public enum Operator {
        GREATER,
        EQUALS,
        LESSER,
        CARET,
        TILDE;

        static List<Operator> getBySymbol(String symbol) {
            List<Operator> operators = new ArrayList<>();
            switch (symbol) {
                case ">":
                    operators.add(GREATER);
                    break;
                case ">=":
                    operators.add(GREATER);
                    operators.add(EQUALS);
                    break;
                case "<":
                    operators.add(LESSER);
                    break;
                case "<=":
                    operators.add(LESSER);
                    operators.add(EQUALS);
                    break;
                case "=":
                    operators.add(EQUALS);
                    break;
                case "~":
                    operators.add(TILDE);
                    break;
                case "^":
                    operators.add(CARET);
                    break;
                default:
                    return null;
            }
            return operators;
        }
    }
}
