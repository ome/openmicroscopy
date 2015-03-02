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
package org.openmicroscopy.shoola.agents.metadata.util;

import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A {@link JTextPane} which can be expanded/collapsed
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ExpandableTextPane extends JTextPane {

    /** Default numbers of lines shown in collapsed state */
    public final static int DEFAULT_LINES = 3;

    /** Regex for html linebreak tags */
    private static final String LINEBREAK = "<(b|B|h|H)(r|R)/?>";

    /** Number of lines shown in collapsed state */
    private int showLines = DEFAULT_LINES;

    /** The original text */
    private String text = "";

    /** Flag if component is shown in expanded state */
    private boolean expanded = false;

    /** Flag to show 'show more/less' links */
    private boolean showMoreLink = true;

    /**
     * Creates a new instance with {@link #DEFAULT_LINES} of line number shown
     * in collapsed state
     */
    public ExpandableTextPane() {
        this(DEFAULT_LINES, true);
    }

    /**
     * Creates a new instance with {@link #DEFAULT_LINES} of line number shown
     * in collapsed state
     * 
     * @param showMoreLink
     *            Pass <code>true</code> to show a 'show more/less' link to
     *            expand/collapse the component
     */
    public ExpandableTextPane(boolean showMoreLink) {
        this(DEFAULT_LINES, showMoreLink);
    }

    /**
     * Creates a new instance with {@link #DEFAULT_LINES} of line number shown
     * in collapsed state
     * 
     * @param showLines
     *            Number of lines shown in collapsed state
     */
    public ExpandableTextPane(int showLines) {
        this(showLines, true);
    }

    /**
     * Creates a new instance which shows a specific number of lines in
     * collapsed state
     * 
     * @param showLines
     *            Number of lines shown in collapsed state
     * @param showMoreLink
     *            Pass <code>true</code> to show a 'show more/less' link to
     *            expand/collapse the component
     */
    public ExpandableTextPane(int showLines, boolean showMoreLink) {
        setContentType("text/html");
        setEditable(false);
        this.showLines = showLines;
        this.showMoreLink = showMoreLink;

        if (showMoreLink) {
            addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        setExpanded(!isExpanded());
                    }
                }
            });
        }
    }

    @Override
    /**
     * Sets the text displayed in the component.
     * For linebreak the "<br>" HTML tag has to be used; other HTML tags
     * can be used.
     */
    public void setText(String t) {
        this.text = t.replaceAll("<(H|h)(T|t)(M|m)(L|l)/?>", "");
        if (expanded || getLineCount() < showLines) {
            super.setText(getSubstring(Integer.MAX_VALUE));
        } else {
            super.setText(getSubstring(showLines));
        }
    }

    /**
     * Expands/Collapses the component
     * 
     * @param b
     *            Pass <code>true</code> to expand, <code>false</code> to
     *            collapse
     */
    public void setExpanded(boolean b) {
        this.expanded = b;
        if (expanded) {
            super.setText(getSubstring(Integer.MAX_VALUE));
        } else {
            super.setText(getSubstring(showLines));
        }
    }

    /**
     * Returns if the component is currently in expanded state
     * 
     * @return See above
     */
    public boolean isExpanded() {
        return this.expanded;
    }

    /**
     * Returns if the component can be expanded
     * 
     * @return See above
     */
    public boolean isExpandable() {
        return getLineCount() >= showLines;
    }

    /**
     * Get the total number of lines in the original text
     * 
     * @return
     */
    private int getLineCount() {
        if (text.trim().length() == 0)
            return 0;
        return text.split(LINEBREAK).length;
    }

    /**
     * Return the first lines up to nLines (adds a "..." line to the end of the
     * String returned)
     * 
     * @param nLines
     *            Maximum number of lines
     * @return See above
     */
    private String getSubstring(int nLines) {
        String[] lines = text.split(LINEBREAK);
        StringBuilder result = new StringBuilder();
        result.append("<html>");
        int i = 0;
        for (; i < nLines && i < lines.length; i++) {
            result.append(lines[i]);
            result.append("<br/>");
        }

        if (i < lines.length) {
            if (showMoreLink)
                result.append("<a href=\"/\">Show more...</a>");
            else
                result.append("...");
        } else if (showMoreLink && isExpanded()) {
            result.append("<a href=\"/\">Show less...</a>");
        }

        result.append("</html>");

        return result.toString();
    }
}
