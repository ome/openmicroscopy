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

import org.jdesktop.swingx.JXTaskPane;

import abbot.finder.BasicFinder;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;

/**
 * Robot Framework SwingLibrary keyword library offering methods for testing component state.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class ComponentStateLibrary
{
    /** Allow Robot Framework to instantiate this library only once. */
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    /**
     * <table>
     *   <td>Is Browser Collapsed</td>
     *   <td>name of browser</td>
     * </table>
     * @param browserName the name of the <code>JXTaskPane</code> that is to be queried
     * @return if the browser is collapsed
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     */
    public boolean isBrowserCollapsed(final String browserName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final JXTaskPane taskPane = (JXTaskPane) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JXTaskPane && browserName.equals(component.getName());
            }});
        return taskPane.isCollapsed();
    }
}
