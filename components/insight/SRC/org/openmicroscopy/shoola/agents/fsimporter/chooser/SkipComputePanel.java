package org.openmicroscopy.shoola.agents.fsimporter.chooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Options panel to display a list of check boxes to enable skip
 * compute flags in importer. Holds state of what options have been
 * selected
 */
class SkipComputePanel extends JPanel {

    /**
     * Text for checkboxes
     */
    private static final String TEXT_THUMBNAIL_GENERATION = "thumbnail generation";
    private static final String TEXT_MIN_MAX_COMPUTE = "min/max compute";
    private static final String TEXT_CHECKSUM_CHECKING = "checksum checking";
    private static final String TEXT_UPGRADE_CHECK = "Upgrade check for Bio-Formats";

    /**
     * Tooltips for checkboxes
     */
    private static final String TEXT_TOOLTIP_THUMBNAIL_SKIP = "Skip generation of thumbnails\n" +
            "Thumbnails will usually be generated when accessing the images post-import via the OMERO clients.";

    private static final String TEXT_TOOLTIP_MIN_MAX_SKIP = "Skip calculation of the minima and maxima pixel values\n" +
            "This option will also skip the calculation of the pixels checksum. Recalculating minima and maxima pixel " +
            "values post-import is currently not supported. See Calculation of minima and maxima pixel " +
            "values for more information.";

    private static final String TEXT_TOOLTIP_CHECKSUM_SKIP = "Skip checksum calculation on image files before and " +
            "after transfer\n" +
            "This option effectively sets the --checksum_algorithm to use a fast algorithm, File-Size-64, that " +
            "considers only file size, not the actual file contents.";

    private static final String TEXT_TOOLTIP_UPGRADE_SKIP = "Skip upgrade check for Bio-Formats";


    private JCheckBox thumbGenCheckbox;
    private JCheckBox minMaxCheckBox;
    private JCheckBox checksumCheckBox;
    private JCheckBox upgradeCheckCheckBox;

    private Map<String, Object> choices = new HashMap<String, Object>();


    public SkipComputePanel() {
        super(new GridLayout(0, 1));

        // Check box options for advanced panel
        thumbGenCheckbox = new JCheckBox(TEXT_THUMBNAIL_GENERATION);
        thumbGenCheckbox.setToolTipText(TEXT_TOOLTIP_THUMBNAIL_SKIP);
        thumbGenCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean doThumbnails = !thumbGenCheckbox.isSelected();
                choices.put("doThumbnails", doThumbnails);
            }
        });
        thumbGenCheckbox.setSelected(false);

        minMaxCheckBox = new JCheckBox(TEXT_MIN_MAX_COMPUTE);
        minMaxCheckBox.setToolTipText(TEXT_TOOLTIP_MIN_MAX_SKIP);
        minMaxCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                choices.put("noStatsInfo", thumbGenCheckbox.isSelected());
            }
        });
        minMaxCheckBox.setSelected(false);

        checksumCheckBox = new JCheckBox(TEXT_CHECKSUM_CHECKING);
        checksumCheckBox.setToolTipText(TEXT_TOOLTIP_CHECKSUM_SKIP);
        checksumCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (thumbGenCheckbox.isSelected()) {
                    choices.put("checksumAlgorithm", "{}");
                } else {
                    choices.put("checksumAlgorithm", "File-Size-64");
                }
            }
        });
        checksumCheckBox.setSelected(false);

        upgradeCheckCheckBox = new JCheckBox(TEXT_UPGRADE_CHECK);
        upgradeCheckCheckBox.setToolTipText(TEXT_TOOLTIP_UPGRADE_SKIP);
        upgradeCheckCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (thumbGenCheckbox.isSelected()) {
                    choices.put("checksumAlgorithm", "{}");
                } else {
                    choices.put("checksumAlgorithm", "");
                }
            }
        });

        add(thumbGenCheckbox);
        add(minMaxCheckBox);
        add(checksumCheckBox);
    }

    public Map<String, Object> getChoices() {
        return choices;
    }
}
