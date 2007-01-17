/*
 * ome.formats.testclient.LogAppender
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

package ome.formats.importer;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
        if (t == null) System.err.println(s);
        else
        {
            try
            {
                doc = (StyledDocument) t.getDocument();
                style = doc.addStyle("StyleName", null);
                // StyleConstants.setForeground(style, Color.red);
                StyleConstants.setFontFamily(style, "SansSerif");
                StyleConstants.setFontSize(style, 12);

                doc.insertString(doc.getLength(), s + "\n", style);
                // Toolkit.getDefaultToolkit().beep();
            } catch (BadLocationException e)
            {

            }
        }
    }

    public void setTextArea(JTextPane debugTextPane)
    {
        this.t = debugTextPane;

    }
}
