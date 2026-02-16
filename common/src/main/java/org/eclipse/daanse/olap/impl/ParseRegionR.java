/*
 * Copyright (c) 2023-2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.impl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.daanse.olap.api.query.ParseRegion;

/**
 * Region of parser source code.
 *
 * The main purpose of a ParseRegion is to give detailed locations in
 * error messages and warnings from the parsing and validation process.
 *
 * A region has a start and end line number and column number. A region is
 * a point if the start and end positions are the same.
 *
 * The line and column number are one-based, because that is what end-users
 * understand.
 *
 * A region's end-points are inclusive. For example, in the code
 *
 * SELECT FROM [Sales]
 *
 * the SELECT token has region [1:1, 1:6].
 *
 * Regions are immutable.
 */
public record ParseRegionR(int startLine, int startColumn, int endLine, int endColumn) implements ParseRegion {

    /**
     * Compact constructor with validation.
     */
    public ParseRegionR {
        assert endLine >= startLine;
        assert endLine > startLine || endColumn >= startColumn;
    }

    /**
     * Creates a point ParseRegion (same start and end).
     *
     * @param line Line of the beginning and end of the region
     * @param column Column of the beginning and end of the region
     * @return ParseRegionR representing a point
     */
    public static ParseRegionR of(int line, int column) {
        return new ParseRegionR(line, column, line, column);
    }

    // Interface methods - delegate to record accessors
    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public int getStartColumn() {
        return startColumn;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * Returns whether this region has the same start and end point.
     *
     * @return whether this region has the same start and end point
     */
    public boolean isPoint() {
        return endLine == startLine && endColumn == startColumn;
    }

    @Override
    public String toString() {
        return "[" + startLine + ":" + startColumn
            + (isPoint() ? "" : ", " + endLine + ":" + endColumn)
            + "]";
    }

    /**
     * Combines this region with other regions.
     *
     * @param nodes Source code regions
     * @return region which represents the span of the given regions
     */
    public ParseRegion plus(final ParseTreeNode... nodes) {
        return plusAll(
            new AbstractList<ParseRegion>() {
                public ParseRegion get(int index) {
                    final ParseTreeNode node = nodes[index];
                    if (node == null) {
                        return null;
                    }
                    return node.getRegion();
                }

                public int size() {
                    return nodes.length;
                }
            });
    }

    public ParseRegion plus(final List<? extends ParseTreeNode> nodes) {
        if (nodes == null) {
            return this;
        }
        return plusAll(
            new AbstractList<ParseRegion>() {
                public ParseRegion get(int index) {
                    final ParseTreeNode node = nodes.get(index);
                    if (node == null) {
                        return null;
                    }
                    return node.getRegion();
                }

                public int size() {
                    return nodes.size();
                }
            });
    }

    /**
     * Combines this region with other regions.
     *
     * @param regions Source code regions
     * @return region which represents the span of the given regions
     */
    public ParseRegion plus(ParseRegionR... regions) {
        return plusAll(Arrays.asList(regions));
    }

    /**
     * Combines this region with a list of parse tree nodes to create a
     * region which spans from the first point in the first to the last point
     * in the other.
     *
     * @param regions Collection of source code regions
     * @return region which represents the span of the given regions
     */
    public ParseRegion plusAll(Iterable<ParseRegion> regions) {
        return sum(
            regions,
            getStartLine(),
            getStartColumn(),
            getEndLine(),
            getEndColumn());
    }

    /**
     * Combines the parser positions of a list of nodes to create a position
     * which spans from the beginning of the first to the end of the last.
     *
     * @param nodes Collection of parse tree nodes
     * @return region which represents the span of the given nodes
     */
    public static ParseRegion sum(Iterable<ParseRegion> nodes) {
        return sum(nodes, Integer.MAX_VALUE, Integer.MAX_VALUE, -1, -1);
    }

    private static ParseRegion sum(
        Iterable<ParseRegion> regions,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn)
    {
        int testLine;
        int testColumn;
        for (ParseRegion region : regions) {
            if (region == null) {
                continue;
            }
            testLine = region.getStartLine();
            testColumn = region.getStartColumn();
            if ((testLine < startLine)
                || ((testLine == startLine) && (testColumn < startColumn)))
            {
                startLine = testLine;
                startColumn = testColumn;
            }

            testLine = region.getEndLine();
            testColumn = region.getEndColumn();
            if ((testLine > endLine)
                || ((testLine == endLine) && (testColumn > endColumn)))
            {
                endLine = testLine;
                endColumn = testColumn;
            }
        }
        return new ParseRegionR(startLine, startColumn, endLine, endColumn);
    }

    /**
     * Looks for one or two carets in an MDX string, and if present, converts
     * them into a parser position.
     *
     * @param code Source code
     * @return object containing source code annotated with region
     */
    public static RegionAndSourceR findPos(String code) {
        int firstCaret = code.indexOf('^');
        if (firstCaret < 0) {
            return new RegionAndSourceR(code, null);
        }
        int secondCaret = code.indexOf('^', firstCaret + 1);
        if (secondCaret < 0) {
            String codeSansCaret =
                code.substring(0, firstCaret)
                    + code.substring(firstCaret + 1);
            int[] start = indexToLineCol(code, firstCaret);
            return new RegionAndSourceR(
                codeSansCaret,
                ParseRegionR.of(start[0], start[1]));
        } else {
            String codeSansCaret =
                code.substring(0, firstCaret)
                    + code.substring(firstCaret + 1, secondCaret)
                    + code.substring(secondCaret + 1);
            int[] start = indexToLineCol(code, firstCaret);
            secondCaret--;
            secondCaret--;
            int[] end = indexToLineCol(code, secondCaret);
            return new RegionAndSourceR(
                codeSansCaret,
                new ParseRegionR(start[0], start[1], end[0], end[1]));
        }
    }

    private static int[] indexToLineCol(String code, int i) {
        int line = 0;
        int j = 0;
        while (true) {
            String s;
            int rn = code.indexOf("\r\n", j);
            int r = code.indexOf("\r", j);
            int n = code.indexOf("\n", j);
            int prevj = j;
            if ((r < 0) && (n < 0)) {
                assert rn < 0;
                s = null;
                j = -1;
            } else if ((rn >= 0) && (rn < n) && (rn <= r)) {
                s = "\r\n";
                j = rn;
            } else if ((r >= 0) && (r < n)) {
                s = "\r";
                j = r;
            } else {
                s = "\n";
                j = n;
            }
            if ((j < 0) || (j > i)) {
                return new int[]{line + 1, i - prevj + 1};
            }
            assert s != null;
            j += s.length();
            ++line;
        }
    }

    private static int lineColToIndex(String code, int line, int column) {
        --line;
        --column;
        int i = 0;
        while (line-- > 0) {
            i = code.indexOf("\n", i) + "\n".length();
        }
        return i + column;
    }

    /**
     * Generates a string of the source code annotated with caret symbols ("^")
     * at the beginning and end of the region.
     *
     * @param source Source code
     * @return Source code annotated with position
     */
    public String annotate(String source) {
        return addCarets(source, startLine, startColumn, endLine, endColumn);
    }

    private static String addCarets(
        String sql,
        int line,
        int col,
        int endLine,
        int endCol)
    {
        String sqlWithCarets;
        int cut = lineColToIndex(sql, line, col);
        sqlWithCarets = sql.substring(0, cut) + "^"
            + sql.substring(cut);
        if ((col != endCol) || (line != endLine)) {
            cut = lineColToIndex(sqlWithCarets, endLine, endCol + 1);
            ++cut;
            if (cut < sqlWithCarets.length()) {
                sqlWithCarets =
                    sqlWithCarets.substring(0, cut)
                        + "^" + sqlWithCarets.substring(cut);
            } else {
                sqlWithCarets += "^";
            }
        }
        return sqlWithCarets;
    }

    /**
     * Combination of a region within an MDX statement with the source text
     * of the whole MDX statement.
     */
    public record RegionAndSourceR(String source, ParseRegion region) {
    }
}
