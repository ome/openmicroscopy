/*
 * org.openmicroscopy.shoola.agents.browser.BrowserAgent
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
package org.openmicroscopy.shoola.agents.browser;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.agents.browser.datamodel.ProgressMessageFormatter;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.layout.NumColsLayoutMethod;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserView;
import org.openmicroscopy.shoola.agents.browser.ui.StatusBar;
import org.openmicroscopy.shoola.agents.events.LoadDataset;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/**
 * The agent class that connects the browser to the rest of the client
 * system, and receives events triggered by other parts of the client.
 * Subscribes and places events on the EventBus.
 * 
 * The BrowserAgent responds to the following events: (list events)
 * 
 * The BrowserAgent places the following events on the queue: (list)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br><br>
 * <b>Internal Version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserAgent implements Agent, AgentEventListener
{
    private Registry registry;
    private EventBus eventBus;
    private BrowserEnvironment env;
    private TopFrame tf;
    
    private boolean useServerThumbs;
    private int thumbnailWidth;
    private int thumbnailHeight;
    
    /**
     * The XML key for getting the desired thumbnail extraction mode.
     * (server or composite)
     */
    public static final String THUMBNAIL_MODE_KEY =
        "/agents/browser/config/useServerThumbs";
    
    /**
     * The XML key for getting the composite mode thumbnail width.
     */
    public static final String THUMBNAIL_WIDTH_KEY =
        "/agents/browser/config/thumbnailWidth";
    
    /**
     * The XML key for getting the composite mode thumbnail height.
     */
    public static final String THUMBNAIL_HEIGHT_KEY =
        "/agents/browser/config/thumbnailHeight";
        
    public static final String DUMMY_DATASET_KEY =
        "/agents/browser/config/dummyDataset";

    /**
     * Initialize the browser controller and register the OMEBrowerAgent with
     * the EventBus.
     */
    public BrowserAgent()
    {
        System.err.println("browser launched");
        env = BrowserEnvironment.getInstance();
        env.setBrowserAgent(this);
    }
    
    /**
     * Does activation stuff (incomplete).
     * 
     * @see org.openmicroscopy.shoola.env.Agent#activate()
     */
    public void activate()
    {
        env.setBrowserManager(new BrowserManager());
    }
    
    /**
     * Checks if termination is possible (incomplete)
     * 
     * @see org.openmicroscopy.shoola.env.Agent#canTerminate()
     */
    public boolean canTerminate()
    {
        // for now, return true; won't keep track of dirty bits-- will
        // commit all changes to DB immediately & write all local config
        // information to file (TODO: change if necessary)
        return true;
    }
    
    /**
     * Does termination stuff (incomplete)
     * 
     * @see org.openmicroscopy.shoola.env.Agent#terminate()
     */
    public void terminate()
    {
        BrowserManager manager = env.getBrowserManager();
        List browserList = manager.getAllBrowsers();
        // TODO: flush local config stuff to disk
    }
    
    /**
     * @see org.openmicroscopy.shoola.env.Agent#setContext(org.openmicroscopy.shoola.env.config.Registry)
     */
    public void setContext(Registry ctx)
    {
        this.registry = ctx;
        this.eventBus = ctx.getEventBus();
        this.tf = ctx.getTopFrame();
        
        Boolean extractionMode = (Boolean)registry.lookup(THUMBNAIL_MODE_KEY);
        this.useServerThumbs = extractionMode.booleanValue();
        
        Integer thumbWidth = (Integer)registry.lookup(THUMBNAIL_WIDTH_KEY);
        Integer thumbHeight = (Integer)registry.lookup(THUMBNAIL_HEIGHT_KEY);
        
        this.thumbnailWidth = 120;//thumbWidth.intValue();
        this.thumbnailHeight = 120;//thumbHeight.intValue();
        
        eventBus.register(this,LoadDataset.class);
        
        JMenuItem testItem = new JMenuItem("Browser");
        testItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                Integer dummyID = (Integer)registry.lookup(DUMMY_DATASET_KEY);
                loadDataset(dummyID.intValue());
            }
        });
        
        tf.addToMenu(TopFrame.VIEW,testItem);
        testItem.setEnabled(true);
    }
    
    /**
     * Instructs the agent to load the Dataset with the given ID into
     * a new browser window.
     * @param browserIndex The ID (primary key) of the dataset to load.
     * @return Whether or not the dataset was succesfully loaded.
     */
    public boolean loadDataset(int datasetID)
    {
        DataManagementService dms = registry.getDataManagementService();
        DatasetData dataset;
        try
        {
            dataset = dms.retrieveDataset(datasetID);
            return loadDataset(dataset);
        }
        catch(DSAccessException dsae)
        {
            UserNotifier notifier = registry.getUserNotifier();
            notifier.notifyError("Data retrieval failure",
                "Unable to retrieve dataset (id = " + datasetID + ")", dsae);
            return false;
        }
        catch(DSOutOfServiceException dsoe)
        {
            // pop up new login window (eventually caught)
            throw new RuntimeException(dsoe);
        }
    }
    
    // loads the information from the Dataset into a BrowserModel, and the
    // also is responsible for triggering the mechanism that loads all the
    // images.
    private boolean loadDataset(DatasetData datasetModel)
    {
        // get that s**t out of here; call a proper parameter, man!
        if(datasetModel == null)
        {
            return false; // REEEEE-JECTED.
        }
        
        final BrowserModel model = new BrowserModel(datasetModel);
        model.setLayoutMethod(new NumColsLayoutMethod(8));
        final BrowserTopModel topModel = new BrowserTopModel();
        final BrowserView view = new BrowserView(model,topModel);
        final BrowserController controller = new BrowserController(model,view);
        controller.setStatusView(new StatusBar());
        
        env.getBrowserManager().addBrowser(controller);
        BrowserInternalFrame bif = new BrowserInternalFrame(controller);
        
        tf.addToDesktop(bif,TopFrame.PALETTE_LAYER);
        bif.setClosable(true);
        bif.setIconifiable(true);
        bif.setMaximizable(true);
        bif.setResizable(true);
        bif.show();
        
        final DataManagementService dms =
            registry.getDataManagementService();
            
        final PixelsService ps =
            registry.getPixelsService();
        
        // we're just going to assume that the DatasetData object does not
        // have the entire image list... might want to refactor this later.
        
        // always initialized as long as catch blocks return false
        List imageList;
        Comparator idComparator = new Comparator()
        {
            public int compare(Object arg0, Object arg1)
            {
                if(arg0 == null)
                {
                    return -1;
                }
                
                if(arg1 == null)
                {
                    return 1;
                }
                
                if(!(arg0 instanceof ImageSummary) ||
                   !(arg1 instanceof ImageSummary))
                {
                    return 0;
                }
                
                ImageSummary is1 = (ImageSummary)arg0;
                ImageSummary is2 = (ImageSummary)arg1;
                
                if(is1.getID() < is2.getID())
                {
                    return -1;
                }
                else if(is1.getID() == is2.getID())
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
        };
        
        try
        {
            // will this order by image ID?
            // should I explicitly order by another parameter?
            imageList = dms.retrieveImages(datasetModel.getID());
            Collections.sort(imageList,idComparator);
            if(imageList == null)
            {
                UserNotifier un = registry.getUserNotifier();
                un.notifyError("Database Error","Invalid Dataset ID specified.");
                return false;
            }
        }
        catch(DSOutOfServiceException dso)
        {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Connection Error",dso.getMessage(),dso);
            return false;
        }
        catch(DSAccessException dsa)
        {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error",dsa.getMessage(),dsa);
            return false;
        }
        
        final StatusBar status = controller.getStatusView();
        status.processStarted(imageList.size());    
        
        // see imageList initialization note above
        final List refList = Collections.unmodifiableList(imageList);
        
        Thread loader = new Thread()
        {
            public void run()
            {
                int count = 1;
                int total = refList.size();
                for(Iterator iter = refList.iterator(); iter.hasNext();)
                {
                    ImageSummary summary = (ImageSummary)iter.next();
                    try
                    {
                        Pixels pix = summary.getDefaultPixels().getPixels();
                        Image image = ps.getThumbnail(pix);
                        ImageData data = new ImageData();
                        data.setID(pix.getID());
                        ThumbnailDataModel tdm = new ThumbnailDataModel(data);
                        // TODO: figure out strategy for adding attributes.  do it here?
                        final Thumbnail t = new Thumbnail(image,tdm);
                        
                        final int theCount = count;
                        final int theTotal = total;
                        Runnable addTask = new Runnable()
                        {
                            public void run()
                            {
                                System.err.println("adding pix "+t.getModel().getID());
                                model.addThumbnail(t);
                                String message =
                                    ProgressMessageFormatter.format("Loaded image %n of %t...",
                                                            theCount,theTotal);
                                status.processAdvanced(message);
                            }
                        };
                        SwingUtilities.invokeLater(addTask);
                        count++;
                    }
                    catch(ImageServerException ise)
                    {
                        UserNotifier un = registry.getUserNotifier();
                        un.notifyError("ImageServer Error",ise.getMessage(),ise);
                        status.processFailed("Error loading images.");
                        return;
                    }
                }
                
                status.processSucceeded("All images loaded.");
                return;
            }
        };
        loader.start();
        return true;
    }
        
    
    /**
     * Gets the valid image types for the particular dataset.
     * @param dataset
     * @return
     */
    public List getImageTypesForDataset(DatasetData dataset)
    {
        return null;
    }

    /**
     * Instructs the agent to load the Dataset with the given ID into the
     * specified browser.
     * 
     * @param browserIndex The index of the browser window to load.
     * @param datasetID The ID of the dataset to load.
     * @return true If the load was successful, false if not.
     */
    public boolean loadDataset(int browserIndex, int datasetID)
    {
        // TODO: fill in loadDataset(int)
        return true;
    }

    /**
     * Instruct the BrowserAgent to fire a LoadImage event, to be handled
     * by another part of the client.
     * 
     * @param imageID The ID of the image to load (in a viewer, for example)
     */
    public void loadImage(int imageID)
    {
        // TODO: fill in loadImage(int)
    }

    /**
     * Instruct the BrowserAgent to fire a LoadImages event, to be handled
     * by another part of the client.
     * 
     * @param IDs The IDs of the image to load (in a viewer, for example)
     */
    public void loadImages(int[] IDs)
    {
        if (IDs == null || IDs.length == 0)
        {
            return;
        }
        // TODO: fill in loadImages(int[])
    }
    
    /**
     * Responds to an event on the event bus.
     * 
     * @see org.openmicroscopy.shoola.env.event.AgentEventListener#eventFired(org.openmicroscopy.shoola.env.event.AgentEvent)
     */
    public void eventFired(AgentEvent e)
    {
        if(e instanceof LoadDataset)
        {
            LoadDataset event = (LoadDataset)e;
            System.err.println("LoadDataset event received by browser");
            loadDataset(event.getDatasetID());
        }
    }
}
