/*
 * org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory
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
 
package org.openmicroscopy.shoola.agents.browser.events;

import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserView;
import org.openmicroscopy.shoola.agents.browser.ui.HoverManager;
import org.openmicroscopy.shoola.agents.browser.ui.SemanticZoomNode;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Factory classes for commonly used browser actions that require some form
 * of composition to work properly.  Which is like, all of them.  Whooooops.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PiccoloActionFactory
{
    /**
     * Generates a mode change action.
     * @param target The browser model to change.
     * @param mode The mode to change the model to.
     * @return The action which executes this, which can be applied to any
     *         node.
     */
    public static PiccoloAction getModeChangeAction(final BrowserModel target,
                                                    final String familyName,
                                                    final BrowserMode mode)
    {
        if(target == null || mode == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.setCurrentMode(familyName,mode);
            }

        };
        return action;
    }
    
    /**
     * Generates an add paint method action.
     * @param target The browser model to change.
     * @param method The (overlay) paint method to add.
     * @param location The layer on which to add the paint method.
     * @return The action which executes this paint method inclusion.
     */
    public static PiccoloAction getAddPaintMethodAction(final BrowserModel target,
                                                        final PaintMethod method,
                                                        final int location)
    {
        if(target == null || method == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.addPaintMethod(method,location);
            }
        };
        return action;
    }
    
    /**
     * Generates a remove paint method action.
     * @param target The browser model to change.
     * @param method The (overlay) paint method to remove.
     * @param location The layer from which to remove the paint method.
     * @return The action which executes this paint method exclusion.
     */
    public static PiccoloAction getRemovePaintMethodAction(final BrowserModel target,
                                                           final PaintMethod method,
                                                           final int location)
    {
        if(target == null || method == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.removePaintMethod(method,location);
            }
        };
        return action;
    }
    
    /**
     * Generates a select thumbnail action that depends on the mode of the
     * @param target The browser model to tie this action to.
     * @return A select thumbnail action that will change the specified
     *         BrowserModel.
     */
    public static PiccoloAction getSelectThumbnailAction(final BrowserModel target)
    {
        if(target == null)
        {
            return null;
        }
        
        final BrowserMode selectionMode =
            target.getCurrentMode(BrowserModel.SELECT_MODE_NAME);
            
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                PNode node = e.getPickedNode();
                if(!(node instanceof Thumbnail))
                {
                    return;
                }
                Thumbnail t = (Thumbnail)node;
                
                // checks if command key is down
                if(PiccoloModifiers.getModifier(e) !=
                   PiccoloModifiers.MOUSE_INDIV_SELECT)
                {
                    target.deselectAllThumbnails();                
                }
                target.selectThumbnail(t);

                Rectangle2D bounds = t.getBounds().getBounds2D();
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when executed, will trigger an opening of
     * this particular thumbnail.
     * @param t The thumbnail to open in the viewer.
     * @return A PiccoloAction that wraps the appropriate viewer trigger code
     *         in an execute() statement.
     */
    public static PiccoloAction getOpenInViewerAction(final Thumbnail t)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                BrowserEnvironment env = BrowserEnvironment.getInstance();
                BrowserAgent agent = env.getBrowserAgent();
                agent.loadImage(t);
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when executed, will trigger an information
     * view of this particular thumbnail.
     * @param t The thumbnail to query.
     * @return A PiccoloAction that wraps the appropriate DM trigger code
     *         in an execute() statement.
     */
    public static PiccoloAction getInfoFromDMAction(final Thumbnail t)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                BrowserEnvironment env = BrowserEnvironment.getInstance();
                BrowserAgent agent = env.getBrowserAgent();
                agent.showImageInfo(t);
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when executed, will trigger the annotation
     * dialog for this particular image shown in this thumbnail.
     * @param t The thumbnail to annotate (current image being annotated)
     * @return A PiccoloAction that wraps the appropriate Annotator trigger
     *         code in an execute() statement.
     */
    public static PiccoloAction getAnnotateImageAction(final Thumbnail t)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                BrowserEnvironment env = BrowserEnvironment.getInstance();
                BrowserAgent agent = env.getBrowserAgent();
                agent.annotateImage(t);
            }
            
            public void execute(PInputEvent e)
            {
                execute();
            }
        };
        return action;
    }
    
    /**
     * Creates an action that, when executed, will trigger the annotation
     * dialog for this particular image shown in this thumbnail.  The
     * annotator will be launched at the specified point.
     * @param t The thumbnail to annotate (current image being annotated)
     * @param point Where to launch the annotator.
     * @return A PiccoloAction that wraps the appropriate Annotator trigger
     *         code in an execute() statement.
     */
    public static PiccoloAction getAnnotateImageAction(final Thumbnail t,
                                                       final Point point)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                BrowserEnvironment env = BrowserEnvironment.getInstance();
                BrowserAgent agent = env.getBrowserAgent();
                agent.annotateImage(t,point);
            }
            
            public void execute(PInputEvent e)
            {
                execute();
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when exected, will trigger a zoom-to-fit
     * command.
     * @param model The browser to affect.
     * @return The zoom-to-fit PiccoloAction.
     */
    public static PiccoloAction getZoomToFitAction(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.ZOOM_MODE_NAME,
                                     BrowserMode.ZOOM_TO_FIT_MODE);
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when exected, will trigger a zoom-to-actual
     * command.
     * @param model The browser to affect.
     * @return The zoom-to-fit PiccoloAction.
     */
    public static PiccoloAction getZoomToActualAction(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.ZOOM_MODE_NAME,
                                     BrowserMode.ZOOM_ACTUAL_MODE);
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when exected, will trigger a zoom-to-50%
     * command.
     * @param model The browser to affect.
     * @return The zoom-to-fit PiccoloAction.
     */
    public static PiccoloAction getZoomTo50Action(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.ZOOM_MODE_NAME,
                                     BrowserMode.ZOOM_50_MODE);
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when exected, will trigger a zoom-to-75%
     * command.
     * @param model The browser to affect.
     * @return The zoom-to-fit PiccoloAction.
     */
    public static PiccoloAction getZoomTo75Action(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.ZOOM_MODE_NAME,
                                     BrowserMode.ZOOM_75_MODE);
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when exected, will trigger a zoom-to-200%
     * command.
     * @param model The browser to affect.
     * @return The zoom-to-fit PiccoloAction.
     */
    public static PiccoloAction getZoomTo200Action(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.ZOOM_MODE_NAME,
                                     BrowserMode.ZOOM_200_MODE);
            }
        };
        return action;
    }
    
    public static PiccoloAction getMagnifyOffAction(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                execute(null);
            }
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.SEMANTIC_MODE_NAME,
                                     BrowserMode.IMAGE_NAME_MODE);
            }
        };
        return action;
    }
    
    public static PiccoloAction getMagnifyOnAction(final BrowserModel model)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                execute(null);
            }
            public void execute(PInputEvent e)
            {
                model.setCurrentMode(BrowserModel.SEMANTIC_MODE_NAME,
                                     BrowserMode.SEMANTIC_ZOOMING_MODE);
            }
        };
        return action;
    }
    
    public static PiccoloAction getImageEnterAction(final BrowserView view)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                Thumbnail t = (Thumbnail)e.getPickedNode();
                view.generateMessage(t.getModel().getName());
            }
        };
        return action;
    }
    
    public static PiccoloAction getImageExitAction(final BrowserView view)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                view.clearMessages();
            }
            public void execute(PInputEvent e)
            {
                execute();   
            }
        };
        return action;
    }
    
    public static PiccoloAction getSemanticEnterAction(final BrowserView view,
                                                       final HoverManager layer)
    {
        PiccoloAction enterAction = getImageEnterAction(view);
        PiccoloAction action = new CompositePiccoloAction(enterAction)
        {
            public void execute(PInputEvent e)
            {
                super.execute(e);
                Thumbnail t = (Thumbnail)e.getPickedNode();
                
                double width = t.getWidth()*e.getCamera().getViewScale();
                double height = t.getHeight()*e.getCamera().getViewScale();
                if(width < SemanticZoomNode.getStandardWidth() &&
                   height < SemanticZoomNode.getStandardHeight())
                {
                    Image image = t.getImage();
                    SemanticZoomNode semanticNode =
                        new SemanticZoomNode(t);
                    
                    Point2D point = new Point2D.Double(t.getOffset().getX()+
                                                       t.getBounds().getCenter2D().getX(),
                                                       t.getOffset().getY()+
                                                       t.getBounds().getCenter2D().getY());
                    Point2D dummyPoint = new Point2D.Double(point.getX(),point.getY());
                    Dimension2D size = semanticNode.getBounds().getSize();
                    
                    Point2D viewPoint = e.getCamera().viewToLocal(dummyPoint);
                    
                    semanticNode.setOffset(viewPoint.getX()-size.getWidth()/2,
                                           viewPoint.getY()-size.getHeight()/2);
                    
                    double offRight = semanticNode.getOffset().getX()+
                                      semanticNode.getBounds().getWidth()-
                                      e.getCamera().getBounds().getWidth();
                    
                    double offBottom = semanticNode.getOffset().getY()+
                                       semanticNode.getBounds().getHeight()-
                                       e.getCamera().getBounds().getHeight();
                    
                    double offLeft = semanticNode.getOffset().getX();
                    double offTop = semanticNode.getOffset().getY();
                    
                    if(offRight > 0)
                    {
                        offLeft = offLeft-offRight-4;
                        semanticNode.setOffset(offLeft,offTop);
                    }
                    if(offBottom > 0)
                    {
                        offTop = offBottom-offTop-4;
                        semanticNode.setOffset(offLeft,offTop);
                    }
                    if(offLeft < 4)
                    {
                        offLeft = 4;
                        semanticNode.setOffset(offLeft,offTop);
                    }
                    if(offTop < 4)
                    {
                        offTop = 4;
                        semanticNode.setOffset(offLeft,offTop);
                    }
                    
                    layer.nodeEntered(e.getCamera(),semanticNode,200);
                }
            }
        };
        return action;
    }
    
    public static PiccoloAction getOverlayExitAction(final BrowserView view,
                                                     final HoverManager layer)
    {
        PiccoloAction exitAction = getImageExitAction(view);
        PiccoloAction action = new CompositePiccoloAction(exitAction)
        {
            public void execute(PInputEvent e)
            {
                super.execute(e);
                if(layer.getDisplayedNode() != null)
                {
                    PNode node = layer.getDisplayedNode();
                    PCamera camera = e.getCamera();
                    Point2D pos = camera.viewToLocal(e.getPosition());
                    Rectangle2D bounds =
                        new Rectangle2D.Double(node.getOffset().getX(),
                                               node.getOffset().getY(),
                                               node.getWidth(),
                                               node.getHeight());
                    if(!bounds.contains(pos))
                    {
                        layer.hideSemanticNode(e.getCamera());
                    }
                }
                layer.nodeExited();
            }
        };
        return action;
    }
}
