/*
 * org.openmicroscopy.shoola.agents.hiviewer.tpane.TitleBar
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.hiviewer.tpane;




//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies


/** 
 * A versatile title bar UI for the {@link TinyPane}.
 * This component morphs in any of the title bars specified by the constants 
 * in {@link TinyPane} every time a title bar type is specified. The title
 * bar is logically divided in two areas.  The left area contains any buttons
 * and icons required by the current title bar type.  All the space on the
 * right of the button/icon area is used to draw the frame's title.  This 
 * component has no borders so to keep its area as small as possible.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TitleBar
    extends JComponent
    implements PropertyChangeListener
{
    
    /** 
     * Paints the background when the frame is not highlighted.
     * It's stateless, so we share it.
     */
    private static final Painter NORMAL_PAINTER = new BgPainter();
    
    /** Horizontal space, in pixels, around a component. */
    static final int    H_SPACING = 2;
    
    /** The minimum width, in pixels, the title bar can be shrunk to. */
    static final int    MIN_WIDTH = 48;
    

    /** 
     * Is in charge of painting the background.
     * Will be a different object depending on whether the frame is
     * highlighted.
     */
    private Painter         bgPainter;
    
    /** 
     * The icon in the title bar.
     * It may be <code>null</code>, depending on the title bar type. 
     */
    private TinyPaneIcon   icon;
    
    /** 
     * The button the user presses to collapse/expand the frame.
     * It may be <code>null</code>, depending on the title bar type. 
     */
    private SizeButton      sizeButton;
    
    /**
     * The button that lets users switch between multi and single-view mode.
     * It may be <code>null</code>, depending on the title bar type.
     */
    private ViewModeButton  viewModeButton;
    
    /** 
     * Paints the title string.
     * It may be <code>null</code>, depending on the title bar type. 
     */
    private TinyPaneTitle   title;
    
    /** 
     * The height of the title bar.
     * This value depends on the current title bar type, but is fixed for
     * each type.  In fact, a title bar can't be resized along the <i>y</i>
     * axis.
     */
    private int             fixedHeight;
    
    /** The Model this title bar is for. */
    private TinyPane        model;
    
    /**
     * Asks all current sub-components to register with the Model.
     * Called after switching to a different title bar type.
     */
    private void attachAll()
    {
        if (icon != null) {
            icon.attach();
            //new MouseInputForwarder(icon, this);
        }
        if (sizeButton != null) sizeButton.attach();
        if (viewModeButton != null) viewModeButton.attach();
        if (title != null) {
            title.attach();
            //new MouseInputForwarder(title, this);
        }
    }
    
    /**
     * Gets rid of all current sub-components.
     * Called right before switching to a different title bar type. 
     */
    private void detachAll()
    {
        TinyObserver[] comp = new TinyObserver[] {sizeButton, title};
        for (int i = 0; i < comp.length; ++i)
            if (comp[i] != null) {
                comp[i].detach();
                remove((Component) comp[i]);
            }
    }
    
    /**
     * Updates the title bar to the new highlight in the Model.
     * 
     * @param highlightColor The highlight color if a request to highlight the
     *                       frame was made, or <code>null</code> if the frame
     *                       is in normal mode.
     */
    private void update(Color highlightColor) 
    {
        if (highlightColor == null)  //No highlighting required, normal mode.
            bgPainter = NORMAL_PAINTER;
        else bgPainter = new HiBgPainter(highlightColor);  //Highlight bg.
        repaint();
    }
    
    /**
     * Updates the title bar to the new title bar type in the Model.
     * 
     * @param titleBarType The title bar type.
     */
    private void update(int titleBarType)
    {
        detachAll();
        removeAll();
        switch (titleBarType) {
            case TinyPane.NO_BAR:
                fixedHeight = 0;
                break;
            case TinyPane.HEADER_BAR:
                fixedHeight = 4;
                break;
            case TinyPane.SMALL_BAR:
                fixedHeight = 12;
                sizeButton = new SizeButton(model);
                add(sizeButton);
                title = new TinyPaneTitle(model);
                add(title);
                break;
            case TinyPane.FULL_BAR:
                fixedHeight = 18;
                icon = new TinyPaneIcon(model);
                add(icon);
                viewModeButton = new ViewModeButton(model);
                add(viewModeButton);
                sizeButton = new SizeButton(model);
                add(sizeButton);
                title = new TinyPaneTitle(model);
                add(title);
                break;
            case TinyPane.STATIC_BAR:
                fixedHeight = 18;
                icon = new TinyPaneIcon(model);
                add(icon);
                title = new TinyPaneTitle(model);
                add(title);
                break;
        }
        attachAll();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model The Model this title bar is for.
     *              Mustn't be <code>null</code>.
     */
    TitleBar(TinyPane model) 
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        model.addPropertyChangeListener(this);
        update(model.getHighlight());
        update(model.getTitleBarType());
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new TitleBarLayout());
    }
    
    /**
     * Returns the fixed height of the title bar.
     * This value depends on the current title bar type, but is fixed for
     * each type.  In fact, a title bar can't be resized along the <i>y</i>
     * axis.
     * 
     * @return See above.
     */
    int getFixedHeight() { return fixedHeight; }
    
    /** 
     * Overridden to do custom painting required for this component. 
     * @see JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)  
    {   
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.WHITE);
        g2D.fillRect(0, 0, getWidth(), getHeight());
        bgPainter.paint(g2D, new Rectangle(0, 0, getWidth(), getHeight()));
    }
    
    /**
     * Updates the display every time the the Model's title bar type or
     * highlight change.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propName = pce.getPropertyName();
        if (TinyPane.HIGHLIGHT_PROPERTY.equals(propName))
            update((Color) pce.getNewValue());
        else if (TinyPane.TITLEBAR_TYPE_PROPERTY.equals(propName))
            update(((Integer) pce.getNewValue()).intValue());
    }

}
