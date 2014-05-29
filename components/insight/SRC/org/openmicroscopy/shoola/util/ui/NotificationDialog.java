/*
 * org.openmicroscopy.shoola.util.ui.NotificationDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

//Third-party libraries
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXHeader.IconPosition;
import org.jdesktop.swingx.painter.RectanglePainter;

//Application-internal dependencies

/** 
 * A general-purpose modal dialog to display a notification message.
 * An icon can be specified to display by the message and an <i>OK</i>
 * button is provided to close the dialog. The dialog is brought up by the
 * {@link #setVisible(boolean)} method and is automatically disposed after the
 * user closes it.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *          <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class NotificationDialog
    extends JDialog
{

    /** Bound property indication to do something with the hyperlink.*/
    public static final String HYPERLINK_OPEN_PROPERTY = "hyperlinkOpen";

    /** Bound property indication to close the notification dialog.*/
    public static final String CLOSE_NOTIFICATION_PROPERTY =
            "closeNotification";

    /** Bound property indication to close the notification dialog.*/
    public static final String CANCEL_NOTIFICATION_PROPERTY =
            "cancelNotification";

    /** 
     * The preferred size of the widget that displays the notification message.
     * Only the part of text that fits into this display area will be displayed.
     */
    protected static final Dimension MSG_AREA_SIZE = new Dimension(300, 50);

    /** 
     * The size of the invisible components used to separate widgets
     * horizontally.
     */
    protected static final Dimension H_SPACER_SIZE = new Dimension(20, 1);

    /** 
     * The size of the invisible components used to separate widgets
     * vertically.
     */
    protected static final Dimension V_SPACER_SIZE = new Dimension(1, 20);

    /** 
     * The outmost container.
     * All other widgets are added to this panel, which, in turn, is then 
     * added to the dialog's content pane.
     */
    protected JXHeader contentPanel;

    /** Contains the message and the message icon, if any. */
    protected JPanel messagePanel;

    /** Contains the {@link #okButton}. */
    protected JPanel controlsPanel;

    /** Hides and disposes of the dialog. */
    protected JButton okButton;

    /** Cancel any action. */
    protected JButton cancelButton;

    /** Component hosting the UI components. */
    private JPanel mainPanel;

    /** The original message displayed.*/
    protected String message;

    /** 
     * Listener invoking the <code>close</code> method when the dialog
     * shuts down.
     */
    protected WindowAdapter windowAdapter;

    /** Creates the various UI components that make up the dialog. */
    private void createComponents()
    {
        mainPanel = new JPanel();
        contentPanel = new JXHeader();
        contentPanel.setBackgroundPainter(
                new RectanglePainter(getBackground(), null));
        messagePanel = new JPanel();
        messagePanel.setOpaque(true);
        controlsPanel = new JPanel();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        cancelButton.setVisible(false);
        getRootPane().setDefaultButton(okButton);
    }

    /**
     * Binds the {@link #close() close} action to the exit event generated
     * either by the close icon or by the {@link #okButton}.
     */
    private void attachListeners()
    {
        windowAdapter = new WindowAdapter() {
            public void windowClosing(WindowEvent we) { close(); }
        };
        addWindowListener(windowAdapter);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { close(); }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { cancel(); }
        });
    }

    /** Cancels any action. */
    protected void cancel()
    {
        setVisible(false);
        dispose();
        firePropertyChange(CANCEL_NOTIFICATION_PROPERTY,
                Boolean.valueOf(false), Boolean.valueOf(true));
    }

    /** Hides and disposes of the dialog. */
    protected void close()
    {
        setVisible(false);
        dispose();
        firePropertyChange(CLOSE_NOTIFICATION_PROPERTY,
                Boolean.valueOf(false), Boolean.valueOf(true));
    }

    /**
     * Builds and lays out the {@link #contentPanel}.
     * It will contain the notification message along with the message icon,
     * if any.
     *
     * @param msg The notification message.
     * @param icon The icon to display by the message.
     * @return See above.
     */
    private JPanel buildCommentPanel(String msg, Icon icon)
    {
        contentPanel.setDescription(msg);
        contentPanel.setOpaque(false);
        if (icon != null) {
            contentPanel.setIcon(icon);
            contentPanel.setIconPosition(IconPosition.LEFT);
        }

        return contentPanel;
    }

    /**
     * Builds and lays out the {@link #controlsPanel}.
     * The {@link #okButton} will be added to this panel.
     *
     * @return See above.
     */
    private JPanel buildControlPanel()
    {
        controlsPanel.setBorder(null);
        controlsPanel.add(cancelButton);
        controlsPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        controlsPanel.add(okButton);
        controlsPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        return UIUtilities.buildComponentPanelRight(controlsPanel);
    }

    /** 
     * Formats the text as <code>HTML</code> text.
     * 
     * @param message The message to display.
     * @return See above.
     */
    private JEditorPane buildHTMLPane(String message)
    {
        JEditorPane htmlPane =UIUtilities.buildTextEditorPane(message);
        htmlPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(
                        e.getEventType())) {
                    String url;
                    if (e.getURL() == null) url = e.getDescription();
                    else url = e.getURL().toString();
                    firePropertyChange(HYPERLINK_OPEN_PROPERTY, null, url);
                }

            }
        });
        return htmlPane;
    }
    /**
     * Builds and lays out the {@link #contentPanel}, then adds it to the
     * content pane.
     *
     * @param message The notification message.
     * @param messageIcon The icon to display by the message.
     */
    private void buildGUI(String message, Icon messageIcon, boolean html)
    {
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        if (html) mainPanel.add(buildHTMLPane(message), c);
        else mainPanel.add(buildCommentPanel(message, messageIcon), c);
        c.gridy++;
        mainPanel.add(Box.createVerticalStrut(5), c);
        c.gridy++;
        mainPanel.add(buildControlPanel(), c);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Initializes the UI.
     *
     * @param message The message to display.
     * @param messageIcon The icon laid out next to the message.
     * @param html Indicates to use an pane displaying <code>HTML</code>
     * content if <code>true</code>, <code>false</code> otherwise.
     */
    private void initiliaze(String message, Icon messageIcon, boolean html)
    {
        this.message = message;
        createComponents();
        attachListeners();
        setAlwaysOnTop(true);
        setModal(true);
        buildGUI(message, messageIcon, html);
        pack();
    }

    /**
     * Creates a new dialog.
     * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
     *
     * @param owner The parent window.
     * @param title The title to display on the title bar.
     * @param message The notification message.
     * @param messageIcon An optional icon to display by the message.
     */
    public NotificationDialog(JFrame owner, String title, String message,
            Icon messageIcon)
    {
        super(owner, title);
        //setResizable(false);
        //Believe it or not the icon from owner won't be displayed if the
        //dialog is not resizable. 
        initiliaze(message, messageIcon, false);
    }

    /**
     * Creates a new dialog.
     * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
     *
     * @param owner The parent window.
     * @param title The title to display on the title bar.
     * @param message The notification message.
     * @param messageIcon An optional icon to display by the message.
     */
    public NotificationDialog(JDialog owner, String title, String message,
            Icon messageIcon) 
    {
        super(owner, title);
        initiliaze(message, messageIcon, false);
    }

    /**
     * Creates a new dialog.
     * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
     *
     * @param title The title to display on the title bar.
     * @param message The notification message.
     * @param messageIcon An optional icon to display by the message.
     */
    public NotificationDialog(String title, String message, Icon messageIcon)
    {
        setTitle(title);
        initiliaze(message, messageIcon, false);
    }

    /**
     * Creates a new dialog.
     * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
     *
     * @param owner The parent window.
     * @param title The title to display on the title bar.
     * @param message The notification message.
     * @param html Indicates to use an pane displaying <code>HTML</code>
     * content if <code>true</code>, <code>false</code> otherwise.
     */
    public NotificationDialog(JDialog owner, String title, String message,
            boolean html)
    {
        super(owner, title);
        //setResizable(false);
        //Believe it or not the icon from owner won't be displayed if the
        //dialog is not resizable. 
        initiliaze(message, null, html);
    }

    /**
     * Creates a new dialog.
     * You have to call {@link #setVisible(boolean)} to actually display it
     * on screen.
     *
     * @param owner The parent window.
     * @param title The title to display on the title bar.
     * @param message The notification message.
     * @param html Indicates to use an pane displaying <code>HTML</code>
     * content if <code>true</code>, <code>false</code> otherwise.
     */
    public NotificationDialog(JFrame owner, String title, String message,
            boolean html)
    {
        super(owner, title);
        //setResizable(false);
        //Believe it or not the icon from owner won't be displayed if the
        //dialog is not resizable. 
        initiliaze(message, null, html);
    }

}
