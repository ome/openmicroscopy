/*
 * ome.logic.RenderingSettingsImpl
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

// Third-party libraries
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IPixels;
import ome.api.IRenderingSettings;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.Dataset;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.Color;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;
import omeis.providers.re.ColorsFactory;
import omeis.providers.re.Renderer;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.metadata.StatsFactory;
import omeis.providers.re.quantum.QuantumFactory;

/**
 * Implementation of the {@link IRenderingSettings} I/F.
 * 
 * @author Aleksandra Tarkowska <a
 *         href="mailto:a.tarkowska@dundee.ac.uk">a.tarkowska@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: 1696 $ $Date:
 *          2007/09/06 23:27:31 $) </small>
 * @since OME3.0
 */
@RevisionDate("$Date: 2007-09-06 14:29:18 +0100 (Thu, 06 Sep 2007) $")
@RevisionNumber("$Revision: 1792 $")
@Stateless
@Remote(IRenderingSettings.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IRenderingSettings"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IRenderingSettings",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(IRenderingSettings.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IRenderingSettings")
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class RenderingSettingsImpl extends AbstractLevel2Service implements
        IRenderingSettings, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4383698215540637039L;
    
    /** The logger for this class. */
    private transient static Log log = 
        LogFactory.getLog(RenderingSettingsImpl.class);

    /** Reference to the service used to retrieve the pixels data. */
    protected transient PixelsService pixelsData;

    /** Reference to the service used to retrieve the pixels metadata. */
    protected transient IPixels pixelsMetadata;
    
    /** Quantum factory instance for enumeration lookup and verification. */
    protected transient QuantumFactory quantumFactory;
    
    /** An enumerated list of rendering models. */
    protected transient List<RenderingModel> renderingModels;
     
    /**
     * Performs the logic specified by {@link #resetDefaultsInImage(long)}.
     * 
     * @param image The image to handle.
     */
    private void resetDefaults(Image image)
    {
    	if (image == null) return;
    	resetDefaults(image.getPrimaryPixels());
    }
    
    /**
     * Resets a specific set of rendering settings back to those that are 
     * specified by the rendering engine intelligent <i>pretty good image 
     * (PG)</i> logic and a given pixels set.
     * 
     * @param pixels The pixels object whose rendering settings are to be reset.
     * @param settings The rendering settings which are to be reset.
     * @param save Whether or not the rendering settings should be saved.
     * @param computeStats Pass <code>true</code> to compute the stats 
     * 					   determining the input interval, <code>false</code>
     *                     otherwise.
     * @return See above.
     */
    private RenderingDef resetDefaults(RenderingDef settings, Pixels pixels,
                                       boolean save, boolean computeStats)
    {
        List<Family> families = pixelsMetadata.getAllEnumerations(Family.class);
        renderingModels = 
            pixelsMetadata.getAllEnumerations(RenderingModel.class);
        quantumFactory = new QuantumFactory(families);
        try
        {
            PixelBuffer buffer = pixelsData.getPixelBuffer(pixels);
            resetDefaults(settings, pixels, quantumFactory,
                    renderingModels, buffer, computeStats);
            buffer.close();
            
            // Increment the version of the rendering settings so that we 
            // can have some notification that either the RenderingDef 
            // object itself or one of its children in the object graph has 
            // been updated. FIXME: This should be implemented using 
            // IUpdate.touch() or similar once that functionality exists.
            settings.setVersion(settings.getVersion() + 1);
            
            if (save)
            {
                pixelsMetadata.saveRndSettings(settings);
            }
            return settings;
        } 
        catch (IOException e)
        {
            log.debug("An I/O error occurred while attempting to reset " +
                      "rendering settings " + settings + " for pixels set " + 
                      pixels, e);
            throw new ResourceError(
                    e.getMessage() + " Please check server log.");
        }
    }
    
    /**
     * Resets a pixel's rendering settings back to those that are specified by
     * the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * 
     * @param pixels The pixels object whose rendering settings are to be reset.
     */
    private void resetDefaults(Pixels pixels)
    {
    	if (pixels == null) return;
        RenderingDef settings = getRenderingSettings(pixels.getId());
        resetDefaults(settings, pixels, true, true);
	}

	/**
	 * Performs the logic specified by {@link #resetDefaultsInCategory(long)}.
	 * 
	 * @param category The category to handle.
	 * @return The collection of images linked to the category.
	 */
	private Set<Long> resetDefaults(Category category)
	{
		if (category == null) return new HashSet<Long>();
		String sql = "select i from Image i "
			+ " left outer join fetch i.categoryLinks cil "
			+ " left outer join fetch cil.parent c where c.id = :id";
		List<Image> images = 
			iQuery.findAllByQuery(sql, new Parameters().addId(category.getId()));
        Set<Long> imageIds = new HashSet<Long>();
        for (Image i : images)
        {
            imageIds.add(i.getId());
        }
        return resetDefaultsInSet(Image.class, imageIds);
	}

	/**
	 * Performs the logic specified by {@link #resetDefaultsInDataset(long)}.
	 * 
	 * @param dataset The dataset to handle.
	 * @return The collection of images linked to the dataset.
	 */
	private Set<Long> resetDefaults(Dataset dataset)
	{
		if (dataset == null) return new HashSet<Long>();
		String sql = "select i from Image i "
			+ " left outer join fetch i.datasetLinks dil "
			+ " left outer join fetch dil.parent d where d.id = :id";
		List<Image> images = 
			iQuery.findAllByQuery(sql, new Parameters().addId(dataset.getId()));
		Set<Long> imageIds = new HashSet<Long>();
		for (Image i : images)
		{
		    imageIds.add(i.getId());
		}
		return resetDefaultsInSet(Image.class, imageIds);
	}

	/**
     * Resets a pixel's rendering settings back to those that are specified by
     * the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * 
     * @param pixels The pixels object whose rendering settings are to be set.
     */
    private void setOriginalSettings(Pixels pixels)
    {
    	if (pixels == null) return;
        RenderingDef settings = getRenderingSettings(pixels.getId());
        resetDefaults(settings, pixels, true, false);
    }

    /**
     * Performs the logic specified by {@link #resetDefaultsInImage(long)}.
     * 
     * @param image The image to handle.
     */
    private void setOriginalSettings(Image image)
    {
    	if (image == null) return;
    	setOriginalSettings(image.getPrimaryPixels());
    }

    /**
	 * Sets the original settings for the images linked to the specified 
	 * dataset.
	 * 
	 * @param dataset The dataset to handle.
	 * @return The collection of images linked to the dataset.
	 */
	private Set<Long> setOriginalSettings(Dataset dataset)
	{
		if (dataset == null) return new HashSet<Long>();
		String sql = "select i from Image i "
			+ " left outer join fetch i.datasetLinks dil "
			+ " left outer join fetch dil.parent d where d.id = :id";
		List<Image> images = 
			iQuery.findAllByQuery(sql, new Parameters().addId(dataset.getId()));
        Set<Long> imageIds = new HashSet<Long>();
        for (Image image : images)
        {
            imageIds.add(image.getId());
        }
		return setOriginalSettingsInSet(Image.class, imageIds);
	}
	
    /**
     * Resets a rendering definition to its predefined defaults.
     * 
     * @param def The rendering definition to reset.
     * @param pixels The pixels set to reset the definition based upon.
     * @param quantumFactory A populated quantum factory.
     * @param renderingModels An enumerated list of all rendering models.
     * @param buffer A pixel buffer which maps to the <i>planeDef</i>.
     * @param computeStats Pass <code>true</code> to compute the stats,
     * 			           <code>false</code> otherwise
     */
    private void resetDefaults(RenderingDef def, Pixels pixels,
            QuantumFactory quantumFactory, List<RenderingModel> renderingModels,
            PixelBuffer buffer, boolean computeStats) {
        // The default rendering definition settings
        def.setDefaultZ(pixels.getSizeZ() / 2);
        def.setDefaultT(0);
    
        // Set the rendering model to RGB if there is more than one channel,
        // otherwise set it to greyscale.
        RenderingModel defaultModel = null;
        if (pixels.sizeOfChannels() > 1) {
            for (RenderingModel model : renderingModels)
            {
                if (model.getValue().equals(Renderer.MODEL_HSB))
                    defaultModel = model;
            }
        } else {
            for (RenderingModel model : renderingModels)
            {
                if (model.getValue().equals(Renderer.MODEL_GREYSCALE))
                    defaultModel = model;
            }
        }
        if (defaultModel == null)
        {
            throw new IllegalArgumentException(
                "Unable to find default rendering model in enumerated list.");
        }
        def.setModel(defaultModel);
    
        // Quantization settings
        QuantumDef quantumDef = def.getQuantization();
        quantumDef.setCdStart(0);
        quantumDef.setCdEnd(QuantumFactory.DEPTH_8BIT);
        quantumDef.setBitResolution(QuantumFactory.DEPTH_8BIT);
        def.setQuantization(quantumDef);
    
        // Reset the channel bindings
        resetChannelBindings(def, pixels, quantumFactory, buffer, computeStats);
    }
    
    /**
     * Resets the channel bindings for the current active pixels set.
     * 
     * @param def
     *            the rendering definition to link to.
     * @param pixels
     *            the pixels set to reset the bindings based upon.
     * @param quantumFactory
     *            a populated quantum factory.
     * @param buffer
     *            a pixel buffer which maps to the <i>planeDef</i>.
     * @param computeStats 
     * 			  Pass <code>true</code> to compute the stats,
     * 			  <code>false</code> otherwise
     */
    private void resetChannelBindings(RenderingDef def, Pixels pixels,
            QuantumFactory quantumFactory, PixelBuffer buffer, boolean
            computeStats) {
        // The actual channel bindings we are returning
        List<ChannelBinding> 
        	channelBindings = def.<ChannelBinding>collectWaveRendering(null);
    
        // Default plane definition for our rendering definition
        PlaneDef planeDef = getDefaultPlaneDef(def);
    
        int i = 0;
        ChannelBinding channelBinding;
        for (Channel channel : pixels.<Channel>collectChannels(null)) {
            Family family = quantumFactory.getFamily(QuantumFactory.LINEAR);
    
            channelBinding = channelBindings.get(i);
            channelBinding.setFamily(family);
            channelBinding.setCoefficient(new Double(1));
    
            // If we have more than one channel set each of the first three
            // active, otherwise only activate the first.
            if (i < 3) {
                channelBinding.setActive(true);
            } else {
                channelBinding.setActive(false);
            }
    
            // Handle updating or recreating a color for this channel.
            Color defaultColor = ColorsFactory.getColor(i, channel);
            if (channelBinding.getColor() == null) {
                channelBinding.setColor(ColorsFactory.getColor(i, channel));
            } else {
                Color color = channelBinding.getColor();
                color.setRed(defaultColor.getRed());
                color.setGreen(defaultColor.getGreen());
                color.setBlue(defaultColor.getBlue());
                color.setAlpha(defaultColor.getAlpha());
            }
            channelBinding.setNoiseReduction(false);
            i++;
        }
    
        // Set the input start and input end for each channel binding based upon
        // the computation of the pixels set's location statistics.
        if (computeStats)
        	computeLocationStats(pixels, channelBindings, planeDef, buffer);
        else {
        	StatsInfo stats;
            for (int w = 0; w < pixels.sizeOfChannels(); w++) {
                // FIXME: This is where we need to have the ChannelBinding -->
                // Channel linkage. Without it, we have to assume that the order in
                // which the channel bindings was created matches up with the order
                // of the channels linked to the pixels set.
            	channelBinding = channelBindings.get(w);
            	stats = pixels.getChannel(w).getStatsInfo();
            	 if (stats == null)
                 	throw new ResourceError("Pixels set is missing statistics" +
                 			" for channel '"+ w +"'. This suggests an image " +
                 		    "import error or failed image import.");
            	 channelBinding.setInputStart(stats.getGlobalMin().floatValue());
            	 channelBinding.setInputEnd(stats.getGlobalMax().floatValue());
            }
        }
    }
    
    /**
     * Computes the location statistics for a set of rendering settings.
     * 
     * @param pixels	The pixels set.
     * @param cbs		The collection of settings corresponding to channel.
     * @param planeDef	The 2D-plane. Mustn't be <code>null</code>
     * @param buf		The buffer.
     */
    private void computeLocationStats(Pixels pixels,
            List<ChannelBinding> cbs, PlaneDef planeDef, PixelBuffer buf) {
        if (planeDef == null) {
            throw new NullPointerException("No plane definition.");
        }
        StatsFactory sf = new StatsFactory();
        ChannelBinding cb;
        for (int w = 0; w < pixels.sizeOfChannels(); w++) {
            // FIXME: This is where we need to have the ChannelBinding -->
            // Channel linkage. Without it, we have to assume that the order in
            // which the channel bindings was created matches up with the order
            // of the channels linked to the pixels set.
            cb = cbs.get(w);
            sf.computeLocationStats(pixels, buf, planeDef, w);
            cb.setNoiseReduction(sf.isNoiseReduction());
            cb.setInputStart(new Float(sf.getInputStart()));
            cb.setInputEnd(new Float(sf.getInputEnd()));
        }
    }
    
    /**
     * Creates the default plane definition to use for generation of the very
     * first image displayed by <i>2D</i> viewers based upon a rendering
     * definition.
     * 
     * @param renderingDef
     *            the rendering definition to base the plane definition upon.
     * @return The default <i>XY</i>-plane for the <i>renderingDef</i>.
     */
    private PlaneDef getDefaultPlaneDef(RenderingDef renderingDef) {
        PlaneDef pd = new PlaneDef(PlaneDef.XY, renderingDef.getDefaultT());
        pd.setZ(renderingDef.getDefaultZ());
        return pd;
    }

    /**
     * Creates new channel bindings for each channel in the pixels set.
     * 
     * @param p
     *            the pixels set to create channel bindings based upon.
     * @return a new set of blank channel bindings.
     */
    private List<ChannelBinding> createNewChannelBindings(Pixels p) {
        List<ChannelBinding> cbs = new ArrayList<ChannelBinding>();
        ChannelBinding binding;
        for (int i = 0; i < p.getSizeC(); i++) {
            binding = new ChannelBinding();
            binding.setColor(new Color());
            cbs.add(binding);
        }
        return cbs;
    }
    
    /**
     * Loads objects from the Hibernate store in a list context.
     * @param klass The type of object to load.
     * @param nodeIds The object IDs to load.
     * @return A typed {@link java.util.List} of objects retrieved from the
     * Hibernate store.
     */
    private <T extends IObject> List<T> loadObjects(Class<T> klass, 
    												Set<Long> nodeIds)
    {
    	List<T> toReturn = new ArrayList<T>();
    	for (Long nodeId : nodeIds)
    	{
    		toReturn.add(iQuery.get(klass, nodeId));
    	}
    	return toReturn;
    }

    /**
     * Applies the settings to the passed collection of Images. Returns a map
     * with two keys: A <code>True</code> key whose value is a list of image's
     * ids the settings were successfully applied to, a <code>False</code> key
     * whose value is a list of image's ids the settings could not be applied
     * to.
     * 
     * @param from
     *            The image to copy the settings from.
     * @param images
     *            The collection of image to copy the settings to.
     * @return See above.
     */
    private Map<Boolean, List<Long>> applySettings(long from, Set<Image> images) {

        if (images.isEmpty())
            throw new ValidationException("Target does not contain any Images.");

        List<Long> trueList = new ArrayList<Long>();
        List<Long> falseList = new ArrayList<Long>();

        try {
            Iterator<Image> i = images.iterator();
            Image image;
            boolean r;
            while (i.hasNext()) {
                image = i.next();
                try {
                    r = applySettingsToImage(from, image.getId());
                    if (r)
                        trueList.add(image.getId());
                    else
                        falseList.add(image.getId());
                } catch (Exception e) {
                    falseList.add(image.getId());
                }
            }
        } catch (NoSuchElementException expected) {
            throw new ApiUsageException(
                    "There are no elements assigned to the Dataset");
        }

        Map<Boolean, List<Long>> result = new HashMap<Boolean, List<Long>>();
        result.put(Boolean.TRUE, trueList);
        result.put(Boolean.FALSE, falseList);

        return result;
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#sanityCheckPixels(Pixels, Pixels)
     */
    public boolean sanityCheckPixels(Pixels pFrom, Pixels pTo) {
        if (pTo == null || pFrom == null)
            return false;
        String vFrom = pFrom.getPixelsType().getValue();
        String vTo = pTo.getPixelsType().getValue();
        if (!vFrom.equals(vTo))
            return false;
        if (pFrom.getSizeC().compareTo(pTo.getSizeC()) != 0)
            return false;
        Iterator<Channel> i = pFrom.iterateChannels();
        Channel c;
        List<Integer> wavelengths = new ArrayList<Integer>(pFrom
                .sizeOfChannels());
        // Problem no access to channel index.
        LogicalChannel lc;
        while (i.hasNext()) {
            c = i.next();
            lc = c.getLogicalChannel();
            if (lc != null)
                wavelengths.add(lc.getEmissionWave());
        }
        i = pTo.iterateChannels();
        int r = 0;
        while (i.hasNext()) {
            c = i.next();
            lc = c.getLogicalChannel();
            if (lc != null && wavelengths.contains(lc.getEmissionWave()))
                r++;
        }
        if (r != wavelengths.size())
            return false;
        return true;
    }

    /**
     * Sets injector. For use during configuration. Can only be called once.
     * 
     * @param metaService
     *            The value to set.
     */
    @RolesAllowed("user")
    public void setPixelsMetadata(IPixels metaService) {
        getBeanHelper().throwIfAlreadySet(this.pixelsMetadata, metaService);
        pixelsMetadata = metaService;
    }

    /**
     * Sets injector. For use during configuration. Can only be called once.
     * 
     * @param dataService
     *            The value to set.
     */
    @RolesAllowed("user")
    public void setPixelsData(PixelsService dataService) {
        getBeanHelper().throwIfAlreadySet(this.pixelsData, dataService);
        pixelsData = dataService;
    }

    /**
     * Returns the interface this implementation is for.
     * @see AbstractLevel2Service#getServiceInterface()
     */
    @RolesAllowed("user")
    public Class<? extends ServiceInterface> getServiceInterface() {
        return IRenderingSettings.class;
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * @see IRenderingSettings#applySettingsToSet(long, Class, Set)
     */
    @RolesAllowed("user")
    public <T extends IObject> void applySettingsToSet(long from, 
    		Class<T> toType, Set<T> to) {
        // TODO Auto-generated method stub

    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * @see IRenderingSettings#applySettingsToCategory(long, long)
     */
    @RolesAllowed("user")
    public Map<Boolean, List<Long>> applySettingsToCategory(long from, long to) {

        String sql = "select i from Image i "
                + " left outer join fetch i.categoryLinks cil "
                + " left outer join fetch cil.parent c " + " where c.id = :id";
        List<Image> images =
        	iQuery.findAllByQuery(sql, new Parameters().addId(to));
        return applySettings(from, new HashSet<Image>(images));
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F. 
     * @see IRenderingSettings#applySettingsToProject(long, long)
     */
    @RolesAllowed("user")
    public Map<Boolean, List<Long>> applySettingsToProject(long from, long to) {

        String sql = "select i from Image i "
                + " left outer join fetch i.datasetLinks dil "
                + " left outer join fetch dil.parent d "
                + " left outer join fetch d.projectLinks pdl "
                + " left outer join fetch pdl.parent pr "
                + " where pr.id = :id";
        List<Image> images =
        	iQuery.findAllByQuery(sql, new Parameters().addId(to));
        return applySettings(from, new HashSet<Image>(images));
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * @see IRenderingSettings#applySettingsToDataset(long, long)
     */
    @RolesAllowed("user")
    public Map<Boolean, List<Long>> applySettingsToDataset(long from, long to) {
        String sql = "select i from Image i "
                + " left outer join fetch i.datasetLinks dil "
                + " left outer join fetch dil.parent d where d.id = :id";
        List<Image> images = 
        	iQuery.findAllByQuery(sql, new Parameters().addId(to));
        return applySettings(from, new HashSet<Image>(images));
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#applySettingsToImage(long, long)
     */
    @RolesAllowed("user")
    public boolean applySettingsToImage(long from, long to) {
        Image img = iQuery.get(Image.class, to);
        return applySettingsToPixels(from, img.getPrimaryPixels().getId());
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#applySettingsToPixels(long, long)
     */
    @RolesAllowed("user")
    public boolean applySettingsToPixels(long from, long to) {

        Pixels pTo = pixelsMetadata.retrievePixDescription(to);
        Pixels pFrom = pixelsMetadata.retrievePixDescription(from);

        boolean b = sanityCheckPixels(pFrom, pTo);
        if (!b)
            return false;
        // get rendering settings from RenderingDef to PixelId
        RenderingDef rdFrom = pixelsMetadata.retrieveRndSettings(from);
        RenderingDef rdTo = pixelsMetadata.retrieveRndSettings(to);

        if (rdFrom == null)
            return false;
        // pixelsMetadata.
        // Controls
        if (rdTo == null) {
            // create Rnd Settings.
            rdTo = createNewRenderingDef(pTo);
        }
        rdTo.setModel(rdFrom.getModel());

        QuantumDef qDefFrom = rdFrom.getQuantization();
        QuantumDef qDefTo = rdTo.getQuantization();

        qDefTo.setBitResolution(qDefFrom.getBitResolution());
        qDefTo.setCdEnd(qDefFrom.getCdEnd());
        qDefTo.setCdStart(qDefFrom.getCdStart());

        Iterator<ChannelBinding> i = rdFrom.iterateWaveRendering();
        Iterator<ChannelBinding> iTo = rdTo.iterateWaveRendering();
        ChannelBinding binding, bindingTo;
        Color cFrom, cTo;
        while (i.hasNext()) {
            binding = i.next();
            bindingTo = iTo.next();

            // channel on or off
            bindingTo.setActive(binding.getActive());
            // mapping coefficient
            bindingTo.setCoefficient(binding.getCoefficient());
            // type of map used
            bindingTo.setFamily(binding.getFamily());
            // lower bound of the pixels intensity interval
            bindingTo.setInputStart(binding.getInputStart());
            // upper bound of the pixels intensity interval
            bindingTo.setInputEnd(binding.getInputEnd());
            // turn on or off the noise reduction algo.
            bindingTo.setNoiseReduction(binding.getNoiseReduction());
            // color used
            cFrom = binding.getColor();
            cTo = bindingTo.getColor();
            cTo.setAlpha(cFrom.getAlpha());
            cTo.setBlue(cFrom.getBlue());
            cTo.setGreen(cFrom.getGreen());
            cTo.setRed(cFrom.getRed());
        }
        
        // Increment the version of the rendering settings so that we 
        // can have some notification that either the RenderingDef 
        // object itself or one of its children in the object graph has 
        // been updated. FIXME: This should be implemented using 
        // IUpdate.touch() or similar once that functionality exists.
        rdTo.setVersion(rdTo.getVersion() + 1);
        
        pixelsMetadata.saveRndSettings(rdTo);
        return true;
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#getRenderingSettings(long)
     */
    @RolesAllowed("user")
    public RenderingDef getRenderingSettings(long pixelsId) {
        return pixelsMetadata.retrieveRndSettings(pixelsId);
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#createNewRenderingDef(Pixels)
     */
    public RenderingDef createNewRenderingDef(@NotNull Pixels pixels) {
        RenderingDef r = new RenderingDef();
        //The default rendering definition settings
        r.setDefaultZ(pixels.getSizeZ() / 2);
        r.setDefaultT(0);
        r.setQuantization(new QuantumDef());

        List<ChannelBinding> list = createNewChannelBindings(pixels);
        r.clearWaveRendering();
        for (ChannelBinding channelBinding : list) {
            r.addChannelBinding(channelBinding);
        }
        // Unload the pixels object to avoid transactional headaches
        Pixels unloadedPixels = new Pixels();
        unloadedPixels.setId(pixels.getId());
        unloadedPixels.unload();
        r.setPixels(unloadedPixels);
        return r;
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#resetDefaults(RenderingDef, Pixels)
     */
    @RolesAllowed("user")
    public void resetDefaults(RenderingDef def, Pixels pixels) {
        resetDefaults(def, pixels, true, true);
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#resetDefaultsNoSave(RenderingDef, Pixels)
     */
    @RolesAllowed("user")
    public RenderingDef resetDefaultsNoSave(RenderingDef def, Pixels pixels) {
        return resetDefaults(def, pixels, false, true);
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * @see IRenderingSettings#resetDefaultsInImage(long)
     */
    @RolesAllowed("user")
    public void resetDefaultsInImage(long to) {
        Image image = iQuery.get(Image.class, to);
        resetDefaults(image);
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * 
     * @see IRenderingSettings#resetDefaultsInDataset(long)
     */
    @RolesAllowed("user")
    public Set<Long> resetDefaultsInDataset(long dataSetId) {
    	Dataset dataset = iQuery.get(Dataset.class, dataSetId);
    	return resetDefaults(dataset);
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#resetDefaultsInCategory(long)
     */
    @RolesAllowed("user")
    public Set<Long> resetDefaultsInCategory(long categoryId) {
    	Category category = iQuery.get(Category.class, categoryId);
    	return resetDefaults(category);
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F. 
     * @see IRenderingSettings#resetDefaultsInSet(Class, Set)
     */
    @RolesAllowed("user")
    public <T extends IObject> Set<Long> resetDefaultsInSet(Class<T> klass, Set<Long> nodeIds)
    {
        if (!Dataset.class.equals(klass) && !Category.class.equals(klass)
            && !Image.class.equals(klass))
        {
            throw new IllegalArgumentException(
                    "Class parameter for resetDefaultsInSet() must be in "
                            + "{Dataset, Category, Image}, not " + klass);
        }

        List<IObject> objects = new ArrayList<IObject>();
        for (Long nodeId : nodeIds)
        {
            objects.add(iQuery.get(klass, nodeId));
        }
        Set<Long> imageIds = new HashSet<Long>();
        for (IObject object : objects)
        {
            try
            {
                if (object instanceof Dataset)
                {
                    imageIds.addAll(resetDefaults((Dataset) object));
                }
                if (object instanceof Category)
                {
                    imageIds.addAll(resetDefaults((Category) object));
                }
                if (object instanceof Image)
                {
                    Image image = (Image) object;
                    resetDefaults(image);
                    imageIds.add(image.getId());
                }
            }
            catch (Throwable t)
            {
                log.error("Error while resetting defaults.", t);
            }
        }
        return imageIds;
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * @see IRenderingSettings#setOriginalSettingsInImage(long)
     */
    @RolesAllowed("user")
    public void setOriginalSettingsInImage(long to) {
        setOriginalSettings(iQuery.get(Image.class, to));
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F.
     * @see IRenderingSettings#setOriginalSettingsInDataset(long)
     */
    @RolesAllowed("user")
    public Set<Long> setOriginalSettingsInDataset(long to) {
        return setOriginalSettings(iQuery.get(Dataset.class, to));
    }
    
    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F. 
     * @see IRenderingSettings#setOriginalSettingsInSet(Class, Set)
     */
    @RolesAllowed("user")
    public <T extends IObject> Set<Long> setOriginalSettingsInSet(
            Class<T> klass, Set<Long> nodeIds)
    {
    	if (!Dataset.class.equals(klass) && !Image.class.equals(klass))
    		throw new IllegalArgumentException(
    				"Class parameter for resetDefaultsInSet() must be in "
    				+ "{Dataset, Image}, not " + klass);

    	List<IObject> objects = new ArrayList<IObject>();
        for (Long nodeId : nodeIds)
        {
            objects.add(iQuery.get(klass, nodeId));
        }
    	Set<Long> imageIds = new HashSet<Long>();
    	Image image;
    	for (IObject object : objects) {
    	    try {
    		if (object instanceof Dataset) {
    			imageIds.addAll(setOriginalSettings((Dataset) object));
    		} else if (object instanceof Image) {
    			image = (Image) object;
    			setOriginalSettings(image);
    			imageIds.add(image.getId());
    		}
    	    } catch (Throwable t) {
    	        log.error("Error while resetting original settings.", t);
    	    }
    	}
    	return imageIds;
    }
    
}
