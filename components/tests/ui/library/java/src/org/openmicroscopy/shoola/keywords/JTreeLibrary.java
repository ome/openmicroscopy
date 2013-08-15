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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.robotframework.abbot.finder.BasicFinder;
import org.robotframework.abbot.finder.ComponentNotFoundException;
import org.robotframework.abbot.finder.Matcher;
import org.robotframework.abbot.finder.MultipleComponentsFoundException;
import org.robotframework.org.netbeans.jemmy.Waitable;
import org.robotframework.org.netbeans.jemmy.Waiter;
import org.robotframework.org.netbeans.jemmy.operators.JTreeOperator;
import org.robotframework.swing.common.TimeoutCopier;
import org.robotframework.swing.common.TimeoutName;
import org.robotframework.swing.tree.NodeTextExtractor;

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
     * Wraps the <code>Get Matching Tree Path</code> keyword for Robot Framework
     * in a Jemmy {@link org.robotframework.org.netbeans.jemmy.Waiter}.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 4.4.9
     */
    private static class GetMatchingTreePathWaiter implements Waitable {
        private final JTree tree;
        private final NodeTextExtractor treeNodeTextExtractor;
        private final Pattern pattern;

        /**
         * Construct a new waiter with the <code>Get Matching Tree Path</code> arguments.
         * Note that the <code>JTree</code> is assumed to already be available even if a matching node is not.
         * @param tree the <code>JTree</code> instance in which to search for the tree path
         * @param pattern the <code>Pattern</code> that must match the whole tree path
         */
        GetMatchingTreePathWaiter(JTree tree, Pattern pattern) {
            this.tree = tree;
            this.treeNodeTextExtractor = new NodeTextExtractor(this.tree);
            this.pattern = pattern;
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
                final StringBuffer treePathText = new StringBuffer();
                /* work through the tree path's nodes to establish the path as a String */
                for (int nodeIndex = 1; nodeIndex <= nextTreePath.size(); nodeIndex++) {
                    final TreePath treePathInstance = new TreePath(nextTreePath.subList(0, nodeIndex).toArray());
                    final Object lastNode = treePathInstance.getLastPathComponent();
                    final String nodeText = this.treeNodeTextExtractor.getText(lastNode, treePathInstance);
                    if (!Strings.isNullOrEmpty(nodeText)) {
                        treePathText.append(nodeText);
                        treePathText.append('|');
                    }
                }
                /* check if the tree path string matches the regular expression */
                if (treePathText.length() > 0) {
                    treePathText.setLength(treePathText.length() - 1);
                    final String treePathString = treePathText.toString();
                    if (this.pattern.matcher(treePathString).matches()) {
                        return treePathString;
                    }
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
}
