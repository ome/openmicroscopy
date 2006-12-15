/*
 * ome.formats.testclient.ImportDialog
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.OMEROMetadataStore;
import ome.model.containers.Dataset;
import ome.model.containers.Project;

/**
 * @author "Brian W. Loranger"
 */
@SuppressWarnings("serial")
public class ImportDialog extends JDialog implements ActionListener {
    private JTextPane instructions;

    private JRadioButton fullPathButton;

    private JRadioButton partPathButton;

    private WholeNumberField numOfDirectoriesField;

    public JCheckBox archiveImage;

    private JButton cancelBtn;

    private JButton importBtn;

    private JComboBox pbox;

    private JComboBox dbox;

    public Dataset dataset;

    public Project project;

    public DatasetItem[] datasetItems = null;

    public ProjectItem[] projectItems = null;

    public boolean cancelled = true;

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(ImportDialog.class);

    public OMEROMetadataStore store;

    private Preferences userPrefs = Preferences
            .userNodeForPackage(ImportDialog.class);

    private Long savedProject = userPrefs.getLong("savedProject", 0);

    private Long savedDataset = userPrefs.getLong("savedDataset", 0);

    public Boolean useFullPath = userPrefs.getBoolean("savedFileNaming", true);

    public Integer numOfDirectories = userPrefs.getInt("savedNumOfDirs", 0);

    ImportDialog(JFrame owner, String title, boolean modal,
            OMEROMetadataStore store) {
        this.store = store;

        if (store != null) {
            projectItems = ProjectItem.createProjectItems(store.getProjects());
            datasetItems = DatasetItem.createEmptyDataset();
        }

        setLocation(200, 200);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(300, 270));
        setLocationRelativeTo(owner);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        String message = "Import these images into which dataset?";

        instructions = addTextPane(this, message, c, 0, 4, 1.0f);

        pbox = addComboBox(this, "Project: ", projectItems, 'P', c, 0, 1, 2,
                "Select dataset to use for this import.");

        dbox = addComboBox(this, "Dataset: ", datasetItems, 'D', c, 0, 1, 2,
                "Select dataset to use for this import.");

        dbox.setEnabled(false);

        JPanel namedPanel = addNamedPanel(this, " File Naming ", c, 0, 4, 1.0f);

        fullPathButton = addRadioButton("Use the full path + file name", c);
        partPathButton = addRadioButton("Use just the file name and include", c);

        ButtonGroup group = new ButtonGroup();
        group.add(fullPathButton);
        group.add(partPathButton);

        if (useFullPath == true) {
            group.setSelected(fullPathButton.getModel(), true);
        } else {
            group.setSelected(partPathButton.getModel(), true);
        }

        namedPanel.add(fullPathButton, c);
        namedPanel.add(partPathButton, c);

        JPanel plainPanel = addPlainPanel(namedPanel, c);
        numOfDirectoriesField = addEntryField(plainPanel, "", "0",
                " directories", 0,
                "Add this number of directories to the file names", 3);
        numOfDirectoriesField.setText(numOfDirectories.toString());

        // // focus on the partial path button if you enter the numofdirfield
        // numOfDirectoriesField.addFocusListener(new FocusListener() {
        // public void focusGained(FocusEvent e) {
        // partPathButton.setSelected(true);
        // }
        //
        // public void focusLost(FocusEvent e) {}
        // });

        archiveImage = addCheckBox(this,
                "Also archive the original image file(s)", c);
        archiveImage.setSelected(false);
        archiveImage.setVisible(false);

        cancelBtn = addButton(this, "Cancel", c, 1, 1, 1.0f, null);
        importBtn = addButton(this, "Add", c, 2, 1, 1.0f, null);

        importBtn.setEnabled(false);
        this.getRootPane().setDefaultButton(importBtn);

        fullPathButton.addActionListener(this);
        partPathButton.addActionListener(this);
        numOfDirectoriesField.addActionListener(this);
        cancelBtn.addActionListener(this);
        importBtn.addActionListener(this);
        pbox.addActionListener(this);

        if (savedProject != 0 && projectItems != null) {
            for (int i = 0; i < projectItems.length; i++) {

                Long pId = projectItems[i].getId();

                if (pId != null && pId.equals(savedProject)) {
                    pbox.setSelectedIndex(i);

                    Project p = ((ProjectItem) pbox.getSelectedItem())
                            .getProject();
                    datasetItems = DatasetItem.createDatasetItems(store
                            .getDatasets(p));
                    dbox.removeAllItems();
                    if (datasetItems.length == 0
                            || pbox.getSelectedIndex() == 0) {

                        datasetItems = DatasetItem.createEmptyDataset();
                        dbox.addItem(datasetItems[0]);
                        dbox.setEnabled(false);
                        importBtn.setEnabled(false);
                    } else {
                        for (int k = 0; k < datasetItems.length; k++) {
                            Long dId = datasetItems[k].getId();
                            dbox.setEnabled(true);
                            importBtn.setEnabled(true);
                            dbox.addItem(datasetItems[k]);
                            if (dId != null && dId.equals(savedDataset)) {
                                dbox.setSelectedIndex(k);
                            }
                        }
                    }
                }
            }
        }

        setVisible(true);
    }

    private JCheckBox addCheckBox(Container container, String string,
            GridBagConstraints c) {
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 20);
        c.weightx = 0.0;

        c.gridx = 0;
        c.gridwidth = 3;

        JCheckBox checkBox = new JCheckBox(string);

        container.add(checkBox, c);

        return checkBox;
    }

    private JRadioButton addRadioButton(String string, GridBagConstraints c) {
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 2, 2, 2);
        c.weightx = 0.0;

        c.gridx = 0;
        c.gridwidth = 3;

        JRadioButton button = new JRadioButton(string);

        return button;

    }

    private WholeNumberField addEntryField(Container container,
            String prefexStr, String initialValue, String suffexStr,
            int mnemonic, String tooltip, int fieldWidth) {
        JLabel prefex = new JLabel(prefexStr);
        prefex.setDisplayedMnemonic(mnemonic);
        container.add(prefex);

        WholeNumberField result = new WholeNumberField(0, fieldWidth);
        result.setHorizontalAlignment(JTextField.CENTER);
        prefex.setLabelFor(result);
        result.setToolTipText(tooltip);
        if (initialValue != null) {
            result.setText(initialValue);
        }

        container.add(result);

        JLabel suffex = new JLabel(suffexStr);
        container.add(suffex);

        return result;
    }

    static JComboBox addComboBox(Container container, String name,
            Object[] initialValues, int mnemonic, GridBagConstraints c,
            int labelCol, int labelWidth, int fieldWidth, String tooltip) {

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

        JComboBox result = null;
        if (initialValues != null) {
            result = new JComboBox(initialValues);
        } else {
            result = new JComboBox();
        }
        label.setLabelFor(result);
        result.setToolTipText(tooltip);
        container.add(result, c);
        return result;
    }

    static JButton addButton(Container container, String name,
            GridBagConstraints c, int column, int width, float weight,
            String tooltip) {

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
            GridBagConstraints c, int column, int width, float weight) {

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

        try {
            document.insertString(document.getLength(), text, style);
        } catch (BadLocationException e) {
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

    static JPanel addNamedPanel(Container container, String name,
            GridBagConstraints c, int column, int width, float weight) {

        c.anchor = GridBagConstraints.SOUTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 20);
        c.weightx = weight;

        c.gridx = column;
        c.gridwidth = width;

        JPanel namedPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints g = new GridBagConstraints();

        namedPanel.setLayout(gridbag);
        namedPanel.setBorder(BorderFactory.createTitledBorder(name));

        container.add(namedPanel, c);

        return namedPanel;
    }

    static JPanel addPlainPanel(Container container, GridBagConstraints c) {

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 20, 2, 2);
        c.weightx = 2.0;

        c.gridx = 0;
        c.gridwidth = 0;

        JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints g = new GridBagConstraints();
        panel.setLayout(gridbag);

        container.add(panel, c);

        return panel;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fullPathButton) {
            useFullPath = true;

        }
        if (e.getSource() == partPathButton) {
            useFullPath = false;
        }
        if (e.getSource() == cancelBtn) {
            cancelled = true;
            this.dispose();
        }
        if (e.getSource() == importBtn) {
            cancelled = false;
            importBtn.requestFocus();
            numOfDirectories = numOfDirectoriesField.getValue();
            dataset = ((DatasetItem) dbox.getSelectedItem()).getDataset();
            project = ((ProjectItem) pbox.getSelectedItem()).getProject();
            userPrefs.putLong("savedProject", ((ProjectItem) pbox
                    .getSelectedItem()).getId());
            userPrefs.putLong("savedDataset", dataset.getId());
            if (fullPathButton.isSelected() == true) {
                userPrefs.putBoolean("savedFileNaming", true);
            } else {
                userPrefs.putBoolean("savedFileNaming", false);
            }
            userPrefs
                    .putInt("savedNumOfDirs", numOfDirectoriesField.getValue());

            this.dispose();
        }
        if (e.getSource() == pbox) {
            cancelled = false;

            if (pbox.getSelectedIndex() == 0) {
                dbox.setEnabled(false);
            } else {
                Project p = ((ProjectItem) pbox.getSelectedItem()).getProject();
                datasetItems = DatasetItem.createDatasetItems(store
                        .getDatasets(p));
            }

            dbox.removeAllItems();
            if (datasetItems.length == 0 || pbox.getSelectedIndex() == 0) {
                datasetItems = DatasetItem.createEmptyDataset();
                dbox.addItem(datasetItems[0]);
                dbox.setEnabled(false);
                importBtn.setEnabled(false);
            } else {
                for (int i = 0; i < datasetItems.length; i++) {
                    dbox.setEnabled(true);
                    importBtn.setEnabled(true);
                    dbox.addItem(datasetItems[i]);
                }
            }

        }
    }

    public static void main(String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName();
        // laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        // laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        // laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        // laf = "javax.swing.plaf.metal.MetalLookAndFeel";

        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            System.err.println(laf + " not supported.");
        }

        ImportDialog dialog = new ImportDialog(null, "Test", true, null);
        if (dialog != null) {
            System.exit(0);
        }
    }

    public class WholeNumberField extends JTextField {

        private Toolkit toolkit;

        private NumberFormat integerFormatter;

        public WholeNumberField(int value, int columns) {
            super(columns);
            toolkit = Toolkit.getDefaultToolkit();
            integerFormatter = NumberFormat.getNumberInstance(Locale.US);
            integerFormatter.setParseIntegerOnly(true);
            setValue(value);
        }

        public int getValue() {
            int retVal = 0;
            try {
                retVal = integerFormatter.parse(getText()).intValue();
            } catch (ParseException e) {
                // This should never happen because insertString allows
                // only properly formatted data to get in the field.
                toolkit.beep();
            }
            return retVal;
        }

        public void setValue(int value) {
            setText(integerFormatter.format(value));
        }

        @Override
        protected Document createDefaultModel() {
            return new WholeNumberDocument();
        }

        protected class WholeNumberDocument extends PlainDocument {

            @Override
            public void insertString(int offs, String str, AttributeSet a)
                    throws BadLocationException {

                char[] source = str.toCharArray();
                char[] result = new char[source.length];
                int j = 0;

                for (int i = 0; i < result.length; i++) {
                    if (Character.isDigit(source[i])) {
                        result[j++] = source[i];
                    } else {
                        toolkit.beep();
                        // System.err.println("insertString: " + source[i]);
                    }
                }
                super.insertString(offs, new String(result, 0, j), a);

            }

        }

    }
}

// Helper classes used by the dialog comboboxes
class DatasetItem {
    private Dataset dataset;

    public DatasetItem(Dataset dataset) {
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public String toString() {
        if (dataset == null) {
            return "";
        }
        return dataset.getName();
    }

    public Long getId() {
        return dataset.getId();
    }

    public static DatasetItem[] createDatasetItems(List<Dataset> datasets) {
        DatasetItem[] items = new DatasetItem[datasets.size()];
        for (int i = 0; i < datasets.size(); i++) {
            items[i] = new DatasetItem(datasets.get(i));
        }
        return items;
    }

    public static DatasetItem[] createEmptyDataset() {
        Dataset d = new Dataset();
        d.setName("--- Empty Set ---");
        DatasetItem[] items = new DatasetItem[1];
        items[0] = new DatasetItem(d);
        return items;
    }
}

class ProjectItem {
    private Project project;

    public ProjectItem(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public String toString() {
        return project.getName();
    }

    public Long getId() {
        return project.getId();
    }

    public static ProjectItem[] createProjectItems(List<Project> projects) {
        ProjectItem[] items = new ProjectItem[projects.size() + 1];
        Project p = new Project();
        p.setName("--- Select Project ---");
        items[0] = new ProjectItem(p);

        for (int i = 1; i < projects.size() + 1; i++) {
            items[i] = new ProjectItem(projects.get(i - 1));
        }
        return items;
    }
}