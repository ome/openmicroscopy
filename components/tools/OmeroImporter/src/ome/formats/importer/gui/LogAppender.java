/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.formats.importer.gui;

import java.util.Enumeration;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Returns and manages the log appender for display
 * @author Brian W. Loranger
 *
 */
public class LogAppender
{

    private JTextPane          t;

    private StyledDocument     doc;

    private Style              style;

    private static LogAppender soleInstance;

    /**
     * Returns the appender
     * @return soleInstance of LogAppender
     */
    public static LogAppender getInstance()
    {
        if (soleInstance == null) soleInstance = new LogAppender();
        return soleInstance;
    }

    /**
     * Stub used by getInstance
     */
    private LogAppender() {}

    /**
     * Formats and appends string to log
     * @param string - string to append
     */
    public void append(String string)
    {
    	if (string != null && string.contains("loci."))
    		return;
        if (t == null)
        {
        	return;
        }
        else
        {
            try
            {
                doc = (StyledDocument) t.getDocument();
                style = doc.addStyle("StyleName", null);
                // StyleConstants.setForeground(style, Color.red);
                StyleConstants.setFontFamily(style, "SansSerif");
                StyleConstants.setFontSize(style, 12);

                doc.insertString(doc.getLength(), string, style);
                
                int maxChars = 100000;
                if (doc.getLength() > maxChars)
                    doc.remove(0, doc.getLength() - maxChars);
                // Toolkit.getDefaultToolkit().beep();
            }
            catch (BadLocationException e)
            {
                throw new RuntimeException(e);
            }
            catch (Throwable t) 
            {
                
            } //Safety catch in case NP passed in.
        }
    }

    /**
     * Set the text area to display logs to
     * 
     * @param debugTextPane
     */
    public void setTextArea(JTextPane debugTextPane)
    {
        this.t = debugTextPane;
    }
    

    /**
     * Sets the logging detail level for all loggers attached to the importer.
     * 
     * @param level - set the default logging level
     */
    public static void setLoggingLevel(Level level)
    {        
        Enumeration<?> loggers = org.apache.log4j.LogManager.getCurrentLoggers();
        
        if (loggers != null)
        {
            while (loggers.hasMoreElements())
            {
                Logger logger = (Logger) loggers.nextElement();
                logger.setLevel(level);
            }
        }
    }
}
