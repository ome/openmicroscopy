/*
 * ome.formats.testclient.LoginDialog
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

    JButton                loginBtn;

    private JTextField     uname;

    private JPasswordField pswd;

    private JTextField     srvr;

    private JTextField     prt;

    public String          username;

    public String          password;

    public String          server;

    public String          port;

    public boolean         cancelled = true;

    private Preferences    userPrefs = Preferences
                                             .userNodeForPackage(LoginDialog.class);

    LoginDialog(JFrame owner, String title, boolean modal)
    {
        setLocation(200, 200);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(360, 220));
        setLocationRelativeTo(owner);

        username = userPrefs.get("username", username);
        // password = userPrefs.get("password", password);
        server = userPrefs.get("server", server);
        port = userPrefs.get("port", port);
        if (port == null) port = "1099";

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        String message = "Enter your username, password, server, and port to "
            + "access the database.";

        JTextPane instructions = addTextPane(this, message, c, 0, 4, 1.0f);

        uname = addEntryField(this, "Username: ", username, 'U', c, 0, 1, 2,
                "Input the database username here.");

        pswd = addPasswordField(this, "Password: ", password, 'P', c, 0, 1, 2,
                "Input the database password here.");

        srvr = addEntryField(this, "Server: ", server, 'S', c, 0, 1, 2,
                "Input the server hostname here.");

        prt = addEntryField(this, "Port: ", port, 'R', c, 0, 1, 1,
                "Input the server port here.");

        loginBtn = addButton(this, "Login", c, 2, 1, 1.0f,
                "Click to login.");

        this.getRootPane().setDefaultButton(loginBtn);

        loginBtn.addActionListener(this);

        addWindowListener(new WindowAdapter()
        {

            public void windowOpened(WindowEvent e)
            {
                if (uname == null) uname.requestFocus();
                else
                    pswd.requestFocus();
            }
        });

        setVisible(true);
    }

    static JTextField addEntryField(Container container, String name,
            String initialValue, int mnemonic, GridBagConstraints c,
            int labelCol, int labelWidth, int fieldWidth, String tooltip)
    {

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 2);
        c.weightx = 0.0;

        c.gridx = labelCol;
        c.gridwidth = labelWidth;

        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        container.add(label, c);

        c.insets = new Insets(2, 2, 2, 20);
        c.weightx = 1.0;

        c.gridx = labelCol + 1;
        c.gridwidth = fieldWidth;

        JTextField result = new JTextField(100);
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);
        container.add(result, c);
        return result;
    }

    static JPasswordField addPasswordField(Container container, String name,
            String initialValue, int mnemonic, GridBagConstraints c,
            int labelCol, int labelWidth, int fieldWidth, String tooltip)
    {

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 2);
        c.weightx = 0.0;

        c.gridx = labelCol;
        c.gridwidth = labelWidth;

        JLabel label = new JLabel(name);
        label.setDisplayedMnemonic(mnemonic);
        container.add(label, c);

        c.insets = new Insets(2, 2, 2, 20);
        c.weightx = 1.0;

        c.gridx = labelCol + 1;
        c.gridwidth = fieldWidth;

        JPasswordField result = new JPasswordField(100);
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) result.setText(initialValue);
        container.add(result, c);
        return result;
    }

    static JButton addButton(Container container, String name,
            GridBagConstraints c, int column, int width, float weight,
            String tooltip)
    {

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 2, 2, 20);
        c.weightx = weight;

        c.gridx = column;
        c.gridwidth = width;

        JButton button = new JButton(name);
        container.add(button, c);

        return button;
    }

    static JTextPane addTextPane(Container container, String text,
            GridBagConstraints c, int column, int width, float weight)
    {

        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 20);
        c.weightx = weight;

        c.gridx = column;
        c.gridwidth = width;

        StyleContext context = new StyleContext();
        StyledDocument document = new DefaultStyledDocument(context);

        Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
        StyleConstants.setSpaceAbove(style, 4);
        StyleConstants.setSpaceBelow(style, 10);

        try
        {
            document.insertString(document.getLength(), text, style);
        } catch (BadLocationException e)
        {
            System.err
                    .println("BadLocationException inserting text to document.");
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
            password = pswd.getText();
            server = srvr.getText();
            port = prt.getText();
            cancelled = false;
            this.dispose();
        }

    }
}
