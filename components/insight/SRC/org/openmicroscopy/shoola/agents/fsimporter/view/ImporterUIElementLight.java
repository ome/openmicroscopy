/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import omero.gateway.model.TagAnnotationData;

import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI;
import org.openmicroscopy.shoola.agents.fsimporter.util.LightFileImportComponent;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.Status;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

/**
 * Component displaying an import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ImporterUIElementLight extends ImporterUIElement {

    JLabel lTotal = new JLabel("0");
    JLabel lRemaining = new JLabel("0");
    JLabel lError = new JLabel("0");

    Map<Integer, JLabel> stepLabel = new HashMap<Integer, JLabel>();
    Map<Integer, JLabel> stepValues = new HashMap<Integer, JLabel>();

    Map<Integer, Integer> importStatus = new ConcurrentHashMap<Integer, Integer>();
    
    @Override
    FileImportComponentI buildComponent(ImportableFile importable,
            boolean browsable, boolean singleGroup, int index,
            Collection<TagAnnotationData> tags) {
        LightFileImportComponent fc = new LightFileImportComponent(importable,
                getID(), object.getTags());
        
        fc.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (Status.STEP_PROPERTY.equals(name)) {
                    String[] tmp = ((String) evt.getNewValue()).split("_");
                    int id = Integer.parseInt(tmp[0]);
                    int step = Integer.parseInt(tmp[1]);
                    
                    importStatus.put(id, step);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateDisplay();
                        }
                    });
                }
            }
        });
        return fc;
    }

    /**
     * Creates a new instance.
     * 
     * @param controller
     *            Reference to the control. Mustn't be <code>null</code>.
     * @param model
     *            Reference to the model. Mustn't be <code>null</code>.
     * @param view
     *            Reference to the model. Mustn't be <code>null</code>.
     * @param id
     *            The identifier of the component.
     * @param index
     *            The index of the component.
     * @param name
     *            The name of the component.
     * @param object
     *            the object to handle. Mustn't be <code>null</code>.
     */
    ImporterUIElementLight(ImporterControl controller, ImporterModel model,
            ImporterUI view, int id, int index, String name,
            ImportableObject object) {
        super(controller, model, view, id, index, name, object);
        buildGUI();
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI() {
        for (int i = 1; i < 7; i++) {
            JLabel l = new JLabel(Status.STEPS.get(i));
            stepLabel.put(i, l);
            stepValues.put(i, new JLabel("0"));
        }

        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setBorder(new LineBorder(Color.LIGHT_GRAY));

        info.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        c.gridx = 0;
        c.gridy = 0;
        info.add(boldLabel("Total images"), c);
        c.gridx = 1;
        info.add(lTotal, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(10, 2, 2, 2);
        info.add(boldLabel("Import Queue"), c);
        c.gridx = 1;
        info.add(lRemaining, c);
        
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.insets = new Insets(10, 2, 2, 2);
        info.add(boldLabel("Processing Queues"), c);
        c.gridwidth = 1;
        c.insets = new Insets(2, 2, 2, 2);
        
        for (int i = 1; i < 6; i++) {
            c.gridx = 0;
            c.gridy = i+2;
            info.add(stepLabel.get(i), c);
            c.gridx = 1;
            info.add(stepValues.get(i), c);
        }
        
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.insets = new Insets(10, 2, 2, 2);
        info.add(boldLabel("Result"), c);
        c.gridwidth = 1;
        c.insets = new Insets(2, 2, 2, 2);

        c.gridx = 0;
        c.gridy = 9;
        info.add(stepLabel.get(6), c);
        c.gridx = 1;
        info.add(stepValues.get(6), c);
        
        c.gridx = 0;
        c.gridy = 10;
        info.add(new JLabel("Errors"), c);
        c.gridx = 1;
        info.add(lError, c);

        add(info, BorderLayout.CENTER);
    }

    private void updateDisplay() {
        lTotal.setText("" + super.totalToImport);
        int complete = 0;
        for (int i = 1; i < 7; i++) {
            int c = 0;
            for (int step : importStatus.values()) {
                if (i == step)
                    c++;
            }
            stepValues.get(i).setText("" + c);
            if(i == 6)
                complete = c;
        }
        lError.setText("" + super.countFailure);
        lRemaining.setText(""+(super.totalToImport-super.countFailure-complete));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (FileImportComponentI.IMPORT_FILES_NUMBER_PROPERTY.equals(name)) {
            // -1 to remove the entry for the folder.
            Integer v = (Integer) evt.getNewValue() - 1;
            totalToImport += v;
            setNumberOfImport();
        }
        updateDisplay();
    }

}
