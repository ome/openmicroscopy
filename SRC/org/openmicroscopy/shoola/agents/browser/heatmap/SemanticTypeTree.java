/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.SemanticTypeTree
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.heatmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.ds.DataException;
import org.openmicroscopy.ds.dto.SemanticElement;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.datamodel.DataElementType;

/**
 * Creates a hierarchy of semantic types.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class SemanticTypeTree
{
    private TreeNode root;
    private Set children;
    
    /**
     * Creates an empty semantic type tree.
     *
     */
    public SemanticTypeTree()
    {
        root = new TreeNode("Empty");
        children = new HashSet();
    }
    
    public SemanticTypeTree(String rootName)
    {
        root = new TreeNode(rootName);
        children = new HashSet();
    }
    
    /**
     * Builds a tree (hopefully there are no cycles within STs) out of
     * the list of given semantic types.  Populates with DFS.
     * @param types
     */
    public SemanticTypeTree(String rootName, SemanticType[] types)
    {
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserAgent agent = env.getBrowserAgent();
        
        root = new TreeNode(rootName);
        List dfsQueue = new ArrayList();
        
        if(types == null || types.length == 0)
        {
            return;
        }
        else
        {
            for(int i=0;i<types.length;i++)
            {
                SemanticType type = types[i];
                TypeNode node = new TypeNode(type);
                root.addChild(node);
                node.setParent(root);
                dfsQueue.add(node);
            }
        }
        while(dfsQueue.size() > 0)
        {
            TypeNode node = (TypeNode)dfsQueue.get(0);
            dfsQueue.remove(0);
            SemanticType st = node.getType();
            List elements = null;
            
            try
            {
                elements = st.getElements();
            }
            catch(DataException dex)
            {
                st = agent.loadTypeInformation(st.getName());
                node.setType(st);
                elements = st.getElements();
            }
            
            // right now, don't hit the DB to go deeper
            for(Iterator iter = elements.iterator(); iter.hasNext();)
            {
                SemanticElement element = (SemanticElement)iter.next();
                String type = element.getDataColumn().getSQLType();
                DataElementType det = DataElementType.forName(type);
                String fqName = "";
                String dbName = "";
                if(node.getFQName() != null)
                {
                    fqName = fqName + node.getFQName() + ".";
                }
                if(node.getDBName() != null)
                {
                    dbName = dbName + node.getDBName() + ".";
                }
                fqName = fqName + element.getName();
                dbName = dbName + element.getDataColumn().getColumnName();
                if(det == DataElementType.ATTRIBUTE)
                {
                    SemanticType childType = element.getDataColumn().getReferenceType();
                    TypeNode typeNode = new TypeNode(element.getName(),childType);
                    typeNode.setFQName(fqName);
                    node.addChild(typeNode);
                    typeNode.setParent(node);
                    if(node.depth() < 1) // TODO: fix deeper behavior later; 1 should be OK now
                    {
                        dfsQueue.add(typeNode);
                    }
                }
                else
                {
                    ElementNode elNode = new ElementNode(element.getName(),det);
                    elNode.setFQName(fqName);
                    elNode.setParent(node);
                    node.addChild(elNode);
                }
            }
        }
    }
    
    /**
     * Returns the root of the tree.
     * @return See above.
     */
    public TreeNode getRootNode()
    {
        return root;
    }
    
    /**
     * A node in the SemanticType tree encapsulating an instance of a
     * SemanticType.
     */
    public static class TypeNode extends TreeNode
    {
        private SemanticType content;
        private Set children;
        
        public TypeNode()
        {
            super("null");
            children = new HashSet();
        }
        
        public TypeNode(SemanticType type)
        {
            super(type.getName());
            this.content = type;
            children = new HashSet();
        }
        
        public TypeNode(String name, SemanticType type)
        {
            super(name + " [" + type.getName() + "]");
            this.content = type;
            children = new HashSet();
        }
        
        public SemanticType getType()
        {
            return content;
        }
        
        public void setType(SemanticType type)
        {
            if(type != null)
            {
                this.content = type;
            }
        }
    }
    
    /**
     * A node that has a primitive value attached to it, and thus has no
     * children.  Attribute children of other Attributes should also be
     * TypeNodes.
     */
    public static class ElementNode extends TreeNode
    {
        private DataElementType type;
        
        public ElementNode(String name, DataElementType type)
        {
            super(name);
            this.type = type;
        }
        
        public DataElementType getType()
        {
            return type;
        }
    }
    
    /**
     * A node within this tree.
     */
    static class TreeNode
    {
        protected TreeNode parent;
        protected Set children;
        protected String name;
        protected String fqName; // fully qualified name
        protected String dbName; // name to retrieve in DB (could be diff.)
        protected boolean lazilyInitialized = false;
        
        public TreeNode(String nodeName)
        {
            this.name = nodeName;
            children = new HashSet();
        }
        
        public void addChild(TreeNode node)
        {
            if(node != null)
            {
                children.add(node);
            }
        }
        
        public Set getChildren()
        {
            return Collections.unmodifiableSet(children);
        }
        
        public String getName()
        {
            return name;
        }
        
        /**
         * Gets the fully qualified (criteria name path) of this node.
         * @return See above.
         */
        public String getDBName()
        {
            return dbName;
        }
        
        /**
         * Sets the fully qualified (criteria name path) of this node to
         * the specified.
         * @param dbName The name to specify.
         */
        public void setDBName(String dbName)
        {
            this.dbName = dbName;
        }
        
        /**
         * Gets the fully qualified (element name path) name of this
         * node.
         * @return The fully qualified name of this node.
         */
        public String getFQName()
        {
            return fqName;
        }
        
        /**
         * Sets the fully qualified (element name path) name of this
         * node.
         * @param fqName The fully qualified name of this node.
         */
        public void setFQName(String fqName)
        {
            this.fqName = fqName;
        }
        
        public void removeChild(TreeNode node)
        {
            if(node != null && children.contains(node))
            {
                children.remove(node);
            }
        }
        
        public TreeNode getParent()
        {
            return parent;
        }
        
        public int depth()
        {
            TreeNode node = parent;
            int depth = 0;
            while(node != null)
            {
                node = node.getParent();
                depth++;
            }
            return depth;
        }
        
        public void setParent(TreeNode parent)
        {
            if(this == parent)
            {
                System.err.println("[TreeNode]: Don't try to make a cycle...");
                return;
            }
            this.parent = parent;
        }
        
        // makes display in JTree easier.
        public String toString()
        {
            return name;
        }
        
        /**
         * Indicates whether or not this node has its info filled in (that is,
         * whether or not the thumbnails in a model have the values of this
         * ST filled in)
         * 
         * @return See above.
         */
        public boolean isLazilyInitialized()
        {
            return lazilyInitialized;
        }
        
        /**
         * Sets whether or not this ndoe has its info filled in (that is,
         * whether or not the thumbnails in a model have the values of this
         * ST filled in)
         * @param initialized
         */
        public void markAsInitialized(boolean initialized)
        {
            lazilyInitialized = initialized;
        }
    }
}
