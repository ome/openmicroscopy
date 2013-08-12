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
     * <table>
     *   <td>Get Matching Tree Path</td>
     *   <td>regular expression</td>
     *   <td><code>JTree</code> component name</td>
     * <table>
     * @param regExp a regular expression that the whole tree path must match
     * @param treeName the component name of the <code>JTree</code> instance to search
     * @return a tree path from the tree that matches the regular expression, or <code>null</code> if none exist
     * @throws MultipleComponentsFoundException if multiple trees have the given tree name
     * @throws ComponentNotFoundException if no trees have the given tree name
     */
    public String getMatchingTreePath(final String regExp, final String treeName)
        throws ComponentNotFoundException, MultipleComponentsFoundException {
        final Pattern pattern = Pattern.compile(regExp);
        /* locate the JTree and its model */
        final JTree tree = (JTree) new BasicFinder().find(new Matcher() {
            public boolean matches(Component component) {
                return component instanceof JTree && treeName.equals(component.getName());
            }});
        final TreeModel genericModel = tree.getModel();
        if (!(genericModel instanceof DefaultTreeModel)) {
            throw new RuntimeException("tree must use the default tree model");
        }
        final DefaultTreeModel model = (DefaultTreeModel) genericModel;
        /* iterate through the tree paths */
        final NodeTextExtractor treeNodeTextExtractor = new NodeTextExtractor(tree);
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
                final String nodeText = treeNodeTextExtractor.getText(lastNode, treePathInstance);
                if (!Strings.isNullOrEmpty(nodeText)) {
                    treePathText.append(nodeText);
                    treePathText.append('|');
                }
            }
            /* check if the tree path string matches the regular expression */
            if (treePathText.length() > 0) {
                treePathText.setLength(treePathText.length() - 1);
                final String treePathString = treePathText.toString();
                if (pattern.matcher(treePathString).matches()) {
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
}
