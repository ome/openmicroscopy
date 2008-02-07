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

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IPixels;
import ome.api.IRenderingSettings;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
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
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;
import omeis.providers.re.Renderer;
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

    /** Reference to the service used to retrieve the pixels data. */
    private PixelsService pixelsData;

    /** Reference to the service used to retrieve the pixels metadata. */
    private IPixels pixelsMetadata;

    /**
     * Checks if the specified sets of pixels are compatible. Returns
     * <code>true</code> if the pixels type is valid, <code>false</code>
     * otherwise.
     * 
     * @param pFrom
     *            The pixels set to copy the settings from.
     * @param pTo
     *            The pixels set to copy the settings to.
     * @return See above.
     */
    private boolean sanityCheckPixels(Pixels pFrom, Pixels pTo) {
        if (pTo == null || pFrom == null)
            return false;
        // throw new ValidationException("Pixels not valid.");
        String vFrom = pFrom.getPixelsType().getValue();
        String vTo = pTo.getPixelsType().getValue();
        if (!vFrom.equals(vTo))
            return false;
        // throw new ValidationException("Pixels type must be the same.");
        if (pFrom.getSizeC().compareTo(pTo.getSizeC()) != 0)
            return false;
        // throw new ValidationException("The Pixels sets must have the " +
        // "same number of channels.");
        if (pFrom.getSizeX().compareTo(pTo.getSizeX()) != 0)
            return false;
        // throw new ValidationException("The Pixels sets must have the " +
        // "same number of pixels along the X-axis.");
        if (pFrom.getSizeY().compareTo(pTo.getSizeY()) != 0)
            return false;
        // throw new ValidationException("The Pixels sets must have the " +
        // "same number of pixels along the Y-axis.");
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
        // throw new ValidationException("Emission wavelengths must match.");
        return true;
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
     * 
     * @see AbstractLevel2Service#getServiceInterface()
     */
    @RolesAllowed("user")
    public Class<? extends ServiceInterface> getServiceInterface() {
        return IRenderingSettings.class;
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#applySettingsToSet(long, Class, Set)
     */
    @RolesAllowed("user")
    public <T extends IObject> void applySettingsToSet(long from, Class<T> toType, Set<T> to) {
        // TODO Auto-generated method stub

    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
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
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
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
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
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
        return applySettingsToPixel(from, img.getPrimaryPixels().getId());
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#applySettingsToPixel(long, long)
     */
    @RolesAllowed("user")
    public boolean applySettingsToPixel(long from, long to) {

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
            rdTo = Renderer.createNewRenderingDef(pTo);
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
     * @see IRenderingSettings#resetDefaultsInImage(long)
     */
    @RolesAllowed("user")
    public void resetDefaultsInImage(long to) {
        Image image = iQuery.get(Image.class, to);
        resetDefaults(image);
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
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
     * Performs the logic specified by {@link resetDefaultsInImage()}.
     */
    private void resetDefaults(Image image)
    {
    	resetDefaults(image.getPrimaryPixels());
    }
    
    /**
     * Resets a pixel's rendering settings back to those that are specified by
     * the rendering engine intelligent <i>pretty good image (PG)</i> logic.
     * 
     * @param pixels The pixels object whose rendering settings are to be reset.
     */
    private void resetDefaults(Pixels pixels) {
	
	    Pixels pixelsObj = 
	    	pixelsMetadata.retrievePixDescription(pixels.getId());
	
	    // Ensure that we haven't just been called before
	    // lookupRenderingDef().
	    List<Family> families = pixelsMetadata.getAllEnumerations(Family.class);
	    List<RenderingModel> renderingModels = pixelsMetadata
	            .getAllEnumerations(RenderingModel.class);
	    QuantumFactory quantumFactory = new QuantumFactory(families);
	    try {
	        PixelBuffer buffer = pixelsData.getPixelBuffer(pixelsObj);
	        RenderingDef def = getRenderingSettings(pixels.getId());
	        Renderer.resetDefaults(def, pixelsObj, quantumFactory,
	                renderingModels, buffer);
	        buffer.close();
	        pixelsMetadata.saveRndSettings(def);
	    } catch (IOException e) {
	        throw new ValidationException(e.getMessage());
	    }
	
	}

	/**
	 * Performs the logic specified by {@link resetDefaultsInCategory()}.
	 */
	private Set<Long> resetDefaults(Category category)
	{
		String sql = "select i from Image i "
			+ " left outer join fetch i.categoryLinks cil "
			+ " left outer join fetch cil.parent c where c.id = :id";
		List<Image> images = 
			iQuery.findAllByQuery(sql, new Parameters().addId(category.getId()));
		return resetDefaults(new HashSet<Image>(images));
	}

	/**
	 * Performs the logic specified by {@link resetDefaultsInDataset()}.
	 */
	private Set<Long> resetDefaults(Dataset dataset)
	{
		String sql = "select i from Image i "
			+ " left outer join fetch i.datasetLinks dil "
			+ " left outer join fetch dil.parent d where d.id = :id";
		List<Image> images = 
			iQuery.findAllByQuery(sql, new Parameters().addId(dataset.getId()));
		return resetDefaults(new HashSet<Image>(images));
	}

	/**
     * Resets a rendering settings back to one or many <code>Images</code>
     * that are specified by the rendering engine intelligent <i>pretty good
     * image (PG)</i> logic.
     * 
     * @param images A {@link java.util.Set} of images to reset the rendering
     * settings.
     * @return A {@link java.util.Set} of image IDs that have had their
     * rendering settings reset.
     */
	private Set<Long> resetDefaults(Set<Image> images) {
		if (images.isEmpty())
			throw new ValidationException("Target does not contain any Images.");
		Set<Long> imageIds = new HashSet<Long>();
		for (Image image : images) {
			resetDefaultsInImage(image.getId());
		}
		return imageIds;
	}
    
    /**
     * Loads objects from the Hibernate store in a list context.
     * @param klass The type of object to load.
     * @param nodeIds The object IDs to load.
     * @return A typed {@link java.util.List} of objects retrieved from the
     * Hibernate store.
     */
    private <T extends IObject> List<T> loadObjects(Class<T> klass, Set<Long> nodeIds)
    {
    	List<T> toReturn = new ArrayList<T>();
    	for (Long nodeId : nodeIds)
    	{
    		toReturn.add(iQuery.get(klass, nodeId));
    	}
    	return toReturn;
    }

    /**
     * Implemented as specified by the {@link IRenderingSettings} I/F
     * 
     * @see IRenderingSettings#resetDefaultsInSet(Class, Set)
     */
    @RolesAllowed("user")
    public Set<Long> resetDefaultsInSet(Class<IObject> klass, Set<Long> nodeIds)
    {
        if (!Dataset.class.equals(klass) && !Category.class.equals(klass)
            && !Image.class.equals(klass))
        {
            throw new IllegalArgumentException(
                    "Class parameter for resetDefaultsInSet() must be in "
                            + "{Dataset, Category, Image}, not " + klass);
        }

        List<IObject> objects = loadObjects(klass, nodeIds);
        Set<Long> imageIds = new HashSet<Long>();
        for (IObject object : objects)
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
        return imageIds;
    }
}
