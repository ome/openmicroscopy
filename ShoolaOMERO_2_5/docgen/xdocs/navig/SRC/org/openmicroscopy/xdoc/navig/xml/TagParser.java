/*
 * org.openmicroscopy.xdoc.navig.xml.TagParser
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
 * Extracts a tag from a sequence of characters.
 * Any string starting with '&lt;' and ending with '&gt;' is considered a
 * valid tag.  However, no <i>XML</i> well-formedness rules are enforced;
 * in particular, the <code>&lt;&gt;</code> tag is considered valid.
 * This class implements a simple state machine which reacts to a single call
 * event: the invocation of the {@link #parse(char) parse} method.  Depending
 * on the machine's state, the passed character is either rejected (not part
 * of a tag) or appended to the current tag.  
 * When the {@link #parse(char) parse} method returns <code>true</code>, the
 * machine has finished parsing the current tag, which can be retrieved by
 * calling the {@link #getTag() getTag} method.
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
class TagParser
{
    
    /** 
     * State flag to denote that the machine is waiting for the a tag start
     * character, '&lt;'.
     */
    private static final int  WAITING = 0;
    
    /** 
     * State flag to denote that the machine is busy parsing a tag.
     */
    private static final int  PARSING_TAG = 1;
    
    
    /** Keeps track of the machine's state. */
    private int             state;
    
    /** Stores the characters of the currently parsed tag. */
    private StringBuffer    tagBuffer;
    
    
    /**
     * Creates a new instance.
     */
    TagParser() 
    {
        tagBuffer = new StringBuffer();
        state = WAITING;
    }
    
    /**
     * Parses the specified character.
     * 
     * @param c The character to parse.
     * @return <code>true</code> if a tag is ready for collection, 
     *          <code>false</code> otherwise.
     * @see #getTag()
     */
    boolean parse(char c)
    {
        boolean done = false;
        switch (state) {
            case WAITING:
                if (c == '<') {
                    tagBuffer = new StringBuffer();
                    tagBuffer.append(c);
                    state = PARSING_TAG;
                }
                break;
            case PARSING_TAG:
                tagBuffer.append(c);
                if (c == '>') {
                    done = true;
                    state = WAITING;
                }
        }
        return done;
    }
    
    /**
     * Returns the currently parsed tag.
     * This method is only guaranteed to return a valid tag (that is, a string
     * starting with '&lt;' and ending with '&gt;') if invoked after the
     * {@link #parse(char) parse} method has returned <code>true</code> and
     * before said method is invoked again.
     * 
     * @return The currently parsed tag.
     */
    String getTag()
    {
        return tagBuffer.toString();
    }
    
}
