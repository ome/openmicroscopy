/*
 * adminTool.ui.LoginDialog 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package adminTool.ui;

//Java imports
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class LoginDialog extends JDialog implements ActionListener
{
    JButton loginBtn;
    
    private JTextField uname;
    private JPasswordField pswd;
    private JTextField srvr;
    private JTextField prt;
    public String username;
    public String password;
    public String server;
    public String port;
   
    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(LoginDialog.class);

    private String savedUserName = userPrefs.get("savedUserName", "username");
    private String savedHostName = userPrefs.get("savedHostName", "hostname");
    public String savedPortNo = userPrefs.get("savedPortNo", "1099");
    
    public boolean cancelled = true;
    
    public LoginDialog(String title, boolean modal, Point location)
    {
        setLocation(location);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(360,220));
        

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        
        String message = "Type in your OMERO username, password, server hostname, " +
                "and port. Then press the login button to log into the database.";
        
        JTextPane instructions = addTextPane(this, message, c, 0, 4, 1.0f);
        
        uname = addEntryField(this, "Username: ", savedUserName, 'U', c, 0, 1, 2,
                "Input the omero database username here.");
        pswd = addPasswordField(this, "Password: ", null, 'P', c, 0, 1, 2,
        "Input the omero database password here.");

        srvr = addEntryField(this, "Server: ", savedHostName, 'S', c, 0, 1, 2,
        "Input the omero server hostname here.");

        prt = addEntryField(this, "Port: ", savedPortNo, 'R', c, 0, 1, 1,
        "Input the omero server port here.");

        loginBtn = addButton(this, "Login", c, 2, 1, 1.0f,
        "Input the omero database password here.");
        
        loginBtn.addActionListener(this);

        setVisible(true);
    }
    
    static JTextField addEntryField(Container container,
            String name,
            String initialValue,
            int mnemonic,
            GridBagConstraints c,
            int labelCol,
            int labelWidth,
            int fieldWidth,
            String tooltip) {
       
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,20,2,2);
        c.weightx = 0.0;

        c.gridx = labelCol;
        c.gridwidth = labelWidth;
                
        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        container.add(label, c);

        c.insets = new Insets(2,2,2,20);
        c.weightx = 1.0;

        c.gridx = labelCol+1;
        c.gridwidth = fieldWidth;

        JTextField result = new JTextField(100);
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);
        container.add(result, c);
        return result;
    }

    static JPasswordField addPasswordField(Container container,
            String name,
            String initialValue,
            int mnemonic,
            GridBagConstraints c,
            int labelCol,
            int labelWidth,
            int fieldWidth,
            String tooltip) {
       
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,20,2,2);
        c.weightx = 0.0;

        c.gridx = labelCol;
        c.gridwidth = labelWidth;
                
        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        container.add(label, c);

        c.insets = new Insets(2,2,2,20);
        c.weightx = 1.0;

        c.gridx = labelCol+1;
        c.gridwidth = fieldWidth;

        JPasswordField result = new JPasswordField(100);
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);
        container.add(result, c);
        return result;
    }

    static JButton addButton(Container container,
            String name,
            GridBagConstraints c,
            int column,
            int width,
            float weight,
            String tooltip) {
        
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,2,2,20);
        c.weightx = weight;

        c.gridx = column;
        c.gridwidth = width;
                
        JButton button = new JButton(name);
        container.add(button, c);

        return button;
    }

    static JTextPane addTextPane(Container container,
            String text,
            GridBagConstraints c,
            int column,
            int width,
            float weight) {
        
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2,20,2,20);
        c.weightx = weight;

        c.gridx = column;
        c.gridwidth = width;
        
        StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
        StyleConstants.setSpaceAbove(style, 4);
        StyleConstants.setSpaceBelow(style, 10);
        
        try {
            document.insertString(document.getLength(), text, style);
        } catch (BadLocationException e) {
            System.err.println("BadLocationException inserting text to document.");
        }
                        
        JTextPane textPane = new JTextPane(document);
        textPane.setOpaque(false);
        textPane.setEditable(false);
        textPane.setFocusable(false);
        
        container.add(textPane, c);

        return textPane;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == loginBtn)
        {
            username = uname.getText();
            password = new String(pswd.getPassword());
            server = srvr.getText();
            port = prt.getText();
            cancelled = false;
            this.dispose();
        }

    }
}



