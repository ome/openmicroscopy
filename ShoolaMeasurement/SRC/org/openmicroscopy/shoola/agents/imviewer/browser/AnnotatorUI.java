/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.AnnotatorUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorFactory;

/** 
 * UI component hosting the annotator and the canvas displaying a smaller
 * version of the original image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AnnotatorUI 
	extends JPanel
{

	/** Canvas displaying a smaller version of the rendered image. */
	private AnnotatorCanvas 	canvas;
	
	/** Reference to the Model. */
    private BrowserModel		model;
    
    /** Reference to the Control. */
    private BrowserControl		controller;
    
    /** Reference to the editor. */
    private AnnotatorEditor 	editor;

    /** UI component hosting the info about the image. */
    private InfoPane			infoPane;
    
    /** The listener listening to the canvas. */
    private ImageCanvasListener	canvasListener;
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new BorderLayout(0, 0));
    	JPanel p = new JPanel();
    	double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, 
				{TableLayout.PREFERRED, 5, TableLayout.FILL}};
        p.setLayout(new TableLayout(tl));
        p.add(canvas, "0, 0");
        p.add(infoPane, "2, 0");
        p.add(new JSeparator(), "0, 2, 2, 2");
    	add(p, BorderLayout.NORTH);
    	add(editor.getUI(), BorderLayout.CENTER);
    }
    
    /** Creates a new instance. */
	AnnotatorUI() {}

	/**
	 * Links the components.
	 * 
	 * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
	 * @param model 		Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 */
	void initialize(BrowserControl controller, BrowserModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		if (controller == null) throw new NullPointerException("No control.");
		canvas = new AnnotatorCanvas(model);
		editor = AnnotatorFactory.getEditor(ImViewerAgent.getRegistry(), 
									model.getImageData(), 
									AnnotatorEditor.HORIZONTAL_LAYOUT);
		infoPane = new InfoPane(model);
		canvasListener = new ImageCanvasListener(model, canvas);
		editor.addPropertyChangeListener(controller);
		buildGUI();
	}

	/**
     * Paints the annotated image.
     * This method should be called straight after setting the 
     * rendered image.
     */
    void paintImage()
    {
        if (model.getRenderedImage() == null) return;
        BufferedImage img = model.getAnnotateImage();
        int w = img.getWidth();
        int h = img.getHeight();
        canvasListener.setAreaSize(w, h);
        canvas.setPreferredSize(new Dimension(w, h));
        canvas.repaint();
    }
    
	/** Retrieves the annotations linked to the viewed image. */
	void activateEditor()
	{
		if (editor != null && !editor.hasTextEntered()) editor.activate();
		if (infoPane != null) infoPane.buildGUI();
	}

	/**
	 * Returns <code>true</code> if the current user entered some
	 * textual annotation, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAnnotationToSave()
	{
		if (editor == null) return false;
		return editor.hasTextEntered();
	}

	/**
	 * Forwards call to the {@link AnnotatorEditor} to save the annotation 
	 * before closing the component.
	 */
	void saveAnnotation()
	{
		if (editor == null) return;
		editor.save();
		
	}
	
}
