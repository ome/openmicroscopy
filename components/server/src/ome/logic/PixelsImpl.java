/*
 * ome.logic.PixelsImpl
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

// Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.api.IPixels;
import ome.api.ServiceInterface;
import ome.conditions.ValidationException;
import ome.io.nio.PixelsService;
import ome.model.IObject;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.services.util.OmeroAroundInvoke;

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/12 23:27:31 $) </small>
 * @since OME2.2
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Stateless
@Remote(IPixels.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IPixels"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IPixels",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(IPixels.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IPixels")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class PixelsImpl extends AbstractLevel2Service implements IPixels {

    public Class<? extends ServiceInterface> getServiceInterface() {
        return IPixels.class;
    }
    
    /** Standard rendering definition HQL query prefix */
    private static final String RENDERING_DEF_QUERY_PREFIX =
        "select rdef from RenderingDef as rdef " + 
        "left outer join fetch rdef.quantization " + 
        "left outer join fetch rdef.model " +
        "left outer join fetch rdef.waveRendering as cb " +
        "left outer join fetch cb.color " +
        "left outer join fetch cb.family " +
        "left outer join fetch rdef.spatialDomainEnhancement where ";
    
    // ~ Service methods
    // =========================================================================

    @RolesAllowed("user")
    public Pixels retrievePixDescription(long pixId) {
        Pixels p = iQuery.findByQuery("select p from Pixels as p "
                + "left outer join fetch p.pixelsType as pt "
                + "left outer join fetch p.channels as c "
                + "left outer join fetch p.pixelsDimensions "
                + "left outer join fetch p.pixelsFileMaps as m "
                + "left outer join fetch m.parent as f "
                + "left outer join fetch f.format "
                + "left outer join fetch c.colorComponent "
                + "left outer join fetch c.logicalChannel as lc "
                + "left outer join fetch c.statsInfo "
                + "left outer join fetch lc.photometricInterpretation "
                + "where p.id = :id",
                new Parameters().addId(pixId));
        return p;
    }

    @RolesAllowed("user")
    public RenderingDef retrieveRndSettings(long pixId) {
        Long userId = getSecuritySystem().getEventContext().getCurrentUserId();
        Parameters params = new Parameters();
        params.addLong("p_id", pixId);
        params.addLong("o_id", userId);

        List<RenderingDef> l = iQuery.findAllByQuery(
                RENDERING_DEF_QUERY_PREFIX +
                "rdef.pixels.id = :p_id and rdef.details.owner.id = :o_id " +
                "order by rdef.details.updateEvent.time", params);
        if (l.size() > 0)
        {
            return l.get(0);
        }
        return null;
    }

    @RolesAllowed("user")
    public RenderingDef loadRndSettings(long renderingDefId) {
        return (RenderingDef) iQuery.findByQuery(
                RENDERING_DEF_QUERY_PREFIX + "rdef.id = :id",
                new Parameters().addId(renderingDefId));
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public Long copyAndResizePixels(long pixelsId, Integer sizeX, Integer sizeY,
            Integer sizeZ, Integer sizeT, List<Integer> channelList, String methodology)
    {
        Pixels from = retrievePixDescription(pixelsId);
        Pixels to = new Pixels();
        
        //      Ensure we have no values out of bounds
        outOfBoundsCheck(sizeX, "sizeX");
        outOfBoundsCheck(sizeY, "sizeY");
        outOfBoundsCheck(sizeZ, "sizeZ");
        outOfBoundsCheck(sizeT, "sizeT");
        // Check that the channels in the list are valid indexes. 
        if(channelList!=null)
        	channelOutOfBounds(channelList, "channel", from);
        
        // Copy basic metadata
        to.setDimensionOrder(from.getDimensionOrder());
        to.setMethodology(methodology);
        to.setPixelsDimensions(from.getPixelsDimensions());
        to.setPixelsType(from.getPixelsType());
        to.setRelatedTo(from);
        to.setSizeX(sizeX != null? sizeX : from.getSizeX());
        to.setSizeY(sizeY != null? sizeY : from.getSizeY());
        to.setSizeZ(sizeZ != null? sizeZ : from.getSizeZ());
        to.setSizeT(sizeT != null? sizeT : from.getSizeT());
        to.setSizeC(channelList!= null ? channelList.size() : from.getSizeC());
        to.setSha1("Pending...");
        
        // Deal with Image linkage
        Image image = from.getImage();
        image.addPixels(to);
        
        // Copy channel data, if the channel list is null copy all channels.
        // or copy the channels in the channelList if it's not null.
        if(channelList!=null)
        	for(Integer channel : channelList)
        		copyChannel(channel, from, to);
        else
        	for(int channel = 0 ; channel < from.sizeOfChannels(); channel++)
        		copyChannel(channel, from, to);
     
        // Save and return our newly created Pixels Id
        image = iUpdate.saveAndReturnObject(image);
        return image.getPixels(image.sizeOfPixels() - 1).getId();
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void saveRndSettings(RenderingDef rndSettings) {
        iUpdate.saveAndReturnObject(rndSettings);
    }

    @RolesAllowed("user")
    public int getBitDepth(PixelsType pixelsType) {
        return PixelsService.getBitDepth(pixelsType);
    }

    @RolesAllowed("user")
    public <T extends IObject> T getEnumeration(Class<T> klass, String value) {
        return iQuery.findByString(klass, "value", value);
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getAllEnumerations(Class<T> klass) {
        return iQuery.findAll(klass, null);
    }
    
    /**
     * Ensures that a particular dimension value is not out of range (ex. less
     * than zero).
     * @param value The value to check.
     * @param name The name of the value to be used for error reporting.
     * @throws ValidationException If <code>value</code> is out of range.
     */
    private void outOfBoundsCheck(Integer value, String name)
    {
        if (value != null && value < 0)
        {
            throw new ValidationException(name + ": " + value + " <= 0");
        }
    }
    
    /**
     * Ensures that a particular dimension value in a list is not out of 
     * range (ex. less than zero).
     * @param channelList The list of channels to check.
     * @param name The name of the value to be used for error reporting.
     * @param pixels the pixels the channel list belongs to.
     * @throws ValidationException If <code>value</code> is out of range.
     */
    private void channelOutOfBounds(List<Integer> channelList, String name, 
    		Pixels pixels)
    {
    	if(channelList.size() == 0)
    		throw new ValidationException("Channel List is not null but size == 0");
    	for(int i = 0 ; i < channelList.size() ; i++)
    	{
    		int value = channelList.get(i);
    		if (value < 0 || value >= pixels.sizeOfChannels())
    			throw new ValidationException(name + ": " + i + " out of bounds");
    	}
    }
    
    /**
     * Copy the channel from the pixels to the pixels called to.
     * @param channel the channel index.
     * @param from the pixel to copy from.
     * @param to the pixels to copy to.
     */
    private void copyChannel(int channel, Pixels from, Pixels to)
    {
       	Channel cFrom = from.getChannel(channel);
        Channel cTo = new Channel();
        cTo.setColorComponent(cFrom.getColorComponent());
        cTo.setLogicalChannel(cFrom.getLogicalChannel());
        cTo.setStatsInfo(new StatsInfo(cFrom.getStatsInfo().getGlobalMin(),
        						cFrom.getStatsInfo().getGlobalMax()));
        to.addChannel(cTo);
    }

}
