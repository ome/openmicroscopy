/*
 * ome.formats.importer.gui.LogAppender
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
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

public class LogAppender
{

    private JTextPane          t;

    private StyledDocument     doc;

    private Style              style;

    private static LogAppender soleInstance;

    public static LogAppender getInstance()
    {
        if (soleInstance == null) soleInstance = new LogAppender();
        return soleInstance;
    }

    private LogAppender()
    {

    }

    public void append(String s)
    {
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

                doc.insertString(doc.getLength(), s, style);
                
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

    public void setTextArea(JTextPane debugTextPane)
    {
        this.t = debugTextPane;
    }
    

    /**
     * Sets the logging detail level for all loggers attached to the importer.
     * 
     * @param level
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
