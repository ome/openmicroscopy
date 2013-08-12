/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.keywords;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXTaskPane;

import org.robotframework.abbot.finder.BasicFinder;
import org.robotframework.abbot.finder.ComponentNotFoundException;
import org.robotframework.abbot.finder.Matcher;
import org.robotframework.abbot.finder.MultipleComponentsFoundException;

/**
 * Robot Framework SwingLibrary keyword library offering methods for checking icons.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class IconCheckLibrary
{
    /** Allow Robot Framework to instantiate this library only once. */
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    /**
     * <table>
     *   <td>Get Image Icon Name</td>
     *   <td>name of <code>JLabel</code> or <code>JXTaskPane</code></td>
     * </table>
     * @param containerName the name of the <code>JLabel</code> or <code>JXTaskPane</code> whose <code>ImageIcon</code>'s filename is required
     * @return the filename of the <code>ImageIcon</code>
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     */
    public String getImageIconName(final String containerName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final JComponent iconBearer = (JComponent) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return (component instanceof JLabel || component instanceof JXTaskPane)
                        && containerName.equals(component.getName());
            }});
        Icon icon = null;
        if (iconBearer instanceof JLabel) {
            icon = ((JLabel) iconBearer).getIcon();
        } else if (iconBearer instanceof JXTaskPane) {
            icon = ((JXTaskPane) iconBearer).getIcon();
        }
        if (!(icon instanceof ImageIcon)) {
            throw new RuntimeException("not an ImageIcon");
        }
        final String iconName = ((ImageIcon) icon).getDescription();
        final int lastSlash = iconName.lastIndexOf('/');
        return lastSlash < 0 ? iconName : iconName.substring(lastSlash + 1);
    }
}
