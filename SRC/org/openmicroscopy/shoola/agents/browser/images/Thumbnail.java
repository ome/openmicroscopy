/*
 * org.openmicroscopy.shoola.agents.browser.images.Thumbnail
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.images;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A view of a thumbnail/small image within the browser framework.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class Thumbnail extends PImage implements MouseDownSensitive,
                                                 MouseOverSensitive
{
    /**
     * Indicates that the overlay should be drawn in the foreground.
     */
    public static final int BACKGROUND_PAINT_METHOD = 1;
    
    /**
     * Indicates that the overlay should be drawn underneath other overlays.
     */
    public static final int MIDDLE_PAINT_METHOD = 2;
    
    /**
     * Indicates that the overlay should be drawn in the foreground.
     */
    public static final int FOREGROUND_PAINT_METHOD = 3;
    
    /**
     * The base model of the thumbnail.
     */
    protected ThumbnailDataModel model;

    /**
     * Defines the list of paint methods to be executed prior to drawing
     * the image.
     */
    protected List backgroundPaintMethods;
    
    /**
     * Defines the list of paint methods to be executed prior to drawing the
     * overlays, but after drawing the image.
     */
    protected List middlePaintMethods;
    
    /**
     * Defines the list of paint methods to be executed after drawing the
     * overlays and image.
     */
    protected List foregroundPaintMethods;
    
    /**
     * The order in which thumbnail-specific overlays should be drawn.
     * Normally, a browser should call setPaintMethodZOrder() to establish the
     * z-ordering of the paint methods.
     */    
    protected PaintMethodZOrder defaultZOrder;

    /**
     * Defines if this thumbnail is mip-mapped.
     */
    protected boolean usesImageSet;
    
    /**
     * Defines the mouse down actions for this thumbnail.
     */
    protected MouseDownActions mouseDownActions;
    
    /**
     * Defines the mouse over actions for this thumbnail.
     */
    protected MouseOverActions mouseOverActions;
    
    /**
     * Defines a list of multiple thumbnails per site, if applicable.
     */
    protected List multipleThumbnailList;
    
    /**
     * Defines a list of multiple models per site, if applicable.
     */
    protected List multipleModelList;
    
    /**
     * Indicates that this thumbnail has multiple images.
     */
    protected boolean hasMultipleImages = false;
    
    /**
     * Indicates the selected image/model index (for multiple-site images)
     */
    protected int multipleSelectedIndex = 0;
    
    /**
     * Gets the lowest (or only) ID of all the images in the thumbnail.
     */
    protected int baseID;
    
    private Color overColor = new Color(192,192,192);
    
    protected Map activeOverlayNodes;
    
    protected List activeOverlayMethods;

    /**
     * Initializes the thumbnail.
     */
    protected void init()
    {
        backgroundPaintMethods = new ArrayList();
        middlePaintMethods = new ArrayList();
        foregroundPaintMethods = new ArrayList();
        defaultZOrder = new PaintMethodZOrder();
        mouseDownActions = new MouseDownActions();
        mouseOverActions = new MouseOverActions();
        activeOverlayNodes = new HashMap();
        activeOverlayMethods = new ArrayList();
    }
    
    /**
     * Initializes the thumbnail in multiple mode.
     */
    protected void initMultiple(Image[] images, ThumbnailDataModel[] models)
    {
        hasMultipleImages = true;
        multipleThumbnailList = Arrays.asList(images);
        multipleModelList = Arrays.asList(models);
    }
    
    /**
     * Constructs a thumbnail around this model (no renderer specified yet)
     * @param tdm The data model.
     */
    public Thumbnail(ThumbnailDataModel tdm)
    {
        super();
        init();
        setAccelerated(true);
        this.model = tdm;
        baseID = tdm.getID();
    }
    
    /**
     * Constructs a thumbnail with the corresponding backing model and the
     * specified image.
     * @param thumbImage The image of the thumbnail.
     * @param tdm The data model behind the thumbnail.
     */
    public Thumbnail(Image thumbImage, ThumbnailDataModel tdm)
    {
        super(thumbImage,false);
        init();
        this.model = tdm;
        baseID = tdm.getID();
    }
    
    /**
     * Constructs a thumbnail with multiple backing images (for example, in
     * a well layout with more than one image or set of pixels per location).
     * The first image will be the default selected image.
     * 
     * @param thumbImages The images for the thumbnail location
     * @param models The models for the respective images.
     */
    public Thumbnail(Image[] thumbImages, ThumbnailDataModel[] models)
    {
        super();
        if(thumbImages == null || models == null
           || thumbImages.length == 0 || models.length == 0)
        {
            throw new IllegalArgumentException("Parameters cannot be null or empty");
        }
        else if(thumbImages.length != models.length)
        {
            throw new IllegalArgumentException("Parameters must be of equal length");
        }
        setImage(thumbImages[0]);
        init();
        initMultiple(thumbImages,models);
        setModel(models[0]);
        
        baseID = Integer.MAX_VALUE;
        for(int i=0;i<models.length;i++)
        {
            if(models[i].getID() < baseID)
            {
               baseID = models[i].getID();
            }
        }
    }
    
    public int getBaseID()
    {
        return baseID;
    }

    /**
     * Gets the underlying model for the thumbnail.
     * 
     * @return This thumbnail's underlying model.
     */
    public ThumbnailDataModel getModel()
    {
        return model;
    }

    /**
     * Sets the underlying model for the thumbnail.
     * 
     * @param model This thumbnail's new data model.
     */
    public void setModel(ThumbnailDataModel model)
    {
        if(model != null)
        {
            this.model = model;
        }
    }
    
    /**
     * Sets the set and ordering of paint methods to the specified
     * order.  These paint methods (for overlays) will always be executed
     * first.
     * 
     * @param order The set and order of paint methods 
     */
    public void setPaintMethodZOrder(PaintMethodZOrder order)
    {
        this.defaultZOrder = order;
        repaint();
    }
    
    /**
     * Adds this paint method to the list of overlays/paint methods to
     * be executed, in addition to the overlays specified in the Z order.
     * This paint method will be executed before the image is drawn.
     * Preferred for per-image or per-group drawing methods (such as selection)
     * where the thumbnail cannot infer its own state in an external context.
     * 
     * @param p The PaintMethod to add to the background.
     */
    public void addBackgroundPaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            backgroundPaintMethods.add(p);
            repaint();
        }
    }
    
    /**
     * Removes this paint method from the list of background paint methods to
     * be executed.
     * @param p The paint method to remove.
     */
    public void removeBackgroundPaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            backgroundPaintMethods.remove(p);
            repaint();
        }
    }
    
    /**
     * Adds this paint method to the list of overlays/paint methods to
     * be executed, in addition to the overlays specified in the Z order.
     * This paint method will be executed after the image is drawn, but
     * before the overlays.  Preferred for per-image or per-group drawing
     * methods (such as in response to thumbnail selection) where the thumbnail
     * cannot infer its own state in an external context.
     * 
     * @param p The PaintMethod to add between the image and standard overlays.
     */
    public void addMiddlePaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            middlePaintMethods.add(p);
            repaint();
        }
    }
    
    /**
     * Removes this paint method from the list of overlays/paint methods to
     * be executed.
     * @param p The paint method to remove.
     */
    public void removeMiddlePaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            middlePaintMethods.remove(p);
            repaint();
        }
    }
    
    
    /**
     * Adds this paint method to the list of overlays/paint methods to be
     * added, in addition to the overlays specified in the Z order. This
     * paint method will be executed after the image and overlays are
     * drawn.
     * 
     * @param p The PaintMethod to add in front of the image and standard
     *          overlays.
     */
    public void addForegroundPaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            foregroundPaintMethods.add(p);
            repaint();
        }
    }
    
    /**
     * Removes this paint method from the list of overlays/paint methods to
     * be executed after all paint methods are complete.
     * @param p The paint method to remove.
     */
    public void removeForegroundPaintMethod(PaintMethod p)
    {
        if(p != null)
        {
            foregroundPaintMethods.remove(p);
            repaint();
        }
    }
    
    /**
     * Returns whether or not this thumbnail contains multiple images.
     * @return See above.
     */
    public boolean isMultipleThumbnail()
    {
        return hasMultipleImages;
    }
    
    /**
     * Returns how many images this thumbnail has (in multiple mode).  If
     * this thumbnail does not have multiple images, then the method will
     * return -1.
     * 
     * @return The number of images this thumbnail has (in multi-mode)
     */
    public int getMultipleCount()
    {
        if(!hasMultipleImages)
        {
            return -1;
        }
        return multipleThumbnailList.size();
    }
    
    /**
     * Returns all images in the thumbnail (if it's a multiple image)
     * @return An array of images in the thumbnail.
     */
    public Image[] getMultipleImages()
    {
        Image[] images = new Image[multipleThumbnailList.size()];
        multipleThumbnailList.toArray(images);
        return images;
    }
    
    /**
     * Returns all backing image models in the thumbnail (if it's a multiple
     * image)
     * @return An array of models in the thumbnail.
     */
    public ThumbnailDataModel[] getMultipleModels()
    {
        ThumbnailDataModel[] models =
            new ThumbnailDataModel[multipleModelList.size()];
        multipleModelList.toArray(models);
        return models;
    }
    
    /**
     * Returns the current index of the selected image, in multiple
     * mode.
     * 
     * @return See above.
     */
    public int getMultipleImageIndex()
    {
        return multipleSelectedIndex;
    }
    
    /**
     * Returns the size of the image collection (in multiple mode).  If this
     * is not a multiple image, this will return 1.
     * @return See above.
     */
    public int getMultipleImageSize()
    {
        if(hasMultipleImages)
        {
            return multipleThumbnailList.size();
        }
        else return 1;
    }
    
    /**
     * Sets the visible image (in multiple mode) to the selected value.
     * @param index The index of the image to make visible.
     */
    public void setMultipleImageIndex(int index)
    {
        if(!hasMultipleImages) return;
        else
        {
            if(index < 0 || index >= multipleThumbnailList.size())
            {
                return;
            }
            else
            {
                multipleSelectedIndex = index;
                setImage((Image)multipleThumbnailList.get(index));
                setModel((ThumbnailDataModel)multipleModelList.get(index));
                repaint();
            }
        }
    }
    
    /**
     * In multiple mode, go to the next image in the selection.  Will wrap
     * around to the first if the last image is currently selected.
     */
    public void showNextImage()
    {
        if(!hasMultipleImages)
        {
            return;
        }
        else
        {
            if(multipleSelectedIndex == multipleThumbnailList.size()-1)
            {
                setMultipleImageIndex(0);
            }
            else
            {
                setMultipleImageIndex(++multipleSelectedIndex);
            }
        }
    }
    
    /**
     * In multiple mode, go to the previous image in the selection.  Will wrap
     * around to the last if the first image is currently selected.
     *
     */
    public void showPreviousImage()
    {
        if(!hasMultipleImages)
        {
            return;
        }
        else
        {
            if(multipleSelectedIndex == 0)
            {
                setMultipleImageIndex(multipleThumbnailList.size()-1);
            }
            else
            {
                setMultipleImageIndex(--multipleSelectedIndex);
            }
        }
    }
    
    /**
     * Paints the Thumbnail and all its overlays.
     * @param context The Piccolo paint context.
     */
    public void paint(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        
        // paint the background overlays
        for(Iterator iter = backgroundPaintMethods.iterator(); iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(context,this);
        }
        
        // now draw the image (methinks this will work)
        // the children (overlay nodes) may or may not be painted here;
        // I can't tell, we shall see
        
        // now, time to iterator through the overlay methods and see if
        // anything needs updating
        for(Iterator iter = activeOverlayMethods.iterator(); iter.hasNext();)
        {
            OverlayMethod method = (OverlayMethod)iter.next();
            method.display(this,context);
        }
        
        super.paint(context);
        
        if(hasMultipleImages)
        {
            PaintShapeGenerator psg = PaintShapeGenerator.getInstance();
            Shape rightFold = psg.getFoldUpperShape();
            DrawStyle backgroundStyle =
                new DrawStyle(null,overColor,null);
            
            DrawStyle oldStyle = backgroundStyle.applyStyle(g2);
            AffineTransform shiftXForm =
                AffineTransform.getTranslateInstance(getWidth()-11,-1);
            g2.fill(shiftXForm.createTransformedShape(rightFold));
            
            Color c = (Color)backgroundStyle.getFillPaint();
            Paint p = new Color(c.getRed(),c.getGreen(),c.getBlue(),153);
            backgroundStyle.setFillPaint(p);
            backgroundStyle.applyStyle(g2);
            
            shiftXForm =
                AffineTransform.getTranslateInstance(getWidth()-10,0);
            
            Shape leftFold = psg.getFoldLowerShape();
            g2.fill(shiftXForm.createTransformedShape(leftFold));
            oldStyle.applyStyle(g2);
        }
        
        // now draw the middle paint methods
        for(Iterator iter = middlePaintMethods.iterator(); iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(context,this);
        }
        
        // now draw the default overlays
        for(Iterator iter = defaultZOrder.getMethodOrder().iterator();
            iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(context,this);
        }
        
        // now draw the foreground paint methods
        for(Iterator iter = foregroundPaintMethods.iterator(); iter.hasNext();)
        {
            PaintMethod p = (PaintMethod)iter.next();
            p.paint(context,this);
        }
    }
    
    /**
     * Adds an overlay method (determines whether an overlay node that can
     * respond to UI events should be added atop the thumbnail) to this
     * thumbnail.
     * @param method The method to add.
     */
    public void addOverlayMethod(OverlayMethod method)
    {
        if(method != null)
        {
            activeOverlayMethods.add(method);
        }
    }
    
    /**
     * Removes an overlay method.
     * @param method
     */
    public void removeOverlayMethod(OverlayMethod method)
    {
        if(method != null)
        {
            activeOverlayMethods.remove(method);
            removeActiveOverlay(method.getDisplayNodeType());
        }
    }
    
    /**
     * Add a node that responds to user input.
     * @param node
     */
    public void addActiveOverlay(OverlayNode node)
    {
        String nodeType = node.getOverlayType();
        activeOverlayNodes.put(nodeType,node);
        addChild(node);
        repaint();
    }
    
    /**
     * Returns whether or not the thumbnail is displaying an overlay
     * node with a specific type.  Maybe should consider switching to
     * basing keys on class, not on strings.
     * 
     * @param nodeType The type of the overlay node to check.
     * @return Whether or not a node of the specified type is already a child
     *         of this thumbnail.
     */
    public boolean hasActiveOverlay(String nodeType)
    {
        return activeOverlayNodes.containsKey(nodeType);
    }
    
    /**
     * Remove the node (type) that responds to user input.
     * @param overlayType
     */
    public void removeActiveOverlay(String overlayType)
    {
        OverlayNode node =
            (OverlayNode)activeOverlayNodes.remove(overlayType);
        if(node != null && node.isDescendentOf(this))
        {
            removeChild(node);
        }
        repaint();
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#getMouseDownActions()
     */
    public MouseDownActions getMouseDownActions()
    {
        return mouseDownActions;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#getMouseOverActions()
     */
    public MouseOverActions getMouseOverActions()
    {
        return mouseOverActions;
    }
    
    /**
     * <b>NOTE:</b> Will do nothing if <code>actions</code> is null.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#setMouseDownActions(org.openmicroscopy.shoola.agents.browser.events.MouseDownActions)
     */
    public void setMouseDownActions(MouseDownActions actions)
    {
        if(actions == null)
        {
            return;
        }
        else
        {
            mouseDownActions = actions;
        }
    }
    
    /**
     * <b>NOTE:</b> Will do nothign if <code>actions<code> is null.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#setMouseOverActions(org.openmicroscopy.shoola.agents.browser.events.MouseOverActions)
     */
    public void setMouseOverActions(MouseOverActions actions)
    {
        if(actions == null)
        {
            return;
        }
        else
        {
            mouseOverActions = actions;
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseClick(PInputEvent e)
    {
        PiccoloAction action =
            mouseDownActions.getMouseClickAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseDoubleClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseDoubleClick(PInputEvent e)
    {
        PiccoloAction action =
            mouseDownActions.getDoubleClickAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }

    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMousePress(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMousePress(PInputEvent e)
    {
        PiccoloAction action =
             mouseDownActions.getMousePressAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseRelease(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseRelease(PInputEvent e)
    {
        PiccoloAction action =
            mouseDownActions.getMouseReleaseAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#respondMouseEnter(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseEnter(PInputEvent e)
    {
        PiccoloAction action =
            mouseOverActions.getMouseEnterAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#respondMouseExit(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseExit(PInputEvent e)
    {
        PiccoloAction action =
            mouseOverActions.getMouseExitAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }
}
