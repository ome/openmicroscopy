/*
 * org.openmicroscopy.shoola.agents.browser.ui.SemanticZoomNode
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
 
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.IconManager;
import org.openmicroscopy.shoola.agents.browser.UIConstants;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;
import org.openmicroscopy.shoola.agents.browser.images.PaintShapeGenerator;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A node that represents the semantically zoomed node that appears when
 * you mouse over a small image.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class SemanticZoomNode extends PImage
                              implements MouseOverSensitive,
                                         MouseDownSensitive
{
    protected Thumbnail parentThumbnail;
    protected Rectangle2D border;
    
    protected Font nameFont = new Font(null,Font.PLAIN,24);
    protected Font multiFont = new Font(null,Font.BOLD,14);
    
    protected Shape prevImageShape = null;
    protected Shape nextImageShape = null;
    
    private static boolean loadedCompositeInfo = false;
    
    private static int compositeWidth = 96; // hardcoded default
    private static int compositeHeight = 96; // hardcoded default
    
    protected Image displayImage;
    
    protected Image openIconImage;
    protected Rectangle2D openIconShape;
    protected Image closeIconImage;
    protected Rectangle2D closeIconShape;
    
    protected Image[] thumbnailImages;
    
    protected boolean multipleModeOn;
    
    /**
     * Makes the node from the specified thumbnail.
     * @param image
     */
    public SemanticZoomNode(Thumbnail parent)
    {
        super(); // TODO change back
        if(!loadedCompositeInfo)
        {
            loadSizeInfo();
        }
        
        if(parent == null)
        {
            throw new IllegalArgumentException("null parent to" +
                "SemanticZoomNode");
        }
        
        multipleModeOn = parent.isMultipleThumbnail();
        
        if(!multipleModeOn)
        {
            Image scaledImage;
            Image originalImage = parent.getImage();
            int width = originalImage.getWidth(null);
            int height = originalImage.getHeight(null);
            
            if(width < height)
        
            {
                scaledImage =
                    originalImage.getScaledInstance(-1,compositeHeight,
                                                    Image.SCALE_DEFAULT);
            }
            else
            {
                scaledImage =
                    originalImage.getScaledInstance(compositeWidth,-1,
                                                    Image.SCALE_DEFAULT);
            }
                                                
            setImage(scaledImage);
        }
        else
        {
            Image[] originalImages = parent.getMultipleImages();
            thumbnailImages = new Image[originalImages.length];
            for(int i=0;i<originalImages.length;i++)
            {
                int width = originalImages[i].getWidth(null);
                int height = originalImages[i].getHeight(null);
                
                if(width < height)
                {
                    thumbnailImages[i] =
                        originalImages[i].getScaledInstance(-1,compositeHeight,
                                                            Image.SCALE_DEFAULT);
                }
                else
                {
                    thumbnailImages[i] =
                        originalImages[i].getScaledInstance(compositeWidth,-1,
                                                            Image.SCALE_DEFAULT);
                }
            }
            setImage(thumbnailImages[parent.getMultipleImageIndex()]);
        }
        
        int xDiff = getImage().getWidth(null)-compositeWidth; // should be only negative...
        int yDiff = getImage().getHeight(null)-compositeHeight; // should be only negative...
        
        // TODO get runnable code for getting composite, showing it
        
        // TODO fix this
        parentThumbnail = parent;
        border = new Rectangle2D.Double(-4,-4,
                                        getImage().getWidth(null)+8,
                                        getImage().getHeight(null)+8);
                                        
        initOverlayShapes();
        setBounds(border);
    }
    
    private void initOverlayShapes()
    {
        int width = getImage().getWidth(null);
        int height = getImage().getHeight(null);
        PaintShapeGenerator generator = PaintShapeGenerator.getInstance();
        
        if(multipleModeOn)
        {
            Shape prevShape = generator.getPrevImageShape();
            Shape nextShape = generator.getNextImageShape();
            
            AffineTransform prevXForm =
                AffineTransform.getTranslateInstance(5,height-prevShape.getBounds2D().getHeight()-5);
            
            AffineTransform nextXForm =
                AffineTransform.getTranslateInstance(width-nextShape.getBounds2D().getWidth()-5,
                                                     height-nextShape.getBounds2D().getHeight()-5);
            
            this.prevImageShape = prevXForm.createTransformedShape(prevShape);
            this.nextImageShape = nextXForm.createTransformedShape(nextShape);
        }
        
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        IconManager manager = env.getIconManager();
        openIconImage = manager.getLargeImage(IconManager.OPEN_IMAGE);
        closeIconImage = manager.getLargeImage(IconManager.CLOSE_IMAGE);
        
        int iconWidth = openIconImage.getWidth(null);
        int iconHeight = openIconImage.getHeight(null);
        
        openIconShape = new Rectangle2D.Double(width-iconWidth-8,50,
                                               iconWidth,iconHeight);
        
        iconWidth = closeIconImage.getWidth(null);
        iconHeight = closeIconImage.getHeight(null);
        
        closeIconShape = new Rectangle2D.Double(width-iconWidth-8,8,
                                                iconWidth,iconHeight);
                                               
              
    }
    
    private static void loadSizeInfo()
    {
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserAgent agent = env.getBrowserAgent();
        int[] dim = agent.getSemanticNodeSize();
        
        if(dim[0] != -1) compositeWidth = dim[0];
        if(dim[1] != -1) compositeHeight = dim[1];
        loadedCompositeInfo = true;
    }
    
    public void loadCompositeImages()
    {
        BrowserEnvironment env = BrowserEnvironment.getInstance();
        BrowserAgent agent = env.getBrowserAgent();
        
        if(!multipleModeOn)
        {
            ThumbnailDataModel model = parentThumbnail.getModel();
            Pixels pix = (Pixels)model.getAttributeMap().getAttribute("Pixels");
            if(pix != null)
            {
                Image image = agent.getResizedThumbnail(pix,compositeWidth,
                                                        compositeHeight);
                if(image != null)
                {
                    setImage(image);
                }
            }
            
            setBounds(border);
        }
        else
        {
            ThumbnailDataModel[] models = parentThumbnail.getMultipleModels();
            for(int i=0;i<models.length;i++)
            {
                Pixels pix =
                    (Pixels)models[i].getAttributeMap().getAttribute("Pixels");
                if(pix != null)
                {
                    thumbnailImages[i] =
                        agent.getResizedThumbnail(pix,compositeWidth,
                                                  compositeHeight);
                }
            }
            Image image = thumbnailImages[parentThumbnail.getMultipleImageIndex()];
            if(image != null)
            {
                setImage(image);
            }
            setBounds(border);
        }
        repaint();
    }
    
    /**
     * Gets the standard width of the semantic node (as loaded in the config
     * file for the browser agent, or 96 by app default)
     * 
     * @return The width of all semantic zoom nodes.
     */
    public static int getStandardWidth()
    {
        if(!loadedCompositeInfo)
        {
            loadSizeInfo();
        }
        return compositeWidth;
    }
    
    /**
     * Gets the standard height of the semantic node (as loaded in the config
     * file for the browser agent, or 96 by app default)
     * 
     * @return
     */
    public static int getStandardHeight()
    {
        if(!loadedCompositeInfo)
        {
            loadSizeInfo();
        }
        return compositeHeight;
    }
    
    /**
     * returns nothing.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#getMouseDownActions()
     */
    public MouseDownActions getMouseDownActions()
    {
        return null;
    }
    
    /**
     * Responds based on overlay location.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseClick(PInputEvent event)
    {
        Point2D pos = event.getPositionRelativeTo(this);
        if(prevImageShape != null)
        {
            if(prevImageShape.contains(pos))
            {
                parentThumbnail.showPreviousImage();
                setImage(thumbnailImages[parentThumbnail.getMultipleImageIndex()]);
                setBounds(border);
                repaint();
                return;
            }
        }
        if(nextImageShape != null)
        {
            if(nextImageShape.contains(pos))
            {
                parentThumbnail.showNextImage();
                setImage(thumbnailImages[parentThumbnail.getMultipleImageIndex()]);
                setBounds(border);
                repaint();
                return;
            }
        }
        if(openIconShape != null)
        {
            if(openIconShape.contains(pos))
            {
                BrowserEnvironment env = BrowserEnvironment.getInstance();
                BrowserAgent agent = env.getBrowserAgent();
                agent.loadImage(parentThumbnail);
                return;
            }
        }
        if(closeIconShape != null)
        {
            if(closeIconShape.contains(pos))
            {
                getParent().removeChild(this);
            }
        }
    }
    
    /**
     * Nothing yet.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseDoubleClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseDoubleClick(PInputEvent event)
    {
        // TODO Auto-generated method stub
    }
    
    /**
     * Doesn't trigger anything.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMousePress(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMousePress(PInputEvent event)
    {
        // don't do nothin'
    }
    
    /**
     * Doesn't trigger anything.
     * 
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseRelease(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseRelease(PInputEvent event)
    {
        // don't do nothin'
    }
    
    /**
     * Doesn't do anything (no overriding)
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#setMouseDownActions(org.openmicroscopy.shoola.agents.browser.events.MouseDownActions)
     */
    public void setMouseDownActions(MouseDownActions actions)
    {
        // don't do nothin'
    }
    
    /**
     * returns nothing.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#getMouseOverActions()
     */
    public MouseOverActions getMouseOverActions()
    {
        return null;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#respondMouseEnter(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseEnter(PInputEvent e)
    {
        // do nothing
    }

    
    /**
     * No setting this-- this is the predefined behavior.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#respondMouseExit(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseExit(PInputEvent e)
    {
        // cheap hack
        try
        {
            getParent().removeChild(this);
        }
        // removal w/o notification possible
        catch(Exception ex) {}
    }
    
    /**
     * does nothing.
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive#setMouseOverActions(org.openmicroscopy.shoola.agents.browser.events.MouseOverActions)
     */
    public void setMouseOverActions(MouseOverActions actions)
    {
        // do nothing
    }
    
    /**
     * Overrides paint() to draw the border, then draw the semantic node.
     *
     */
    public void paint(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        Paint oldPaint = g2.getPaint();
        Font oldFont = g2.getFont();
        Color oldColor = g2.getColor();
        g2.setPaint(Color.yellow);
        g2.fill(border);
        g2.setPaint(oldPaint);
        g2.drawImage(getImage(),0,0,null);
        g2.drawImage(openIconImage,
                     (int)Math.round(openIconShape.getX()),
                     (int)Math.round(openIconShape.getY()),null);
        g2.drawImage(closeIconImage,
                     (int)Math.round(closeIconShape.getX()),
                     (int)Math.round(closeIconShape.getY()),null);
        g2.setFont(nameFont);
        g2.setColor(Color.yellow);
        
        String wellName =
            (String)parentThumbnail.getModel().getValue(UIConstants.WELL_KEY_STRING);
            
        if(wellName != null)
        {
            g2.drawString(wellName,4,26);
        }
        g2.setFont(oldFont);
        g2.setColor(oldColor);
        
        if(multipleModeOn)
        {
            g2.setPaint(Color.yellow);
            g2.setColor(Color.yellow);
            String whichSelected =
                (parentThumbnail.getMultipleImageIndex()+1)
                + "/"
                + (String.valueOf(parentThumbnail.getMultipleImageSize()));
                
            Rectangle2D selectedBounds =
                g2.getFontMetrics(multiFont).getStringBounds(whichSelected,g2);
            
            Rectangle2D totalBounds =
                getBounds().getBounds2D();
            
            g2.setFont(multiFont);
            g2.drawString(whichSelected,
                          (float)((totalBounds.getWidth()-selectedBounds.getWidth())/2),
                          (float)(totalBounds.getHeight()-selectedBounds.getHeight()-5));
            
            g2.fill(prevImageShape);
            g2.fill(nextImageShape);
            g2.setFont(oldFont);
            g2.setPaint(oldPaint);
            g2.setColor(oldColor);
            
        }
        
        Rectangle2D bounds = getBounds().getBounds2D();
        
       /*
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        DrawStyle noteStyle = DrawStyles.ANNOTATION_NODE_STYLE;
        DrawStyle oldStyle = noteStyle.applyStyle(g2);
        PaintShapeGenerator generator = PaintShapeGenerator.getInstance();
        Shape note = generator.getAnnotationNoteShape(width-32,height-38);
        Shape glass = generator.getOuterMagnifierShape(width-32,height-68);
        g2.fill(note);
        g2.draw(note);
        g2.fill(glass);
        g2.draw(glass);
        oldStyle.applyStyle(g2);
        */
    }
}
