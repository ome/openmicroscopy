/*
 * org.openmicroscopy.xdoc.navig.TOCParser
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

package org.openmicroscopy.xdoc.navig;


//Java imports
import java.net.URL;
import java.util.Stack;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.xdoc.navig.xml.Element;
import org.openmicroscopy.xdoc.navig.xml.ParserObserver;
import org.openmicroscopy.xdoc.navig.xml.XMLStreamParser;

/** 
 * Parses the table of contents of a 'doc.xml' file into a tree of
 * {@link DefaultMutableTreeNode}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TOCParser
    implements ParserObserver
{

    /** The <i>Q-Name</i> of the toc element. */
    private static final String     TOC = "toc";
    
    /** The <i>Q-Name</i> of the section element. */
    private static final String     SECTION = "section";
    
    /** The <i>Q-Name</i> of the name attribute. */
    private static final String     NAME = "name";
    
    /** The <i>Q-Name</i> of the href attribute. */
    private static final String     HREF = "href";
    
    
    /** Points to the 'doc.xml' file to parse. */
    private URL     xdoc;
    
    /** 
     * Stores the section nodes in the current parsing path.
     * The bottom of the stack is the toc node, which is the root of the 
     * sections tree.  The top of the stack is the section node that is
     * currently being parsed.  The intermediate items are the nodes from
     * the root to the current section.
     */
    private Stack   sectionsPath;
    
    /**
     * The result of the parsing.
     * This is the root node of the parsed toc tree and contains the toc node
     * descriptor.  Its children correspond to the section nodes under the
     * toc node and each contains a section descriptor.  All descriptors are
     * instances of {@link SectionDescriptor} and are stored as user objects.
     */
    private DefaultMutableTreeNode  parsedTree;
    
    
    /**
     * Creates a new node to add to the {@link #parsedTree}.
     * 
     * @param attrs The attributes of the xml element (either the toc or
     *              the section tag).
     * @return The new node.
     */
    private DefaultMutableTreeNode makeParsedTreeNode(Element e)
    {
        SectionDescriptor d = new SectionDescriptor(
                                        e.getAttribute(NAME),
                                        e.getAttribute(HREF));
        return new DefaultMutableTreeNode(d);
    }
    
    /**
     * Creates a new instance to parse the specified <code>xdoc</code>.
     * 
     * @param xdoc Points to the 'doc.xml' file to parse.  
     *              Mustn't be <code>null</code>.
     */
    TOCParser(URL xdoc)
    {
        if (xdoc == null) throw new NullPointerException("No xdoc.");
        this.xdoc = xdoc;
    }
    
    /**
     * Parses the table of contents of the given 'doc.xml' file.
     * The toc and section nodes are stored into a tree of
     * {@link DefaultMutableTreeNode}s.  This method returns the root node of
     * the parsed tree.  It contains the toc node descriptor.  Its children 
     * correspond to the section nodes under the toc node and each contains a
     * section descriptor.  All descriptors are instances of 
     * {@link SectionDescriptor} and are stored as user objects.
     * 
     * @return The root node of the parsed toc tree.  
     * @throws NavMenuException If an error occurs while parsing the 
     *                          'doc.xml' file.
     */
    DefaultMutableTreeNode parse()
        throws NavMenuException
    {
        sectionsPath = new Stack();
        try {
            XMLStreamParser parser = new XMLStreamParser(
                                                    xdoc.openStream(), this);
            parser.parse();
        } catch (Exception e) {
            throw new NavMenuException("Couldn't parse "+xdoc+".", e);
        } 
        return parsedTree;
    }

    /**
     * Catches start element events.
     * If the current element is the toc or a section, then make a new node
     * and push it on the stack.  If the stack is not empty, then the top of
     * the stack is this node's parent, so link them.
     * 
     * @see ParserObserver#onStartElement(Element)
     */
    public void onStartElement(Element which)
    {
        DefaultMutableTreeNode section = null;
        if (TOC.equals(which.getName())) {
            parsedTree = makeParsedTreeNode(which);
            section = parsedTree;
        } else if (SECTION.equals(which.getName())) {
            section = makeParsedTreeNode(which);
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
                                                          sectionsPath.peek();
            parent.add(section);
            //NOTE: parent can't be null b/c the toc must be in the stack.
        }
        if (section != null) sectionsPath.push(section);
    }

    /**
     * Catches end element events.
     * If the current element is the toc or a section, then we're done with
     * the current toc/section node.  So pop it from the stack.
     * 
     * @see ParserObserver#onEndElement(Element)
     */
    public void onEndElement(Element which)
    {
        if (TOC.equals(which.getName()) || SECTION.equals(which.getName()))
            sectionsPath.pop();  
    }

    /**
     * No-op implementation.
     * Required by {@link ParserObserver}, but not actually needed in our case.
     */
    public void onStart() {}
    
    /**
     * No-op implementation.
     * Required by {@link ParserObserver}, but not actually needed in our case.
     */
    public void onEnd() {}
    
}
