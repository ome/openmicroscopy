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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTaskPane;

import abbot.finder.BasicFinder;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;
import org.robotframework.swing.tree.TreeOperator;
import org.robotframework.swing.tree.TreeSupport;

import com.google.common.collect.ImmutableSet;

/**
 * Robot Framework SwingLibrary keyword library offering methods for checking icons.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class IconCheckLibrary
{
    /** Allow Robot Framework to instantiate this library only once. */
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    /* classes with a getIcon() */
    private static final Collection<Class<? extends JComponent>> ICON_BEARER_CLASSES =
            ImmutableSet.of(AbstractButton.class, JLabel.class, JXTaskPane.class);

    /* tree operator factory */
    private static final TreeSupport TREE_SUPPORT = new TreeSupport();

    /**
     * Get the icon name from the given image icon.
     * @param icon the image icon
     * @return the name of the icon
     */
    private static String getIconName(ImageIcon icon) {
        final String iconName = ((ImageIcon) icon).getDescription();
        final int lastSlash = iconName.lastIndexOf('/');
        return lastSlash < 0 ? iconName : iconName.substring(lastSlash + 1);
    }

    /**
     * Get the icon name from the component's icon property's <code>ImageIcon</code> instance value.
     * @param iconBearer the component that has an icon
     * @return the name of the icon
     * @throws NoSuchMethodException if a <code>getIcon()</code> method is not available for calling
     * @throws InvocationTargetException if the <code>getIcon()</code> method threw an exception
     * @throws IllegalAccessException if a <code>getIcon()</code> method is not available for calling
     * @throws SecurityException if a <code>getIcon()</code> method is not available for calling
     * @throws IllegalArgumentException if a <code>getIcon()</code> method is not available for calling
     */
    private static String getIconName(Component iconBearer)
            throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        final Object icon = iconBearer.getClass().getMethod("getIcon").invoke(iconBearer);
        if (!(icon instanceof ImageIcon)) {
            throw new RuntimeException("not an ImageIcon");
        }
        return getIconName((ImageIcon) icon);
    }

    /**
     * Get the icon name from the component's icon property's <code>ImageIcon</code> instance value.
     * @param component iconBearer the component that has an icon
     * @return the name of the icon, or <code>null</code> if it cannot be determined
     */
    static String getIconNameMaybe(Component component) {
        final Class<? extends Component> componentClass = component.getClass();
        for (final Class<? extends JComponent> iconBearerClass : ICON_BEARER_CLASSES) {
            if (iconBearerClass.isAssignableFrom(componentClass)) {
                try {
                    return getIconName(component);
                } catch (Exception e) { }
            }
        }
        return null;
    }

    /**
     * <table>
     *   <td>Get Image Icon Name</td>
     *   <td>name of icon-bearing component</td>
     * </table>
     * @param containerName the name of the component whose <code>ImageIcon</code>'s filename is required
     * @return the filename of the <code>ImageIcon</code>
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     * @throws NoSuchMethodException if a <code>getIcon()</code> method is not available for calling
     * @throws InvocationTargetException if the <code>getIcon()</code> method threw an exception
     * @throws IllegalAccessException if a <code>getIcon()</code> method is not available for calling
     * @throws SecurityException if a <code>getIcon()</code> method is not available for calling
     * @throws IllegalArgumentException if a <code>getIcon()</code> method is not available for calling
     */
    public String getImageIconName(final String containerName)
            throws ComponentNotFoundException, MultipleComponentsFoundException,
            IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final JComponent iconBearer = (JComponent) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                if (!containerName.equals(component.getName())) {
                    return false;
                }
                final Class<? extends Component> componentClass = component.getClass();
                for (final Class<? extends JComponent> iconBearerClass : ICON_BEARER_CLASSES) {
                    if (iconBearerClass.isAssignableFrom(componentClass)) {
                        return true;
                    }
                }
                return false;
            }});
        return getIconName(iconBearer);
    }

    /**
     * <table>
     *   <td>Get Tree Node Image Icon Name</td>
     *   <td>path to icon-bearing tree node</td>
     *   <td>name of tree</td>
     * </table>
     * @param treeName the name of the tree whose node's <code>ImageIcon</code>'s filename is required
     * @return the filename of the <code>ImageIcon</code>
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     * @throws NoSuchMethodException if a <code>getIcon()</code> method is not available for calling
     * @throws InvocationTargetException if the <code>getIcon()</code> method threw an exception
     * @throws IllegalAccessException if a <code>getIcon()</code> method is not available for calling
     * @throws SecurityException if a <code>getIcon()</code> method is not available for calling
     * @throws IllegalArgumentException if a <code>getIcon()</code> method is not available for calling
     */
    public String getTreeNodeImageIconName(final String nodePath, final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException,
            IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final JTree tree = (JTree) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JTree && treeName.equals(component.getName());
            }});
        final TreeOperator treeOperator = TREE_SUPPORT.treeOperator(treeName);
        final TreePath path = treeOperator.findPath(nodePath);
        final TreeCellRenderer renderer = treeOperator.getTreeOperator().getCellRenderer();
        return getIconName(renderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(),
                false, false, false, tree.getRowForPath(path), false));
    }

    /**
     * <table>
     *   <td>Get Insight Image Icon Name</td>
     *   <td>name of the Insight agent</td>
     *   <td>name of the icon index</td>
     * </table>
     * @param agent the agent as named by the Java package
     * @param constantName the icon index in that agent's <code>IconManager</code>
     * @return the filename of the <code>ImageIcon</code>
     * @throws ClassNotFoundException if the given agent does not have an <code>IconManager</code>
     * @throws IllegalArgumentException if the <code>IconManager</code> code is not as expected
     * @throws SecurityException if the <code>IconManager</code> code is not as expected
     * @throws IllegalAccessException if the <code>IconManager</code> code is not as expected
     * @throws InvocationTargetException if the <code>IconManager</code> code is not as expected
     * @throws NoSuchMethodException if the <code>IconManager</code> code is not as expected
     * @throws NoSuchFieldException if the given icon index is not available from the <code>IconManager</code>
     */
    public String getInsightImageIconName(String agent, String constantName)
            throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException,
                InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        Class<?> iconManagerClass = Class.forName(StaticFieldLibrary.PREFIX + "agents." + agent + ".IconManager");
        final int iconIndex = (Integer) iconManagerClass.getField(constantName).get(null);
        final Object iconManager = iconManagerClass.getMethod("getInstance").invoke(null);
        Method getImageIconMethod = null;
        do {
            try {
                getImageIconMethod = iconManagerClass.getMethod("getImageIcon", int.class);
            } catch (NoSuchMethodException e) {
                iconManagerClass = iconManagerClass.getSuperclass();
            }
        } while (getImageIconMethod == null && iconManagerClass != null);
        return getIconName((ImageIcon) getImageIconMethod.invoke(iconManager, iconIndex));
    }
}
