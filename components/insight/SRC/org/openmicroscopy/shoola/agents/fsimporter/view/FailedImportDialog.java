/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2018 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * A dialog for displaying the error logs for files which failed to import.
 * 
 * @author Domink Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FailedImportDialog extends JDialog {

    /** Holds the error logs (key: file name) */
    private SortedMap<String, String> errors = new TreeMap<String, String>();

    /** Maximum broken import files to list */
    private final int MAX_ENTRIES = 50;

    /**
     * Create new instance
     * 
     * @param owner
     *            The dialog owner
     * @param components
     *            The failed imports
     */
    public FailedImportDialog(JFrame owner,
            Collection<FileImportComponentI> components) {
        super(owner);

        for (FileImportComponentI c : components) {
            ImportErrorObject obj = c.getImportErrorObject();
            errors.put(UIUtilities.formatPartialName(obj.getFile()
                    .getAbsolutePath(), 60), UIUtilities.printErrorText(obj
                    .getException()));
        }

        buildUI();
    }

    private void buildUI() {
        setTitle("Failed imports");

        JPanel con = new JPanel();
        con.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        con.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 2;
        c.ipady = 2;

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        con.add(new JLabel("File:"), c);

        String[] tmp = null;
        if (errors.keySet().size() < MAX_ENTRIES) {
            tmp = new String[errors.keySet().size()];
            tmp = errors.keySet().toArray(tmp);
        } else {
            tmp = new String[MAX_ENTRIES + 1];
            Iterator<String> it = errors.keySet().iterator();
            for (int i = 0; i < MAX_ENTRIES; i++)
                tmp[i] = it.next();
            tmp[50] = "... and " + (errors.keySet().size() - MAX_ENTRIES) + " more.";
        }
        String selected = tmp[0];

        final JComboBox<String> filesBox = new JComboBox<String>(tmp);
        filesBox.setSelectedItem(selected);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        con.add(filesBox, c);

        selected = errors.get(selected);
        final JTextArea errorText = new JTextArea(20, 60);
        errorText.setEditable(false);
        errorText.setText(selected);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        con.add(new JLabel("Error log:"), c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        final JScrollPane sp = new JScrollPane(errorText);
        con.add(sp, c);

        filesBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sel = (String) filesBox.getSelectedItem();
                String text = errors.get(sel);
                errorText.setText(text != null ? text : "");
                errorText.setCaretPosition(0);
            }
        });

        final JButton close = new JButton("Close");
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.fill = GridBagConstraints.NONE;
        con.add(close, c);
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FailedImportDialog.this.dispose();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(con, BorderLayout.CENTER);

        pack();

        errorText.setCaretPosition(0);
    }
}
