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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.robotframework.abbot.finder.BasicFinder;
import org.robotframework.abbot.finder.ComponentNotFoundException;
import org.robotframework.abbot.finder.Matcher;
import org.robotframework.abbot.finder.MultipleComponentsFoundException;
import org.robotframework.abbot.tester.JTreeTester;
import org.robotframework.abbot.util.AWT;
import org.robotframework.org.netbeans.jemmy.Waitable;
import org.robotframework.org.netbeans.jemmy.Waiter;
import org.robotframework.org.netbeans.jemmy.operators.JTreeOperator;
import org.robotframework.swing.common.TimeoutCopier;
import org.robotframework.swing.common.TimeoutName;
import org.robotframework.swing.tree.NodeTextExtractor;
import org.robotframework.swing.tree.TreeOperator;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * Robot Framework SwingLibrary keyword library offering methods for working with the tree view.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class JTreeLibrary
{
    /** Allow Robot Framework to instantiate this library only once. */
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    /**
     * Gets tree paths in a form suitable for SwingLibrary.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 4.4.9
     */
    private static class TreePathGetter {
        private final NodeTextExtractor treeNodeTextExtractor;

        /**
         * Construct a tree path getter for the given tree.
         * @param tree a tree
         */
        TreePathGetter(JTree tree) {
            this.treeNodeTextExtractor = new NodeTextExtractor(tree);
        }

        /**
         * Get the tree node path in a form suitable for SwingLibrary.
         * @param path the Swing tree node path
         * @return the specifier for the path
         */
        public String getTreeNodePath(TreePath path) {
            final int pathLength = path.getPathCount();
            final List<String> pathText = new ArrayList<String>(pathLength);
            for (int nodeIndex = 0; nodeIndex < pathLength; nodeIndex++) {
                final Object node = path.getPathComponent(nodeIndex);
                final String nodeText = this.treeNodeTextExtractor.getText(node, path);
                if (!Strings.isNullOrEmpty(nodeText)) {
                    pathText.add(nodeText);
                }
            }
            return Joiner.on('|').join(pathText);
        }
    }

    /**
     * Wraps the <code>Get Matching Tree Path</code> keyword for Robot Framework
     * in a Jemmy {@link org.robotframework.org.netbeans.jemmy.Waiter}.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 4.4.9
     */
    private static class GetMatchingTreePathWaiter implements Waitable {
        private final JTree tree;
        private final Pattern pattern;
        private final TreePathGetter treePathGetter;

        /**
         * Construct a new waiter with the <code>Get Matching Tree Path</code> arguments.
         * Note that the <code>JTree</code> is assumed to already be available even if a matching node is not.
         * @param tree the <code>JTree</code> instance in which to search for the tree path
         * @param pattern the <code>Pattern</code> that must match the whole tree path
         */
        GetMatchingTreePathWaiter(JTree tree, Pattern pattern) {
            this.tree = tree;
            this.pattern = pattern;
            this.treePathGetter = new TreePathGetter(tree);
        }

        /**
         * @return a matching tree path if any now exist, or <code>null</code> if none do
         */
        public String getMatchingTreePath() {
            /* check the JTree's model */
            final TreeModel genericModel = this.tree.getModel();
            if (!(genericModel instanceof DefaultTreeModel)) {
                return null;
            }
            final DefaultTreeModel model = (DefaultTreeModel) genericModel;
            /* iterate through the tree paths */
            final Set<List<Object>> treePaths = new HashSet<List<Object>>();
            treePaths.add(Collections.singletonList(model.getRoot()));
            while (!treePaths.isEmpty()) {
                /* consider the next tree path */
                final Iterator<List<Object>> treePathIterator = treePaths.iterator();
                final List<Object> nextTreePath = treePathIterator.next();
                treePathIterator.remove();
                /* check if the tree path string matches the regular expression */
                final String treePathString = this.treePathGetter.getTreeNodePath(new TreePath(nextTreePath.toArray()));
                if (this.pattern.matcher(treePathString).matches()) {
                    return treePathString;
                }
                /* if not, then note to check the path's children */
                final Object nextNode = nextTreePath.get(nextTreePath.size() - 1);
                int nextChild = model.getChildCount(nextNode);
                while (nextChild-- > 0) {
                    final Object child = model.getChild(nextNode, nextChild);
                    final List<Object> childTreePath = new ArrayList<Object>(nextTreePath.size() + 1);
                    childTreePath.addAll(nextTreePath);
                    childTreePath.add(child);
                    treePaths.add(childTreePath);
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public Object actionProduced(Object ignored) {
            return getMatchingTreePath();
        }

        /**
         * {@inheritDoc}
         */
        public String getDescription() {
            return "find a path in the tree named " + this.tree.getName() + " matching " + this.pattern.pattern();
        }
    }

    /**
     * <table>
     *   <td>Get Matching Tree Path</td>
     *   <td>regular expression</td>
     *   <td><code>JTree</code> component name</td>
     * <table>
     * @param regExp a regular expression against which to match whole paths, cf. {@link java.util.regex.Pattern}
     * @param treeName the name of the tree whose paths are to be searched
     * @return a tree path from the tree that matches the regular expression
     * @throws InterruptedException if the matcher was interrupted while waiting for a suitable tree path
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     */
    public String getMatchingTreePath(final String regExp, final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException, InterruptedException {
        final JTree tree = (JTree) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JTree && treeName.equals(component.getName());
            }});
        final Waiter waiter = new Waiter(new GetMatchingTreePathWaiter(tree, Pattern.compile(regExp)));
        waiter.setTimeouts(new TimeoutCopier(new JTreeOperator(tree),
                TimeoutName.J_TREE_OPERATOR_WAIT_NODE_VISIBLE_TIMEOUT).getTimeouts());
        return (String) waiter.waitAction(null);
    }

    /**
     * <table>
     *   <td>Get Selected Tree Path</td>
     *   <td><code>JTree</code> component name</td>
     * <table>
     * @param treeName the name of the tree whose node selection is queried
     * @return a tree path from the tree that is the first selected node, or <code>null</code> if none are selected
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     */
    public String getSelectedTreePath(final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final JTree tree = (JTree) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JTree && treeName.equals(component.getName());
            }});
        final TreePath path = tree.getSelectionPath();
        return path == null ? null : new TreePathGetter(tree).getTreeNodePath(path);
    }

    /**
     * <table>
     *   <td>Get Tree Path With Image Icon</td>
     *   <td>the name of the sought path's icon</td>
     *   <td><code>JTree</code> component name</td>
     * <table>
     * @param iconName the name of the icon sought among the tree nodes
     * @param treeName the name of the tree among whose nodes to search
     * @return a tree path from the tree whose node bears the given icon
     * @throws MultipleComponentsFoundException if multiple suitable components have the given name
     * @throws ComponentNotFoundException if no suitable components have the given name
     */
    public String getTreePathWithImageIcon(final String iconName, final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final Set<TreePath> wrongIconPaths = new HashSet<TreePath>();
        final Set<TreePath> pathsToExpand = new HashSet<TreePath>();
        final JTree tree = (JTree) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JTree && treeName.equals(component.getName());
            }});
        final TreeModel model = tree.getModel();
        final TreeCellRenderer renderer = tree.getCellRenderer();
        while (true) {
            final int rowCount = tree.getRowCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                final TreePath path = tree.getPathForRow(rowIndex);
                if (path == null || wrongIconPaths.contains(path)) {
                    continue;
                }
                final Object node = path.getLastPathComponent();
                final Component rowComponent =
                        renderer.getTreeCellRendererComponent(tree, node, false, false, false, rowIndex, false);
                if (iconName.equals(IconCheckLibrary.getIconNameMaybe(rowComponent))) {
                    tree.setSelectionRow(rowIndex);
                    return new TreePathGetter(tree).getTreeNodePath(path);
                }
                if (!(pathsToExpand.contains(path) || model.isLeaf(path.getLastPathComponent()) || tree.isExpanded(rowIndex))) {
                    pathsToExpand.add(path);
                }
                wrongIconPaths.add(path);
            }
            final Iterator<TreePath> pathIterator = pathsToExpand.iterator();
            if (pathIterator.hasNext()) {
                tree.expandPath(pathIterator.next());
                pathIterator.remove();
                try {
                    /* TODO: perhaps some specific component state can be awaited */
                    Thread.sleep(2000);
                } catch (InterruptedException e) { }
            } else {
                throw new RuntimeException("no tree paths with ImageIcon " + iconName);
            }
        }
    }

    /**
     * Test if the tree node popup menu item is enabled.
     * (For motivation, see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/11326">trac #11326</a>.)
     * This initial version detects only first-level menu items, not paths to submenus.
     * @param menuItemText the text of the menu item
     * @param treePath the path to the node whose popup is to be queried
     * @param treeName the name of the tree that has the node of interest
     * @return if the specified menu item is enabled
     * @throws MultipleComponentsFoundException if multiple suitable components could be found
     * @throws ComponentNotFoundException if no suitable components could be found
     */
    private boolean treeNodeMenuItemIsEnabled(final String menuItemText, final String treePath, final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        final JTree tree = (JTree) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JTree && treeName.equals(component.getName());
            }});
        final JTreeOperator operator = new JTreeOperator(tree);
        operator.clickOnPath(new TreeOperator(operator).findPath(treePath), 1, AWT.getPopupMask());
        final JPopupMenu menu = (JPopupMenu) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JPopupMenu && ((JPopupMenu) component).getInvoker() == tree;
            }});
        final JMenuItem menuItem = (JMenuItem) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                if (!(component instanceof JMenuItem)) {
                    return false;
                }
                final JMenuItem menuItem = (JMenuItem) component;
                return menuItemText.equals(menuItem.getText()) && menuItem.getParent() == menu;
            }});
        final boolean isEnabled = menuItem.isEnabled();
        new JTreeTester().actionKeyStroke(KeyEvent.VK_ESCAPE);  /* to close the popup menu */
        return isEnabled;
    }

    /**
     * <table>
     *   <td>Tree Node Menu Item Should Be Enabled</td>
     *   <td>the text of the menu item</td>
     *   <td>the path to the tree node</td>
     *   <td><code>JTree</code> component name</td>
     * </table>
     * @param menuItemText the text of the menu item
     * @param treePath the path to the node whose popup is to be queried
     * @param treeName the name of the tree that has the node of interest
     * @throws MultipleComponentsFoundException if multiple suitable components could be found
     * @throws ComponentNotFoundException if no suitable components could be found
     */
    public void treeNodeMenuItemShouldBeEnabled(final String menuItemText, final String treePath, final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        if (!treeNodeMenuItemIsEnabled(menuItemText, treePath, treeName)) {
            throw new RuntimeException("Menu item '" + menuItemText + "' was disabled");
        }
    }

    /**
     * <table>
     *   <td>Tree Node Menu Item Should Be Disabled</td>
     *   <td>the text of the menu item</td>
     *   <td>the path to the tree node</td>
     *   <td><code>JTree</code> component name</td>
     * </table>
     * @param menuItemText the text of the menu item
     * @param treePath the path to the node whose popup is to be queried
     * @param treeName the name of the tree that has the node of interest
     * @throws MultipleComponentsFoundException if multiple suitable components could be found
     * @throws ComponentNotFoundException if no suitable components could be found
     */
    public void treeNodeMenuItemShouldBeDisabled(final String menuItemText, final String treePath, final String treeName)
            throws ComponentNotFoundException, MultipleComponentsFoundException {
        if (treeNodeMenuItemIsEnabled(menuItemText, treePath, treeName)) {
            throw new RuntimeException("Menu item '" + menuItemText + "' was enabled");
        }
    }
}
