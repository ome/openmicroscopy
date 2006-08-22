/*
 * org.openmicroscopy.xdoc.navig.xml.ParserObserver
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

//Third-party libraries

//Application-internal dependencies

/** 
 * Observes a {@link XMLStreamParser} while an <i>XML</i> stream is parsed.
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
public interface ParserObserver
{
    
    /**
     * Notifies the start of the <i>XML</i> stream parse.
     * This method is invoked only once and is the first method invoked during
     * the parse. 
     */    
    public void onStart();

    /**
     * Called just after a new element has been parsed.
     * This method is called in correspondence of an element start tag.
     * 
     * @param which The start tag of the last parsed element.
     */
    public void onStartElement(Element which);
    
    /**
     * Called just after an element end tag has been parsed.
     * 
     * @param which The end tag of the last parsed element.
     */
    public void onEndElement(Element which);
    
    /**
     * Notifies the end of the <i>XML</i> stream has been reached.
     * This method is invoked only once and is the last method invoked during
     * the parse. 
     */
    public void onEnd();
    
}
