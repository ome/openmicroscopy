package org.openmicroscopy.shoola.agents.fsimporter.chooser;

import javax.swing.*;
import java.awt.*;

/**
 * Options panel to display a list of check boxes to enable skip
 * compute flags in importer. Holds state of what options have been
 * selected
 */
class SkipComputePanel extends JPanel {

	private static final String TEXT_THUMBNAIL_GENERATION = "thumbnail generation";
	private static final String TEXT_MIN_MAX_GENERATION = "min/max compute";
	private static final String TEXT_CHECKSUM_GENERATION = "checksum checking";

	private JCheckBox thumbGenCheckbox;
	private JCheckBox minMaxCheckBox;
	private JCheckBox checksumCheckBox;

	// Store state of check boxs here

	public SkipComputePanel() {
		super(new GridLayout(0, 1));

		// Check box options for advanced panel
		thumbGenCheckbox = new JCheckBox(TEXT_THUMBNAIL_GENERATION);
		thumbGenCheckbox.setToolTipText("Skips the generation of thumbnails on the server" +
				" saving time taken to import images");
		thumbGenCheckbox.addItemListener(e -> {

		});
		minMaxCheckBox = new JCheckBox(TEXT_MIN_MAX_GENERATION);
		thumbGenCheckbox.setToolTipText("Skips the calculation of min and max of color values on the server" +
				" saving time taken to import images");
		minMaxCheckBox.addItemListener(e -> {

		});
		checksumCheckBox = new JCheckBox(TEXT_CHECKSUM_GENERATION);
		checksumCheckBox.setToolTipText("Skips the calculation of min and max of color values on the server" +
				" saving time taken to import images");
		checksumCheckBox.addItemListener(e -> {

		});

		add(thumbGenCheckbox);
		add(minMaxCheckBox);
		add(checksumCheckBox);
	}

	// Get state of check boxes here

}
