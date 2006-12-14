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
