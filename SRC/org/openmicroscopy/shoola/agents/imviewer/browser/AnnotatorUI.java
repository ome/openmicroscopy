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
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
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
    
    /** The UI component hosting the {@link AnnotatorCanvas}. */
    private JLayeredPane		layeredPane;
    
    /** The UI component layed out on the left of the image. */
    private JComponent 			left;
    
    /** The width of the left component. */
    private int					leftWidth;
    
    /** The listener listening to the canvas. */
    private ImageCanvasListener	canvasListener;
    
    /** 
     * Initializes the components composing the display. 
     * 
     * @param view	Reference to the View.
     */
    private void initComponents(BrowserUI view)
    {
        layeredPane = new JLayeredPane();
        canvas = new AnnotatorCanvas(model);
		editor = AnnotatorFactory.getEditor(ImViewerAgent.getRegistry(), 
									model.getImageData(), 
									AnnotatorEditor.HORIZONTAL_LAYOUT);
		infoPane = new InfoPane(model);
		canvasListener = new ImageCanvasListener(view, model, canvas);
		editor.addPropertyChangeListener(controller);
        //The image canvas is always at the bottom of the pile.
        //layeredPane.setLayout(new BorderLayout(0, 0));
        layeredPane.add(canvas, new Integer(0));
    }
    
    /** Creates a new instance. */
	AnnotatorUI() {}

	/**
	 * Links the components.
	 * 
	 * @param view			Reference to the View.
     *                      Mustn't be <code>null</code>.
	 * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
	 * @param model 		Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 */
	void initialize(BrowserUI view, BrowserControl controller, 
					BrowserModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		if (controller == null) throw new NullPointerException("No control.");
		if (view == null) throw new NullPointerException("No View.");
		this.controller = controller;
		this.model = model;
		initComponents(view);
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
        Dimension d = new Dimension(w, h);
		layeredPane.setPreferredSize(d);
        layeredPane.setSize(d);
        canvas.setPreferredSize(d);
        canvas.setSize(d);
        canvas.repaint();
        left.setPreferredSize(new Dimension(leftWidth, h));
    }
    
	/** Retrieves the annotations linked to the viewed image. */
	void activateEditor()
	{
		//if (editor != null && !editor.hasTextEntered()) editor.activate();
		if (editor != null) editor.activate();
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
	
	/**
     * Adds the component to the {@link #layeredPane}. The component will
     * be added to the top of the pile
     * 
     * @param c The component to add.
     */
    void addComponentToLayer(JComponent c)
    {
        layeredPane.add(c, new Integer(1));
    }
    
    /**
     * Removes the component from the {@link #layeredPane}.
     * 
     * @param c The component to remove.
     */
    void removeComponentFromLayer(JComponent c)
    {
        layeredPane.remove(c);
    }

    /**
     * Adds the passes component to the {@link #imagePanel}. 
     * 
     * @param left		The component to add to the left of the image.
     * @param bottom	The component to add to the bottom of the image.
     */
    void buildGUI(JComponent left, JComponent bottom)
    {
    	setLayout(new BorderLayout(0, 0));
    	JPanel imagePanel = new JPanel();
    	double[][] tl = {{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 
    						TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 5, 
    							TableLayout.FILL}};
    	imagePanel.setLayout(new TableLayout(tl));
    	this.left = left;
    	leftWidth = left.getPreferredSize().width;
    	left.setPreferredSize(layeredPane.getPreferredSize());
    	imagePanel.add(left, "0, 0");
    	imagePanel.add(layeredPane, "1, 0");
        imagePanel.add(bottom, "1, 1");
    	imagePanel.add(new JSeparator(), "0, 3, 3, 3");
    	add(imagePanel, BorderLayout.NORTH);
    	add(editor.getUI(), BorderLayout.CENTER);
    }
    
}
