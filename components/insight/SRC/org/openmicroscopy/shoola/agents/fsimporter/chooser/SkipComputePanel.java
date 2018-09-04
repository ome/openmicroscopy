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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.env.data.model.ImportableObject;

/**
 * Options panel to display a list of check boxes to enable skip
 * compute flags in importer. Holds state of what options have been
 * selected
 * 
 * @author  Riad Gozim &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:r.gozim@dundee.ac.uk">r.gozim@dundee.ac.uk</a>
 * 
 */
class SkipComputePanel extends JPanel {

    /**
     * Text for checkboxes
     */
    private static final String TEXT_THUMBNAIL_GENERATION = "Thumbnail generation";
    private static final String TEXT_MIN_MAX_COMPUTE = "Min/max calculation";
    private static final String TEXT_CHECKSUM_CHECKING = "Checksum verification";

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
                choices.put(ImportableObject.OPTION_THUMBNAILS, doThumbnails);
            }
        });
        thumbGenCheckbox.setSelected(false);

        minMaxCheckBox = new JCheckBox(TEXT_MIN_MAX_COMPUTE);
        minMaxCheckBox.setToolTipText(TEXT_TOOLTIP_MIN_MAX_SKIP);
        minMaxCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                choices.put(ImportableObject.OPTION_SKIP_MINMAX, minMaxCheckBox.isSelected());
            }
        });
        minMaxCheckBox.setSelected(false);

        checksumCheckBox = new JCheckBox(TEXT_CHECKSUM_CHECKING);
        checksumCheckBox.setToolTipText(TEXT_TOOLTIP_CHECKSUM_SKIP);
        checksumCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (checksumCheckBox.isSelected()) {
                    choices.put(ImportableObject.OPTION_CHECKSUMS, omero.model.enums.ChecksumAlgorithmFileSize64.value); // File-Size-64 fast
                } else {
                    choices.put(ImportableObject.OPTION_CHECKSUMS, omero.model.enums.ChecksumAlgorithmSHA1160.value);  // default SHA1-160
                }
            }
        });
        checksumCheckBox.setSelected(false);

        add(thumbGenCheckbox);
        add(minMaxCheckBox);
        add(checksumCheckBox);
    }
    
    /**
     * Get the advanced import configuration choices
     * @return See above
     */
    public Map<String, Object> getChoices() {
        return choices;
    }
}
