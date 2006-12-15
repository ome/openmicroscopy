/*
 * src.adminTool.ui.messenger.MessageBox 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.ui.messenger;

// Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$Date: $)
 *          </small>
 * @since OME3.0
 */
public class MessageBox extends JDialog implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 641206591947204316L;

    private JTextArea text;

    private JScrollPane pane;

    private String message;

    private JButton okBtn;

    public MessageBox(Point location, String title, String message) {
        this.message = message;
        setTitle(title);
        setModal(true);
        setSize(300, 180);
        setLocation(location);
        createUI();
        this.setVisible(true);
    }

    void createUI() {
        text = new JTextArea(message);
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        pane = new JScrollPane(text);
        pane.setBorder(BorderFactory
                .createLineBorder(new Color(60, 80, 155), 2));
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
        messagePanel.add(pane);
        okBtn = new JButton("Ok");
        okBtn.addActionListener(this);
        okBtn.setActionCommand("ok");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(160));
        panel.add(okBtn);
        okBtn.setPreferredSize(new Dimension(40, 20));

        this.getContentPane().setLayout(
                new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().add(Box.createVerticalStrut(10));
        this.getContentPane().add(messagePanel);
        messagePanel.setPreferredSize(new Dimension(260, 100));
        messagePanel.setMaximumSize(new Dimension(260, 100));
        messagePanel.setMinimumSize(new Dimension(260, 100));
        this.getContentPane().add(Box.createVerticalStrut(10));
        this.getContentPane().add(panel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            this.dispose();
        }
    }
}
