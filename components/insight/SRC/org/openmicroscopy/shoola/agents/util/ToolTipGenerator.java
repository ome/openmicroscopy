/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util;

//Java imports

/**
 * Utility class which makes it easier to generate html tooltip text by
 * encapsulating the {@link StringBuilder} operations
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */
public class ToolTipGenerator {

    /* The buffer holding the current text */
    private StringBuilder sb;

    /**
     * Creates a new instance
     */
    public ToolTipGenerator() {
        sb = new StringBuilder();
        sb.append("<html><body>");
    }

    /**
     * Adds text without a linebreak
     * 
     * @param line
     *            The text to add
     */
    public void addLineNoBr(String line) {
        addLineNoBr(line, false);
    }

    /**
     * Adds text without a linebreak
     * 
     * @param line
     *            The text to add
     * @param bold
     *            Pass <code>true</code> to make the text bold
     */
    public void addLineNoBr(String line, boolean bold) {
        if (bold)
            sb.append("<b>");
        sb.append(line);
        if (bold)
            sb.append("</b>");
    }

    /**
     * Adds a new line
     * 
     * @param line
     *            The text to add
     */
    public void addLine(String line) {
        addLine(line, false);
    }

    /**
     * Adds a new line
     * 
     * @param line
     *            The text to add
     * @param bold
     *            Pass <code>true</code> to make the text bold
     */
    public void addLine(String line, boolean bold) {
        if (bold)
            sb.append("<b>");
        sb.append(line);
        if (bold)
            sb.append("</b>");
        sb.append("<br/>");
    }

    /**
     * Adds a new line in the form 'key: value'
     * 
     * @param key
     *            The key text
     * @param value
     *            The value text
     */
    public void addLine(String key, String value) {
        addLine(key, value, false);
    }

    /**
     * Adds a new line in the form 'key: value'
     * 
     * @param key
     *            The key text
     * @param value
     *            The value text
     * @param bold
     *            Pass <code>true</code> to make the key text bold
     */
    public void addLine(String key, String value, boolean bold) {
        if (bold)
            sb.append("<b>");
        sb.append(key + ": ");
        if (bold)
            sb.append("</b>");
        sb.append(value);
        sb.append("<br/>");
    }

    @Override
    public String toString() {
        return sb.toString() + "</body></html>";
    }
}
