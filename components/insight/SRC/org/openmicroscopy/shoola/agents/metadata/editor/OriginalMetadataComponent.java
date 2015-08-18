/*
 * org.openmicroscopy.shoola.agents.metadata.editor.OriginalMetadataComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import omero.log.LogMessage;
import omero.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import pojos.FileAnnotationData;
import pojos.ImageData;

/**
 * Displays the original metadata.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class OriginalMetadataComponent
    extends JPanel
    implements PropertyChangeListener
{

    /** The columns of the table. */
    private static final String[] COLUMNS;

    static {
        COLUMNS = new String[] {"Tag", "Value"};
    }

    /** Reference to the model.*/
    private EditorModel model;

    /** Flag indicating if the metadata have been loaded or not. */
    private boolean metadataLoaded;

    /** Button to download the file. */
    private JButton downloadButton;

    /** Builds the tool bar displaying the controls. */
    private JComponent toolBar;

    /** The bar displaying the status. */
    private JComponent statusBar;

    /** 
     * Brings up a dialog so that the user can select where to
     * download the file.
     */
    private void download()
    {
        JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
        FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
                "Download Metadata", "Download the metadata file.", null, true);
        chooser.setSelectedFileFull(FileAnnotationData.ORIGINAL_METADATA_NAME);
        chooser.setCheckOverride(true);
        FileAnnotationData data = model.getOriginalMetadata();
        String name = "";
        if (data != null) name = data.getFileName();
        else {
            ImageData img = model.getImage();
            name = FilenameUtils.removeExtension(img.getName());
        }
        chooser.setSelectedFileFull(name);
        chooser.setApproveButtonText("Download");
        IconManager icons = IconManager.getInstance();
        chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
        chooser.addPropertyChangeListener(this);
        chooser.centerDialog();
    }

    /** Initializes the components. */
    private void initComponents()
    {
        IconManager icons = IconManager.getInstance();
        Icon icon = icons.getIcon(IconManager.DOWNLOAD);
        downloadButton = new JButton(icon);
        downloadButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        downloadButton.setOpaque(false);
        UIUtilities.unifiedButtonLookAndFeel(downloadButton);
        downloadButton.setToolTipText("Download the metadata file.");
        downloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) { download(); }
        });
        toolBar = buildToolBar();
        toolBar.setBackground(UIUtilities.BACKGROUND_COLOR);
        JXBusyLabel label = new JXBusyLabel(new Dimension(icon.getIconWidth(),
                icon.getIconHeight()));
        label.setBackground(UIUtilities.BACKGROUND_COLOR);
        label.setBusy(true);
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(label);
        p.add(Box.createHorizontalStrut(5));
        JLabel l = new JLabel("Loading metadata");
        l.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(l);
        statusBar = UIUtilities.buildComponentPanel(p);
        statusBar.setBackground(UIUtilities.BACKGROUND_COLOR);
        setBackground(UIUtilities.BACKGROUND_COLOR);
        setLayout(new BorderLayout(0, 0));
        add(statusBar, BorderLayout.NORTH);
    }

    /** 
     * Builds the tool bar.
     * 
     * @return See above.
     */
    private JComponent buildToolBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(downloadButton);
        return bar;
    }

    /** 
     * Builds and lays out the UI.
     * 
     * @param components The components to lay out.
     */
    private void buildGUI(Map<String, List<String>> components)
    {
        //Now lay out the elements
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        String key;
        List<String> l;
        Entry<String, List<String>> entry;
        Iterator<Entry<String, List<String>>>
        i = components.entrySet().iterator();
        JPanel row;
        JLabel label;
        p.add(new JSeparator());
        while (i.hasNext()) {
            entry = i.next();
            key = entry.getKey();
            l = entry.getValue();
            if (!CollectionUtils.isEmpty(l)) {
                label = UIUtilities.setTextFont(key);
                label.setBackground(UIUtilities.BACKGROUND_COLOR);
                row = UIUtilities.buildComponentPanel(label);
                row.setBackground(UIUtilities.BACKGROUND_COLOR);
                p.add(row);
                p.add(createTable(l));
            }
        }
        removeAll();
        add(toolBar, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Creates a new table.
     * 
     * @param list The list of elements to add to the table.
     * @return See above
     */
    private JScrollPane createTable(List<String> list)
    {
        Iterator<String> i = list.iterator();
        String[] values;
        Object[][] data = new Object[list.size()][2];
        int index = 0;
        String line;
        String s;
        String[] numbers;
        int count;
        while (i.hasNext()) {
            line = i.next();
            values = line.split("=");
            switch (values.length) {
            case 0:
                data[index][0] = line;
                break;
            case 1:
                data[index][0] = values[0];
                break;
            case 2:
                data[index][0] = values[0];
                data[index][1] = values[1];
                break;
            default:
                s = values[values.length-1];
                numbers = s.split(" ");
                if (numbers != null) {
                    StringBuffer buffer = new StringBuffer();
                    count = 0;
                    for (int j = 0; j < numbers.length; j++) {
                        try {
                            Double.parseDouble(numbers[j]);
                            count++;
                        } catch (Exception e) {
                        }
                    }
                    if (count == numbers.length && count > 1) {
                        //It is a number.
                        s = "";
                        for (int j = 0; j < values.length-1; j++) {
                            buffer.append(values[j]);
                            if (j < values.length-2) buffer.append("=");
                        }

                        data[index][0] = buffer.toString();
                        data[index][1] = values[values.length-1];
                    } else {
                        data[index][0] = values[0];
                        s = "";
                        for (int j = 1; j < values.length; j++) {
                            buffer.append(values[j]);
                            if (j < values.length-1) buffer.append("=");
                        }
                        data[index][1] = buffer.toString();
                    }
                } else data[index][0] = line;
            }
            index++;
        }
        JXTable table = new JXTable(
                new OriginalMetadataTableModel(data, COLUMNS));
        Highlighter h = HighlighterFactory.createAlternateStriping(
                UIUtilities.BACKGROUND_COLOUR_EVEN,
                UIUtilities.BACKGROUND_COLOUR_ODD);
        table.addHighlighter(h);
        return new JScrollPane(table);
    }

    /** 
     * Returns the start to extract the key.
     * 
     * @param line The line to handles
     * @return See above.
     */
    private int getStart(String line)
    {
        if (CommonsLangUtils.isBlank(line)) return 0;
        if (line.startsWith("[")) return 1;
        return 0;
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model.
     */
    OriginalMetadataComponent(EditorModel model)
    {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
    }

    /**
     * Returns <code>true</code> if the metadata have been loaded.
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isMetadataLoaded() { return metadataLoaded; }

    /** Removes all the components when a new node is selected. */
    void clear()
    { 
        metadataLoaded = false;
        removeAll();
        add(statusBar, BorderLayout.NORTH);
    }

    /**
     * Reads the file and displays the result in a table.
     * 
     * @param file The file to read.
     */
    void setOriginalFile(File file)
    {
        metadataLoaded = true;
        if (file == null) {
            JLabel l = new JLabel("Metadata could not be retrieved.");
            l.setBackground(UIUtilities.BACKGROUND_COLOR);
            statusBar = UIUtilities.buildComponentPanel(l);
            statusBar.setBackground(UIUtilities.BACKGROUND_COLOR);
            removeAll();
            add(statusBar, BorderLayout.NORTH);
            return;
        }
        downloadButton.setEnabled(file.length() != 0);
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            Map<String, List<String>> components = 
                    new LinkedHashMap<String, List<String>>();
            try {
                String line = null;
                List<String> l;
                String key = null;
                int start = 0;
                while ((line = input.readLine()) != null) {
                    if (line.contains("=")) {
                        if (CommonsLangUtils.isNotBlank(key)) {
                            l = components.get(key);
                            if (l != null) l.add(line);
                        }
                    } else {
                        line = line.trim();
                        if (line.length() > 0) {
                            start = getStart(line);
                            key = line.substring(start, line.length()-1);
                            if (CommonsLangUtils.isNotBlank(key))
                                components.put(key, new ArrayList<String>());
                        }
                    }
                }
            } finally {
                input.close();
            }
            buildGUI(components);
            file.delete();
        } catch (IOException e) {
            file.delete();
            JLabel l = new JLabel("Loading metadata");
            l.setBackground(UIUtilities.BACKGROUND_COLOR);
            statusBar = UIUtilities.buildComponentPanel(l);
            statusBar.setBackground(UIUtilities.BACKGROUND_COLOR);
            removeAll();
            add(statusBar, BorderLayout.NORTH);
            Logger logger = MetadataViewerAgent.getRegistry().getLogger();
            LogMessage msg = new LogMessage();
            msg.print("An error occurred while reading metadata file.");
            msg.print(e);
            logger.error(this, msg);
        }
    }

    /**
     * Downloads the original file.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
            File[] files = (File[]) evt.getNewValue();
            File folder = files[0];
            if (folder == null)
                folder = UIUtilities.getDefaultFolder();
            UserNotifier un =
                    MetadataViewerAgent.getRegistry().getUserNotifier();
            ImageData img = model.getImage();
            if (img == null) return;
            IconManager icons = IconManager.getInstance();
            DownloadActivityParam activity =
                    new DownloadActivityParam(img.getId(),
                            DownloadActivityParam.METADATA_FROM_IMAGE,
                            folder, icons.getIcon(IconManager.DOWNLOAD_22));
            un.notifyActivity(model.getSecurityContext(), activity);
        }
    }

    /** Extends the table model so that the cells cannot be edited. */
    class OriginalMetadataTableModel
        extends DefaultTableModel
    {
        /**
         *  Constructs a <code>DefaultTableModel</code> and initializes the table
         *  by passing <code>data</code> and <code>columnNames</code>
         *  to the <code>setDataVector</code>
         *  method. The first index in the <code>Object[][]</code> array is
         *  the row index and the second is the column index.
         *
         * @param data the data of the table
         * @param columnNames the names of the columns
         * @see #getDataVector
         * @see #setDataVector
         */
        OriginalMetadataTableModel(Object[][] data, Object[] columnNames)
        {
            super(data, columnNames);
        }

        /**
         * Overridden so that the cell cannot be edited.
         * @see OriginalMetadataTableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int row, int column) { return false; }
    }

}
