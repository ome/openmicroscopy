/*
 * org.openmicroscopy.shoola.agents.browser.ui.BrowserMenuBar
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.shoola.agents.browser.BrowserAgent;
import org.openmicroscopy.shoola.agents.browser.BrowserEnvironment;
import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.BrowserModelAdapter;
import org.openmicroscopy.shoola.agents.browser.datamodel.CategoryTree;
import org.openmicroscopy.shoola.agents.browser.events.BrowserActions;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory;
import org.openmicroscopy.shoola.agents.browser.images.OverlayMethods;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethods;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.layout.CategoryGroupingMethod;
import org.openmicroscopy.shoola.agents.browser.layout.GroupingMethod;
import org.openmicroscopy.shoola.agents.browser.layout.LayoutMethod;
import org.openmicroscopy.shoola.agents.browser.layout.QuantumGroupLayoutMethod;

/**
 * The menu bar for each browser internal frame or frame (should we decide
 * to adapt an MWI as opposed to an MDI GUI).  Allows an external agent to
 * disable/enable menu items based on the content.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserMenuBar extends JMenuBar
{
    public static final int VIEW_MENU = 1;
    public static final int ANALYZE_MENU = 2;
    public static final int LAYOUT_MENU = 3;
    
    public static final int VIEW_WELLNO_ITEM = 101;
    public static final int VIEW_ANNOTATION_ITEM = 102;
    public static final int VIEW_MULTIIMAGE_ITEM = 103;
    
    public static final int ANALYZE_MAGNIFY_ITEM = 201;
    public static final int ANALYZE_HEATMAP_ITEM = 202;
    public static final int ANALYZE_VIEW_CATEGORIES_ITEM = 203;
    public static final int ANALYZE_CATEGORIES_ITEM = 204;
    
    public static final int LAYOUT_DEFAULT_ITEM = 301;
    public static final int LAYOUT_CATEGORY_ITEM = 302;
    
    private Map menuMap;
    private Map menuItemMap;
    
    private BrowserModel actionTarget;
    
    public BrowserMenuBar(BrowserModel target)
    {
        menuMap = new HashMap();
        menuItemMap = new HashMap();
        
        this.actionTarget = target;
        
        add(createViewMenu());
        add(createAnalyzeMenu());
        add(createLayoutMenu()); // comment out if you don't want it
        
        target.addModelListener(new BrowserModelAdapter()
        {
            public void modelUpdated()
            {
                JMenu menu = (JMenu)menuMap.get(new Integer(LAYOUT_MENU));
                JMenuItem menuItem =
                    (JMenuItem)menuItemMap.get(new Integer(LAYOUT_CATEGORY_ITEM));
                menu.remove(menuItem);
                menu.add(createCategoryLayoutMenu());
            }
        });
    }
    
    /**
     * Shortcut for setting the settings for plate or non-plate mode
     * (specifically, well number and layout method)
     * 
     * @param inScreenMode The browser is displaying a chemical screen.
     */
    public void setScreenMode(boolean inScreenMode)
    {
        if(!inScreenMode)
        {
            JMenuItem item =
                (JMenuItem)menuItemMap.get(new Integer(VIEW_WELLNO_ITEM));
            item.setEnabled(false); // default true
        }
    }
    
    // handle adding to the maps
    private JMenu createViewMenu()
    {
        JMenu menu = new JMenu("View");
        menu.add(createViewWellItem());
        menu.add(createViewAnnotationItem());
        // menu.add(createViewMultiImageItem()); (wait until impl.)
        
        menuMap.put(new Integer(VIEW_MENU),menu);
        return menu;
    }
    
    private JMenu createAnalyzeMenu()
    {
        JMenu menu = new JMenu("Analyze");
        
        menu.add(createAnalyzeMagnifierItem());
        menu.addSeparator();
        menu.add(createAnalyzeHeatmapItem());
        menu.add(createAnalyzeViewCategoryItem());
        menu.add(createAnalyzeCategoryItem());
        
        menuMap.put(new Integer(ANALYZE_MENU),menu);
        return menu;
    }
    
    private JMenu createLayoutMenu()
    {
        JMenu menu = new JMenu("Layout");
        
        menu.add(createDefaultLayoutItem());
        menu.add(createCategoryLayoutMenu());
        
        menuMap.put(new Integer(LAYOUT_MENU),menu);
        return menu;
    }
    
    private JMenuItem createViewWellItem()
    {
        JCheckBoxMenuItem wellItem = new JCheckBoxMenuItem("Well Number");
        wellItem.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.DESELECTED)
                {
                    actionTarget.removePaintMethod(PaintMethods.DRAW_WELLNO_METHOD,
                                                   Thumbnail.FOREGROUND_PAINT_METHOD);
                }
                else if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    actionTarget.addPaintMethod(PaintMethods.DRAW_WELLNO_METHOD,
                                                Thumbnail.FOREGROUND_PAINT_METHOD);
                }
            }

        });
        menuItemMap.put(new Integer(VIEW_WELLNO_ITEM),wellItem);
        return wellItem;
    }
    
    private JMenuItem createViewAnnotationItem()
    {
        JCheckBoxMenuItem annotationItem = new JCheckBoxMenuItem("Annotations");
        annotationItem.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.DESELECTED)
                {
                    actionTarget.removeOverlayMethod(OverlayMethods.ANNOTATION_METHOD);
                }
                else if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    actionTarget.addOverlayMethod(OverlayMethods.ANNOTATION_METHOD);
                }
            }
        });
        menuItemMap.put(new Integer(VIEW_ANNOTATION_ITEM),annotationItem);
        return annotationItem;
    }
    
    private JMenuItem createViewMultiImageItem()
    {
        JCheckBoxMenuItem multiImageItem = new JCheckBoxMenuItem("Image Dimensions");
        multiImageItem.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.DESELECTED)
                {
                    // TODO add multidimension paint method
                }
                else if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    // TODO add multidimension paint method
                }
            }
        });
        menuItemMap.put(new Integer(VIEW_MULTIIMAGE_ITEM),multiImageItem);
        return multiImageItem;
    }
    
    private JMenuItem createAnalyzeMagnifierItem()
    {
        JCheckBoxMenuItem magnifierItem = new JCheckBoxMenuItem("Magnifier");
        magnifierItem.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    PiccoloAction magnifierOnAction =
                        PiccoloActionFactory.getMagnifyOnAction(actionTarget);
                    magnifierOnAction.execute();
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED)
                {
                    PiccoloAction magnifierOffAction =
                        PiccoloActionFactory.getMagnifyOffAction(actionTarget);
                    magnifierOffAction.execute();
                }
            }
        });
        
        BrowserMode currentMode =
            actionTarget.getCurrentMode(BrowserModel.SEMANTIC_MODE_NAME);
        
        if(currentMode == BrowserMode.SEMANTIC_ZOOMING_MODE)
        {
            magnifierItem.setSelected(true);
        }
        else
        {
            magnifierItem.setSelected(false);
        }
        
        menuItemMap.put(new Integer(ANALYZE_MAGNIFY_ITEM),magnifierItem);
        return magnifierItem;
    }
    
    private JMenuItem createAnalyzeHeatmapItem()
    {
        JMenuItem heatItem = new JMenuItem("HeatMap");
        heatItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                BrowserActions.SHOW_HEATMAP_ACTION.execute();
            }
        });
        menuItemMap.put(new Integer(ANALYZE_HEATMAP_ITEM),heatItem);
        return heatItem;
    }
    
    private JMenuItem createAnalyzeViewCategoryItem()
    {
        JMenuItem categoryItem = new JMenuItem("View Phenotypes");
        categoryItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                BrowserActions.SHOW_COLORMAP_ACTION.execute();
            }
        });
        menuItemMap.put(new Integer(ANALYZE_VIEW_CATEGORIES_ITEM),categoryItem);
        return categoryItem;
    }
    
    private JMenuItem createAnalyzeCategoryItem()
    {
        final BrowserEnvironment env = BrowserEnvironment.getInstance();
        final BrowserAgent agent = env.getBrowserAgent();
        
        JMenuItem categoryItem = new JMenuItem("Edit Phenotypes");
        categoryItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int datasetID = actionTarget.getDataset().getID();
                String name = actionTarget.getDataset().getName();
                agent.loadCategories(datasetID,name);
            }
        });
        menuItemMap.put(new Integer(ANALYZE_CATEGORIES_ITEM),categoryItem);
        return categoryItem;
    }
    
    private JMenuItem createDefaultLayoutItem()
    {
        JMenuItem defaultItem = new JMenuItem("Default Layout");
        defaultItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                LayoutMethod defaultLM = actionTarget.getDefaultLayoutMethod();
                actionTarget.setLayoutMethod(defaultLM);
            }
        });
        menuItemMap.put(new Integer(LAYOUT_DEFAULT_ITEM),defaultItem);
        return defaultItem;
    }
    
    private JMenu createCategoryLayoutMenu()
    {
        JMenu groupMenu = new JMenu("Arrange by Phenotype");
        /*
        final CategoryTree tree = actionTarget.getCategoryTree();
        List groups = tree.getCategoryGroups();
        for(Iterator iter = groups.iterator(); iter.hasNext();)
        {
            final CategoryGroup cg = (CategoryGroup)iter.next();
            JMenuItem item = new JMenuItem(cg.getName());
            item.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    GroupingMethod gm = new CategoryGroupingMethod(cg,tree);
                    actionTarget.setGroupingMethod(gm);
                    int width = (int)Math.round(actionTarget.getAverageWidth())+10;
                    int height = (int)Math.round(actionTarget.getAverageHeight())+10;
                    LayoutMethod lm =
                        new QuantumGroupLayoutMethod(gm,width,height);
                    actionTarget.setLayoutMethod(lm);
                }
            });
            groupMenu.add(item);
        }
        menuItemMap.put(new Integer(LAYOUT_CATEGORY_ITEM),groupMenu);
        */
        return groupMenu;
    }
    
    /**
     * Enables either a menu or a menu item.
     * @param menuComponent The code (see static variables) of the component to
     *                      enable.
     */
    public void enable(int menuComponent)
    {
        Integer val = new Integer(menuComponent);
        if(menuMap.containsKey(val))
        {
            JMenu menu = (JMenu)menuMap.get(val);
            menu.setEnabled(true);
        }
        if(menuItemMap.containsKey(val))
        {
            JMenuItem menuItem = (JMenuItem)menuItemMap.get(val);
            menuItem.setEnabled(true);
        }
    }
    
    /**
     * Disables either a menu or a menu item.
     * @param menuComponent The code (see static variables) of the component to
     *                      disable.
     */
    public void disable(int menuComponent)
    {
        Integer val = new Integer(menuComponent);
        if(menuMap.containsKey(val))
        {
            JMenu menu = (JMenu)menuMap.get(val);
            menu.setEnabled(false);
        }
        if(menuItemMap.containsKey(val))
        {
            JMenuItem menuItem = (JMenuItem)menuItemMap.get(val);
            menuItem.setEnabled(false);
        }
    }
    
    /**
     * Selects a menu item.
     * @param menuComponent The code (see static variables) of the component to
     *                      select.
     */
    public void select(int menuItemNumber)
    {
        Integer val = new Integer(menuItemNumber);
        if(menuItemMap.containsKey(val))
        {
            JMenuItem menuItem = (JMenuItem)menuItemMap.get(val);
            menuItem.setSelected(true);
        }
    }
    
    /**
     * Deselects a menu item.
     * @param menuComponent The code (see static variables) of the component to
     *                      deselect.
     */
    public void deselect(int menuItemNumber)
    {
        Integer val = new Integer(menuItemNumber);
        if(menuItemMap.containsKey(val))
        {
            JMenuItem menuItem = (JMenuItem)menuItemMap.get(val);
            menuItem.setSelected(false);
        }
    }
}
