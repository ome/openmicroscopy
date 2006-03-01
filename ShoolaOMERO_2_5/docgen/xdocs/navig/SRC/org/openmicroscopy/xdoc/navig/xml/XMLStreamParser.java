/*
 * org.openmicroscopy.xdoc.navig.xml.XMLStreamParser
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

package org.openmicroscopy.xdoc.navig.xml;


//Java imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//Third-party libraries

//Application-internal dependencies

/** 
 * Event-based <i>XML</i> parser.
 * This is a tiny parser that offers minimal capabilities, almost no 
 * well-formedness check and no validation at all.
 * <p>This class is a quick hack to avoid using a full-fledged <i>SAX</i>
 * parser, which in the majority of instances would cause problems in an
 * applet &#151; from security exceptions to non-availability of specific
 * features.  However, it turns out that (luckily) this is all we need to
 * parse a 'doc.xml' file.</p> 
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
public class XMLStreamParser
{

    /** The stream to parse. */
    private BufferedReader  xmlStream;
    
    /** We notify it of parsing events. */
    private ParserObserver  observer;
    
    
    /**
     * Creates a new instance.
     * 
     * @param xmlStream The stream to parse.  Mustn't be <code>null</code>.
     * @param observer To notify of parsing events.
     */
    public XMLStreamParser(InputStream xmlStream, ParserObserver observer)
    {
        if (xmlStream == null) throw new NullPointerException("No xml stream.");
        if (observer == null) throw new NullPointerException("No observer.");
        InputStreamReader reader = new InputStreamReader(xmlStream);
        this.xmlStream = new BufferedReader(reader);
        this.observer = observer;
    }
    
    /**
     * Starts the parse of the <i>XML</i> stream.
     * 
     * @throws IOException If an <i>I/O</i> error occurs while reading from
     *                      the stream.
     */
    public void parse() 
        throws IOException
    {
        TagParser currentTag = new TagParser();
        observer.onStart();
        int c;
        while ((c = xmlStream.read()) != -1) {
            if (currentTag.parse((char) c)) {
                Element e = new Element(currentTag.getTag());
                switch (e.getType()) {
                    case Element.INLINE:
                        observer.onStartElement(e);
                        observer.onEndElement(e);
                        break;
                    case Element.START:
                        observer.onStartElement(e);
                        break;
                    case Element.END:
                        observer.onEndElement(e);
                }
            }
        }
        observer.onEnd();
    }
    //NOTE: The semantics of onEnd() is, for now, unspecified in the case of
    //      an exception (the I/F says nothing about it).  If onEnd() should
    //      be called even in the presence of an exception, then we have to
    //      put it in a *finally* clause.  However, a better option would be
    //      to add error callbacks in the ParserObserver.
    
}
