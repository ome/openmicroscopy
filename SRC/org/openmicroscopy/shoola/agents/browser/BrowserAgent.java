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
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.openmicroscopy.shoola.agents.annotator.events.AnnotateImage;
import org.openmicroscopy.shoola.agents.annotator.events.ImageAnnotated;
import org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapManager;
import org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapModel;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.ds.st.ImagePlate;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.agents.browser.datamodel.CompletePlate;
import org.openmicroscopy.shoola.agents.browser.datamodel.PlateInfo;
import org.openmicroscopy.shoola.agents.browser.datamodel.PlateInfoParser;
import org.openmicroscopy.shoola.agents.browser.datamodel.ProgressMessageFormatter;
import org.openmicroscopy.shoola.agents.browser.events.AnnotateImageHandler;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;
import org.openmicroscopy.shoola.agents.browser.layout.NumColsLayoutMethod;
import org.openmicroscopy.shoola.agents.browser.layout.PlateLayoutMethod;
import org.openmicroscopy.shoola.agents.browser.ui.BPalette;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserInternalFrame;
import org.openmicroscopy.shoola.agents.browser.ui.BrowserView;
import org.openmicroscopy.shoola.agents.browser.ui.PaletteFactory;
import org.openmicroscopy.shoola.agents.browser.ui.StatusBar;
import org.openmicroscopy.shoola.agents.browser.ui.UIWrapper;
import org.openmicroscopy.shoola.agents.browser.util.KillableThread;
import org.openmicroscopy.shoola.agents.datamng.events.ViewImageInfo;
import org.openmicroscopy.shoola.agents.events.LoadDataset;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
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
    
    private List imageTypeList;
    
    private Map activeThreadMap;
    
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
        
    public static final String SEMANTIC_WIDTH_KEY =
        "/agents/browser/config/semanticWidth";    
    
    public static final String SEMANTIC_HEIGHT_KEY =
        "/agents/browser/config/semanticHeight";

    /**
     * Initialize the browser controller and register the OMEBrowerAgent with
     * the EventBus.
     */
    public BrowserAgent()
    {
        env = BrowserEnvironment.getInstance();
        env.setBrowserAgent(this);
        activeThreadMap = new IdentityHashMap();
        imageTypeList = new ArrayList();
    }
    
    /**
     * Does activation stuff (incomplete).
     * 
     * @see org.openmicroscopy.shoola.env.Agent#activate()
     */
    public void activate()
    {
        env.setBrowserManager(new BrowserManager());
        env.setHeatMapManager(new HeatMapManager());
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
        
        env.setIconManager(IconManager.getInstance(ctx));
        
        Boolean extractionMode = (Boolean)registry.lookup(THUMBNAIL_MODE_KEY);
        this.useServerThumbs = extractionMode.booleanValue();
        
        Integer thumbWidth = (Integer)registry.lookup(THUMBNAIL_WIDTH_KEY);
        Integer thumbHeight = (Integer)registry.lookup(THUMBNAIL_HEIGHT_KEY);
        
        this.thumbnailWidth = 120;//thumbWidth.intValue();
        this.thumbnailHeight = 120;//thumbHeight.intValue();
        
        eventBus.register(this,LoadDataset.class);
        eventBus.register(this,ImageAnnotated.class);
        
        /*
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
        */
        
        // test code to check for image STs
        SemanticTypesService sts = registry.getSemanticTypesService();
        try
        {
            List typeList = sts.getAvailableImageTypes();
            for(Iterator iter = typeList.iterator(); iter.hasNext();)
            {
                SemanticType st = (SemanticType)iter.next();
                imageTypeList.add(st);
            }
        }
        catch(DSOutOfServiceException dso)
        {
            dso.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Connection Error",dso.getMessage(),dso);
        }
        catch(DSAccessException dsa)
        {
            dsa.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error",dsa.getMessage(),dsa);
        }
    }
    
    /**
     * Instructs the agent to load the Dataset with the given ID into
     * a new browser window.
     * @param browserIndex The ID (primary key) of the dataset to load.
     * @return Whether or not the dataset was succesfully loaded.
     */
    public void loadDataset(int datasetID)
    {
        BrowserManager manager = env.getBrowserManager();
        int index;
        if((index = manager.hasBrowser(datasetID))
            != BrowserManager.NOT_FOUND)
        {
            manager.setActiveBrowser(index);
            return;
        }
        DataManagementService dms = registry.getDataManagementService();
        DatasetData dataset;
        
        final BrowserModel model = new BrowserModel();
        model.setLayoutMethod(new NumColsLayoutMethod(8));
        BrowserTopModel topModel = new BrowserTopModel();
        
        BPalette optionPalette = PaletteFactory.getOptionPalette(model,topModel);
        topModel.addPalette(UIConstants.OPTIONS_PALETTE_NAME,optionPalette);
        topModel.hidePalette(optionPalette);
        
        optionPalette.setOffset(0,0);
        BrowserView view = new BrowserView(model,topModel);
        BrowserController controller = new BrowserController(model,topModel,view);
        controller.setStatusView(new StatusBar());

        final int browserIndex = 0; // default behavior for new browser
        final BrowserInternalFrame bif = new BrowserInternalFrame(controller);
        env.getBrowserManager().addBrowser(bif);

        StatusBar status = controller.getStatusView();

        tf.addToDesktop(bif,TopFrame.PALETTE_LAYER);
        bif.setClosable(true);
        bif.setIconifiable(true);
        bif.setMaximizable(true);
        bif.setResizable(true);
        bif.show();
        
        final int theDataset = datasetID;
        KillableThread retrieveThread = new KillableThread()
        {
            public void run()
            {
                addLoaderThread(bif.getController(),this);
                try
                {
                    DataManagementService dms =
                        registry.getDataManagementService();
                    DatasetData dataset = dms.retrieveDataset(theDataset);
                    model.setDataset(dataset);
                    if(!kill)
                    {
                        bif.setTitle("Image Browser: "+dataset.getName());
                        loadDataset(browserIndex,dataset);
                    }
                    else
                    {
                        System.err.println("killed OK");
                    }
                }
                catch(DSAccessException dsae)
                {
                    UserNotifier notifier = registry.getUserNotifier();
                    notifier.notifyError("Data retrieval failure",
                    "Unable to retrieve dataset (id = " + theDataset + ")", dsae);
                    return;
                }
                catch(DSOutOfServiceException dsoe)
                {
                    // pop up new login window (eventually caught)
                    throw new RuntimeException(dsoe);
                }
                removeLoaderThread(bif.getController(),this);
            }
        };
        
        retrieveThread.start();
        writeStatusImmediately(status,"Loading dataset from DB...");
            
    }
    
    // loads the information from the Dataset into a BrowserModel, and the
    // also is responsible for triggering the mechanism that loads all the
    // images.
    private boolean loadDataset(int whichBrowser, DatasetData datasetModel)
    {
        // get that s**t out of here; call a proper parameter, man!
        if(datasetModel == null)
        {
            return false; // REEEEE-JECTED.
        }
        
        // TODO sync bug here (messes up if other browser closed)
        final BrowserController controller =
            env.getBrowserManager().getBrowser(whichBrowser).getController();
        
        final BrowserModel model = controller.getBrowserModel();
        final BrowserView view = controller.getView();
        final StatusBar status = controller.getStatusView();
        
        final DataManagementService dms =
            registry.getDataManagementService();
        
        final SemanticTypesService sts =
            registry.getSemanticTypesService();
            
        final PixelsService ps =
            registry.getPixelsService();
        
        // we're just going to assume that the DatasetData object does not
        // have the entire image list... might want to refactor this later.
        
        // always initialized as long as catch blocks return false
        List imageList;
        Map plateMap;
        
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
        
        boolean plateMode = false;
        List plateList;
        List annotationList;
        Map annotationMap;
        PlateInfo plateInfo = new PlateInfo();
        
        try
        {
            // explicit interrupt check
            if(!activeThreadMap.containsKey(controller))
            {
                System.err.println("killed OK");
                return false;
            }
            // will this order by image ID?
            // should I explicitly order by another parameter?
            writeStatusImmediately(status,"Retrieving image records from DB...");
            imageList = dms.retrieveImages(datasetModel.getID());
            if(imageList == null)
            {
                UserNotifier un = registry.getUserNotifier();
                un.notifyError("Database Error","Invalid Dataset ID specified.");
                return false;
            }

            Collections.sort(imageList,idComparator);
            List idList = new ArrayList();
            
            // explicit interrupt check
            if(!activeThreadMap.containsKey(controller))
            {
                System.err.println("killed OK");
                return false;
            }
            // get plate information (if any) so that we can properly add
            // images
            writeStatusImmediately(status,"Retrieving plate information from DB...");
            for(int i=0;i<imageList.size();i++)
            {
                ImageSummary summary = (ImageSummary)imageList.get(i);
                idList.add(new Integer(summary.getID()));
            }
            
            plateList = sts.retrieveImageAttributes("ImagePlate",idList);
            
            // explicit interrupt check
            if(!activeThreadMap.containsKey(controller))
            {
                System.err.println("killed OK");
                return false;
            }
            writeStatusImmediately(status,"Retrieving annotation information from DB...");
            annotationList = sts.retrieveImageAttributes("ImageAnnotation",idList);
            annotationMap = new HashMap();
            for(Iterator iter = annotationList.iterator(); iter.hasNext();)
            {
                ImageAnnotation ia = (ImageAnnotation)iter.next();
                annotationMap.put(new Integer(ia.getImage().getID()),ia);
            }
            
            writeStatusImmediately(status,"Filling in relevant ST info from DB...");
            loadRelevantTypes(imageList,model,status);
            
            // going to assume that all image plates in dataset belong to
            // same plate (could be very wrong)
            if(plateList != null && plateList.size() > 0)
            {
                plateMode = true;
                String[] wellNames = new String[plateList.size()];
                for(int i=0;i<plateList.size();i++)
                {
                    ImagePlate plate = (ImagePlate)plateList.get(i);
                    wellNames[i] = plate.getWell();
                }
            
                plateInfo = PlateInfoParser.buildPlateInfo(wellNames);
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
        
        final Map imageMap = new HashMap();
        for(Iterator iter = imageList.iterator(); iter.hasNext();)
        {
            ImageSummary summary = (ImageSummary)iter.next();
            imageMap.put(new Integer(summary.getID()),summary);
        }
        
        status.processStarted(imageList.size());
        // see imageList initialization note above
        final List refList = Collections.unmodifiableList(imageList);
        final List refPlateList = Collections.unmodifiableList(plateList);
        final PlateInfo refInfo = plateInfo;
        final Map refAnnotations = annotationMap;
        
        KillableThread plateLoader = new KillableThread()
        {
            public void run()
            {
                addLoaderThread(controller,this);
                final List thumbnails = new ArrayList();
                int count = 1;
                int total = refList.size();
                
                PlateLayoutMethod lm = new PlateLayoutMethod(refInfo.getNumRows(),
                                                             refInfo.getNumCols());
                model.setLayoutMethod(lm);
                
                CompletePlate plate = new CompletePlate();
                for(Iterator iter = refPlateList.iterator(); iter.hasNext();)
                {
                    ImagePlate ip = (ImagePlate)iter.next();
                    plate.put(ip.getWell(),new Integer(ip.getImage().getID()));
                }
                
                boolean wellSized = false;
                for(int i=0;i<refInfo.getNumRows();i++)
                {
                    for(int j=0;j<refInfo.getNumCols();j++)
                    {
                        // explicit break out
                        if(kill)
                        {
                            j=refInfo.getNumCols();
                            i=refInfo.getNumRows();
                            break;
                        }
                        String row = refInfo.getRowName(i);
                        String col = refInfo.getColumnName(j);
                        String well = row+col;
                        List sampleList = (List)plate.get(well);
                        if(sampleList.size() == 1)
                        {
                            Integer intVal = (Integer)sampleList.get(0);
                            ImageSummary sum = (ImageSummary)imageMap.get(intVal);
                            try
                            {
                                Pixels pix = sum.getDefaultPixels().getPixels();
                                Image image = ps.getThumbnail(pix);
                                if(!wellSized)
                                {
                                    lm.setWellWidth(image.getWidth(null));
                                    lm.setWellHeight(image.getHeight(null));
                                    wellSized = true;
                                }
                                ThumbnailDataModel tdm = new ThumbnailDataModel(sum);
                                tdm.setValue(UIConstants.WELL_KEY_STRING,well);
                                tdm.getAttributeMap().putAttribute(pix);
                                
                                ImageAnnotation annotation =
                                    (ImageAnnotation)refAnnotations.get(new Integer(sum.getID()));
                                    
                                if(annotation != null)
                                {
                                    tdm.getAttributeMap().putAttribute(annotation);
                                }
                                final Thumbnail t = new Thumbnail(image,tdm);
                                lm.setIndex(t,i,j);
                                
                                final int theCount = count;
                                final int theTotal = total;
                                Runnable addTask = new Runnable()
                                {
                                    public void run()
                                    {
                                        thumbnails.add(t);
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
                        else
                        {
                            Image[] images = new Image[sampleList.size()];
                            ThumbnailDataModel[] models =
                                new ThumbnailDataModel[sampleList.size()];
                            for(int k=0;k<sampleList.size();k++)
                            {
                                Integer intVal = (Integer)sampleList.get(k);
                                ImageSummary sum = (ImageSummary)imageMap.get(intVal);
                                try
                                {
                                    Pixels pix = sum.getDefaultPixels().getPixels();
                                    Image image = ps.getThumbnail(pix);
                                    ThumbnailDataModel tdm = new ThumbnailDataModel(sum);
                                    tdm.setValue(UIConstants.WELL_KEY_STRING,well);
                                    tdm.getAttributeMap().putAttribute(pix);
                                    ImageAnnotation annotation =
                                        (ImageAnnotation)refAnnotations.get(new Integer(sum.getID()));
                                        
                                    if(annotation != null)
                                    {
                                        tdm.getAttributeMap().putAttribute(annotation);
                                    }
                                    images[k] = image;
                                    models[k] = tdm;
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
                            
                            final Thumbnail t = new Thumbnail(images,models);
                            lm.setIndex(t,i,j);
                            final int theCount = count;
                            final int theTotal = total;
                            
                            Runnable addTask = new Runnable()
                            {
                                public void run()
                                {
                                    String message =
                                        ProgressMessageFormatter.format("Loaded image %n of %t...",
                                                                        theCount,theTotal);
                                    status.processAdvanced(message);
                                    thumbnails.add(t);
                                }
                            };
                            SwingUtilities.invokeLater(addTask);
                        }
                    }
                }
                
                if(!kill)
                {
                    Runnable finalTask = new Runnable()
                    {
                        public void run()
                        {
                            Thumbnail[] ts = new Thumbnail[thumbnails.size()];
                            thumbnails.toArray(ts);
                            model.addThumbnails(ts);
                            status.processSucceeded("All images loaded.");
                        }
                    };
                    SwingUtilities.invokeLater(finalTask);
                    return;
                }
                else
                {
                    System.err.println("killed OK");
                }
                removeLoaderThread(controller,this);
            }
        };
        
        KillableThread loader = new KillableThread()
        {
            public void run()
            {
                addLoaderThread(controller,this);
                int count = 1;
                int total = refList.size();
                
                for(Iterator iter = refList.iterator(); (iter.hasNext() && !kill);)
                {
                    ImageSummary summary = (ImageSummary)iter.next();
                    
                    try
                    {
                        Pixels pix = summary.getDefaultPixels().getPixels();
                        Image image = ps.getThumbnail(pix);
                        ThumbnailDataModel tdm = new ThumbnailDataModel(summary);
                        tdm.getAttributeMap().putAttribute(pix);
                        ImageAnnotation annotation =
                            (ImageAnnotation)refAnnotations.get(new Integer(summary.getID()));
                            
                        if(annotation != null)
                        {
                            tdm.getAttributeMap().putAttribute(annotation);
                        }
                        // TODO: figure out strategy for adding attributes.  do it here?
                        final Thumbnail t = new Thumbnail(image,tdm);
                        
                        final int theCount = count;
                        final int theTotal = total;
                        Runnable addTask = new Runnable()
                        {
                            public void run()
                            {
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
                
                if(!kill)
                {
                    Runnable finalTask = new Runnable()
                    {
                        public void run()
                        {
                            status.processSucceeded("All images loaded.");
                        }
                    };
                    SwingUtilities.invokeLater(finalTask);
                    return;
                }
                removeLoaderThread(controller,this);
            }
        };
        
        // explicit interrupt check
        if(!activeThreadMap.containsKey(controller))
        {
            System.err.println("killed OK");
            return false;
        }
        
        if(plateMode)
        {
            plateLoader.start();
        }
        else
        {
            loader.start();
        }
        
        return true;
    }
    
    // display content information immediately.
    private void writeStatusImmediately(final StatusBar status,
                                        final String message)
    {
        Runnable writeTask = new Runnable()
        {
            public void run()
            {
                status.setLeftText(message);
            }
        };
        SwingUtilities.invokeLater(writeTask);
    }

    /**
     * Instructs the agent to load the Dataset with the given ID into the
     * specified browser.
     * 
     * @param browserIndex The index of the browser window to load.
     * @param datasetID The ID of the dataset to load.
     * @return true If the load was successful, false if not.
     */
    public void loadDataset(int browserIndex, int datasetID)
    {
        final int theDataset = datasetID;
        final int theIndex = browserIndex;
        BrowserManager manager = env.getBrowserManager();
        final UIWrapper browser = manager.getBrowser(browserIndex);
        
        BrowserController controller = browser.getController();
        
        final BrowserModel model = controller.getBrowserModel();
        
        Thread retrieveThread = new Thread()
        {
            public void run()
            {
                try
                {
                    DataManagementService dms =
                        registry.getDataManagementService();
                    DatasetData dataset = dms.retrieveDataset(theDataset);
                    model.setDataset(dataset);
                    browser.setBrowserTitle("Image Browser: "+dataset.getName());
                    loadDataset(theIndex,dataset);
                }
                catch(DSAccessException dsae)
                {
                    UserNotifier notifier = registry.getUserNotifier();
                    notifier.notifyError("Data retrieval failure",
                    "Unable to retrieve dataset (id = " + theDataset + ")", dsae);
                    return;
                }
                catch(DSOutOfServiceException dsoe)
                {
                    // pop up new login window (eventually caught)
                    throw new RuntimeException(dsoe);
                }
            }
        };
    }
    
    /**
     * Fills the model with a list of pertinent image-granular attributes.
     * @param model The model to load.
     */
    private void loadRelevantTypes(List imageList, BrowserModel targetModel,
                                   StatusBar status)
    {
        if(imageList == null || targetModel == null) return;
        List relevantTypes = new ArrayList();
        SemanticTypesService sts = registry.getSemanticTypesService();
        
        List integerList = new ArrayList();
        
        for(Iterator iter = imageList.iterator(); iter.hasNext();)
        {
            ImageSummary is = (ImageSummary)iter.next();
            integerList.add(new Integer(is.getID()));
        }
        
        for(int i=0;i<imageTypeList.size();i++)
        {
            SemanticType st = (SemanticType)imageTypeList.get(i);
            try
            {
                writeStatusImmediately(status,"Counting "+st.getName()+
                                       " attributes from DB ("+
                                       (i+1)+"/"+imageTypeList.size()+")");
                int count = sts.countImageAttributes(st,integerList);
                if(count > 0)
                {
                    relevantTypes.add(st);
                } 
            }
            catch(DSAccessException dsa)
            {
                UserNotifier un = registry.getUserNotifier();
                un.notifyError("Server Error","Could not count attributes",dsa);
            }
            catch(DSOutOfServiceException dso)
            {
                UserNotifier un = registry.getUserNotifier();
                un.notifyError("Communication Error","Could not retrieve count",dso);
            }
        }
        
        SemanticType[] types = new SemanticType[relevantTypes.size()];
        relevantTypes.toArray(types);
        targetModel.setRelevantTypes(types);
        writeStatusImmediately(status,"Filling in analyzed semantic types...");
        HeatMapModel hmm = new HeatMapModel(targetModel);
        HeatMapManager manager = env.getHeatMapManager();
        manager.putHeatMapModel(hmm);
        manager.showModel(targetModel.getDataset().getID());
    }
    
    // keeps track of the time-consuming loader threads.
    private void addLoaderThread(BrowserController loader,
                                 KillableThread thread)
    {
        if(activeThreadMap.containsKey(loader))
        {
            List list = (List)activeThreadMap.get(loader);
            list.add(thread);
        }
        else
        {
            List list = new ArrayList();
            list.add(thread);
            activeThreadMap.put(loader,list);
        }
    }
    
    private void removeLoaderThread(BrowserController loader,
                                    KillableThread thread)
    {
        if(activeThreadMap.containsKey(loader))
        {
            List list = (List)activeThreadMap.get(loader);
            list.remove(thread);
            if(list.size() == 0)
            {
                activeThreadMap.remove(loader);
            }
        }
    }
    
    /**
     * Indicates browser shutdown; interrupt any threads associated with
     * this browser.
     * @param loader The browser to cancel loading.
     */
    public void interruptThread(BrowserController loader)
    {
        if(activeThreadMap.containsKey(loader))
        {
            List list = (List)activeThreadMap.get(loader);
            for(Iterator iter = list.iterator(); iter.hasNext();)
            {
                KillableThread kt = (KillableThread)iter.next();
                kt.kill();
            }
            activeThreadMap.remove(loader);
        }
    }
    
    /**
     * Instruct the BrowserAgent to fire a LoadImage event, to show
     * the current image and pixels represented in the thumbnail.
     * @param t The thumbnail of the image to load in the viewer.
     */
    public void loadImage(Thumbnail t)
    {
        ThumbnailDataModel tdm = t.getModel(); // gets current model
        int imageID = tdm.getID();
        Pixels pixels = (Pixels)tdm.getAttributeMap().getAttribute("Pixels");
        int pixelsID = pixels.getID();
        
        loadImage(imageID,pixelsID);
    }

    /**
     * Instruct the BrowserAgent to fire a LoadImage event, to be handled
     * by another part of the client.
     * 
     * @param imageID The ID of the image to load (in a viewer, for example)
     */
    public void loadImage(int imageID, int pixelsID)
    {
        LoadImage imageEvent = new LoadImage(imageID,pixelsID);
        EventBus eventBus = registry.getEventBus();
        eventBus.post(imageEvent);
    }
    
    /**
     * Use the DM to show image info about a particular thumbnail.
     * @param t The thumbnail to query.
     */
    public void showImageInfo(Thumbnail t)
    {
        if(t == null) return;
        ThumbnailDataModel tdm = t.getModel();
        ImageSummary is = tdm.getImageInformation();
        showImageInfo(is);
    }
    
    /**
     * Use the DM to visualize the specified ImageSummary data object,
     * located (likely) inside a Thumbnail's ThumbnailDataModel.
     * @param is The image summary to show.
     */
    public void showImageInfo(ImageSummary is)
    {
        if(is == null) return;
        ViewImageInfo imageInfoEvent = new ViewImageInfo(is);
        EventBus eventBus = registry.getEventBus();
        eventBus.post(imageInfoEvent);
    }
    
    /**
     * Use the Annotator to annotate the image currently selected in the
     * specified thumbnail.
     * @param t The thumbnail with the image to annotate.
     */
    public void annotateImage(Thumbnail t)
    {
        if(t == null) return;
        ThumbnailDataModel tdm = t.getModel();
        ImageSummary summary = tdm.getImageInformation();
        annotateImage(summary,null); // default popup location
    }
    
    public void annotateImage(Thumbnail t, Point popupLocation)
    {
        if(t == null) return;
        ThumbnailDataModel tdm = t.getModel();
        ImageSummary summary = tdm.getImageInformation();
        annotateImage(summary,popupLocation);
    }
    
    /**
     * Use the Annotator to annotate the image with the specified ID.
     * @param imageID The ID of the image to annotate.
     */
    public void annotateImage(ImageSummary imageInfo, Point popupLocation)
    {
        AnnotateImage event = new AnnotateImage(imageInfo.getID(),
                                                imageInfo.getName());
        if(popupLocation != null)
        {
            event.setSpecifiedLocation(popupLocation);
        }
        
        // makes sure correct response occurs
        event.setCompletionHandler(new AnnotateImageHandler());
        EventBus eventBus = registry.getEventBus();
        eventBus.post(event);
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
     * Returns the width and height of the size the semantic window onto a node
     * should be, specified in the registry file.  Suggested: 150x150.  Cool.
     * If the value is not specified in the config file, either width or height
     * (or both) will return -1.  Also, if a negative value is specified in
     * the config file, that parameter will be listed as -1.
     * 
     * @return [width,height].
     */
    public int[] getSemanticNodeSize()
        throws NullPointerException
    {
        Integer width = (Integer)registry.lookup(SEMANTIC_WIDTH_KEY);
        Integer height = (Integer)registry.lookup(SEMANTIC_HEIGHT_KEY);
        
        int widthVal, heightVal;
        if(width == null || width.intValue() <= 0)
        {
            widthVal = -1;
        }
        else
        {
            widthVal = width.intValue();
        }
        
        if(height == null || height.intValue() <= 0)
        {
            heightVal = -1;
        }
        else
        {
            heightVal = height.intValue();
        }
        
        return new int[] {widthVal,heightVal};
    }
    
    /**
     * Gets a thumbnail of a different size (same settings)
     * @param width The width of the image with the default thumb settings
     *              to retrieve.
     * @param height The height of the image with the default thumb settings
     *               to retrieve.
     * @return A new composite.
     */
    public Image getResizedThumbnail(Pixels pix, int width, int height)
    {
        if(pix == null)
        {
            return null;
        }
        
        PixelsService ps = registry.getPixelsService();
        try
        {
            return ps.getThumbnail(pix,width,height);
        }
        catch(ImageServerException ise)
        {
            // don't do user notification, make this more subtle
            System.err.println("could not load composite thumbnail");
            return null;
        }
    }
    
    /**
     * Loads the semantic type with the given name.
     * @param typeName
     * @return
     */
    public SemanticType loadTypeInformation(String typeName)
    {
        SemanticTypesService sts = registry.getSemanticTypesService();
        try
        {
            return sts.retrieveSemanticType(typeName);
        }
        catch(DSOutOfServiceException dso)
        {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Connection Error",dso.getMessage(),dso);
        }
        catch(DSAccessException dsa)
        {
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error",dsa.getMessage(),dsa);
        }
        return null; // fallback case
    }
    
    /**
     * TODO: maybe hide this later in favor of doing something that ensures
     *       browser will be notified of changes?
     * @return The STS behind this agent.
     */
    public SemanticTypesService getSemanticTypesService()
    {
        return registry.getSemanticTypesService();
    }
    
    /**
     * Returns a reference to the top frame (TODO: maybe hide desired functions
     * behind the BA, as above)
     * @return A reference to the TopFrame, the overarching container of Shoola.
     */
    public TopFrame getTopFrame()
    {
        return registry.getTopFrame();
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
            loadDataset(event.getDatasetID());
        }
        else if(e instanceof ImageAnnotated)
        {
            ImageAnnotated event = (ImageAnnotated)e;
            event.complete();
        }
    }
}
