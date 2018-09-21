/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2018 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorHandler;

import org.apache.commons.io.FileUtils;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/**
 * Component displaying the status of a specific import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * @since 3.0-Beta4
 */
public class StatusLabel extends JPanel implements PropertyChangeListener {

    /** The default text of the component. */
    public static final String DEFAULT_TEXT = "Pending...";
    
    /** The text displayed when the file is already selected. */
    public static final String DUPLICATE = "Already processed, skipping";

    /** The text indicating the scanning steps. */
    public static final String SCANNING_TEXT = "Scanning...";

    /** The text indicating the scanning steps. */
    public static final String CANCELLED_TEXT = "Cancelled";

    /** The width of the upload bar. */
    private static final int WIDTH = 200;

    /** The maximum number of value for upload. */
    private static final int MAX = 100;
    
    /** The label displaying the general import information. */
    private JLabel generalLabel;

    /** Indicate the progress of the upload. */
    private JProgressBar uploadBar;

    /** Indicate the progress of the processing. */
    private JProgressBar processingBar;
    
    /** The labels displaying information before the progress bars. */
    private List<JLabel> labels;

    /** The exception if an error occurred. */
    private ImportException exception;

    /** The component tracking the status */
    private Status status;
    
    /**
     * Formats the size of the uploaded data.
     * 
     * @param value
     *            The value to display.
     * @return See above.
     */
    private String formatUpload(long value) {
        StringBuffer buffer = new StringBuffer();
        String v = FileUtils.byteCountToDisplaySize(value);
        String[] values = v.split(" ");
        if (values.length > 1) {
            String u = values[1];
            if (status.getUnits().equals(u))
                buffer.append(values[0]);
            else
                buffer.append(v);
        } else
            buffer.append(v);
        buffer.append("/");
        buffer.append(status.getFileSize());
        return buffer.toString();
    }

    /** Builds and lays out the UI. */
    private void buildUI() {
        labels = new ArrayList<JLabel>();
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(generalLabel);
        JLabel label = new JLabel("Upload");
        label.setVisible(false);
        labels.add(label);
        add(label);
        add(uploadBar);
        add(Box.createHorizontalStrut(5));
        label = new JLabel("Processing");
        label.setVisible(false);
        labels.add(label);
        add(label);
        add(processingBar);
        setOpaque(false);
    }

    /** Initializes the components. */
    private void initialize() {
        generalLabel = new JLabel(DEFAULT_TEXT);
        Font f = generalLabel.getFont();
        Font derived = f.deriveFont(f.getStyle(), f.getSize() - 2);
        uploadBar = new JProgressBar(0, MAX);
        uploadBar.setFont(derived);
        uploadBar.setStringPainted(true);
        Dimension d = uploadBar.getPreferredSize();
        uploadBar.setPreferredSize(new Dimension(WIDTH, d.height));
        processingBar = new JProgressBar(0, Status.STEPS.size());
        processingBar.setStringPainted(true);
        processingBar.setString(DEFAULT_TEXT);
        processingBar.setFont(derived);
        uploadBar.setVisible(false);
        processingBar.setVisible(false);
    }

    /**
     * Creates a new instance.
     * 
     * @param status
     *            The status
     */
    public StatusLabel(Status status) {
        this.status = status;
        status.addPropertyChangeListener(this);
        initialize();
        buildUI();
    }

    @Override
    public void propertyChange(PropertyChangeEvent pe) {
        if (pe.getPropertyName().equals(Status.IMPORT_EVENT)) {
            ImportEvent event = (ImportEvent) pe.getNewValue();
            if (event instanceof ImportCandidates.SCANNING) {
                if (!status.isMarkedAsCancel() && exception == null)
                    generalLabel.setText(SCANNING_TEXT);
            } else if (event instanceof ErrorHandler.MISSING_LIBRARY) {
                exception = new ImportException(
                        ImportException.MISSING_LIBRARY_TEXT,
                        ((ErrorHandler.MISSING_LIBRARY) event).exception);
            } else if (event instanceof ErrorHandler.UNKNOWN_FORMAT) {
                exception = new ImportException(
                        ImportException.UNKNOWN_FORMAT_TEXT,
                        ((ErrorHandler.UNKNOWN_FORMAT) event).exception);
            } else if (event instanceof ErrorHandler.FILE_EXCEPTION) {
                ErrorHandler.FILE_EXCEPTION e = (ErrorHandler.FILE_EXCEPTION) event;
                exception = new ImportException(e.exception);
            } else if (event instanceof ErrorHandler.INTERNAL_EXCEPTION) {
                ErrorHandler.INTERNAL_EXCEPTION e = (ErrorHandler.INTERNAL_EXCEPTION) event;
                exception = new ImportException(e.exception);
            } else if (event instanceof ImportEvent.FILE_UPLOAD_BYTES) {
                ImportEvent.FILE_UPLOAD_BYTES e = (ImportEvent.FILE_UPLOAD_BYTES) event;
                long v = status.getTotalUploadedSize() + e.uploadedBytes;
                if (status.getSizeUpload() != 0) {
                    uploadBar.setValue((int) (v * MAX / status.getSizeUpload()));
                }
                StringBuffer buffer = new StringBuffer();
                if (v != status.getSizeUpload())
                    buffer.append(formatUpload(v));
                else
                    buffer.append(status.getFileSize());
                buffer.append(" ");
                if (e.timeLeft != 0) {
                    String s = UIUtilities.calculateHMSFromMilliseconds(
                            e.timeLeft, true);
                    buffer.append(s);
                    if (CommonsLangUtils.isNotBlank(s))
                        buffer.append(" Left");
                    else
                        buffer.append("complete");
                }
                uploadBar.setString(buffer.toString());
            } else if (event instanceof ImportEvent.FILESET_UPLOAD_START) {
                Iterator<JLabel> i = labels.iterator();
                while (i.hasNext()) {
                    i.next().setVisible(true);
                }
                generalLabel.setText("");
                uploadBar.setVisible(true);
                processingBar.setVisible(true);
            } else if (event instanceof ImportEvent.FILESET_UPLOAD_PREPARATION) {
                generalLabel.setText("Preparing upload...");
            } else if (event instanceof ImportEvent.IMPORT_STARTED) {
                ImportEvent.IMPORT_STARTED e = (ImportEvent.IMPORT_STARTED) event;
            }
            
            processingBar.setValue(status.getStep());
            processingBar.setString(Status.STEPS.get(status.getStep()));
        } else if(pe.getPropertyName().equals(Status.DUPLICATE_PROPERTY)) {
            generalLabel.setText(DUPLICATE);
        } else if(pe.getPropertyName().equals(Status.CANCELLED_PROPERTY)) {
            generalLabel.setText(CANCELLED_TEXT);
        }
    }

}
