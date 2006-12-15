/*
 * adminTool.ui.ErrorDialog 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.ui;

// Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import src.adminTool.model.AdminToolException;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class ErrorDialog extends JDialog implements ActionListener {
    private JTextArea operationText;

    private JTextArea exceptionText;

    private JTextArea exceptionDetail;

    private JScrollPane operationTextPane;

    private JScrollPane exceptionTextPane;

    private JScrollPane exceptionDetailPane;

    private JButton okBtn;

    private JButton showBtn;

    private JTabbedPane tabbedPane;

    private JPanel errorPanel;

    private JPanel detailPanel;

    private JPanel buttonPanel;

    private Icon errorIcon;

    private Exception exception;

    private String operation;

    private String debugText;

    public ErrorDialog(Point location, String title, Exception e, String op) {
        exception = e;
        operation = op;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(title);
        setModal(true);
        setLocation(location);
        setSize(new Dimension(400, 300));
        getExceptionText();
        createUIElements();
        createUI();
        setVisible(true);
    }

    void getExceptionText() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Exception e = ((AdminToolException) exception).getException();
        e.printStackTrace(pw);
        debugText = sw.toString();
    }

    void createUIElements() {
        tabbedPane = new JTabbedPane();
        okBtn = new JButton("Ok");
        okBtn.addActionListener(this);
        okBtn.setActionCommand("ok");
        exceptionDetail = new JTextArea();
        exceptionText = new JTextArea();
        operationText = new JTextArea();
        setTextAreaStyle(operationText);
        setTextAreaStyle(exceptionDetail);
        setTextAreaStyle(exceptionText);
        exceptionDetailPane = new JScrollPane(exceptionDetail);
        exceptionTextPane = new JScrollPane(exceptionText);
        operationTextPane = new JScrollPane(operationText);
        exceptionDetailPane.setBorder(BorderFactory.createLineBorder(new Color(
                83, 134, 219), 2));
        exceptionTextPane.setBorder(BorderFactory.createLineBorder(new Color(
                83, 134, 219), 2));
        operationTextPane.setBorder(BorderFactory.createLineBorder(new Color(
                83, 134, 219), 2));
        errorIcon = new ImageIcon("graphx/messagebox_info_nuvola48.png");
        errorPanel = createErrorPanel();
        detailPanel = createDetailPanel();
        buttonPanel = createButtonPanel();
        tabbedPane.add("Error", errorPanel);
        tabbedPane.add("Debug", detailPanel);
    }

    JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        this.add(Box.createHorizontalStrut(100));
        this.add(okBtn);
        return panel;
    }

    void setTextAreaStyle(JTextArea t) {
        t.setWrapStyleWord(true);
        t.setLineWrap(true);
        t.setAutoscrolls(true);
    }

    JPanel createErrorPanel() {
        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(new JLabel(errorIcon));
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(operationTextPane);
        operationTextPane.setPreferredSize(new Dimension(300, 100));
        operationTextPane.setMaximumSize(new Dimension(300, 100));
        operationText.setText(operation);
        errorPanel.add(topPanel);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BorderLayout());
        labelPanel.add(new JLabel("The server sent this error message."));
        errorPanel.add(labelPanel);
        Exception e = ((AdminToolException) exception).getException();
        errorPanel.add(exceptionTextPane);
        exceptionText.setText(e.getMessage());
        exceptionTextPane.setPreferredSize(new Dimension(360, 75));
        exceptionTextPane.setMaximumSize(new Dimension(360, 75));
        return errorPanel;
    }

    JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.add(exceptionDetailPane);
        exceptionDetail.setText(debugText);
        exceptionDetailPane.setPreferredSize(new Dimension(360, 225));
        exceptionDetailPane.setMaximumSize(new Dimension(360, 225));
        return detailPanel;
    }

    void createUI() {
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(tabbedPane, BorderLayout.NORTH);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok"))
            this.dispose();
    }
}
