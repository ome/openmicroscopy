package ome.services.projection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.List;

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

import ome.api.IPixels;
import ome.api.IProjection;
import ome.api.ServiceInterface;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelData;
import ome.io.nio.PixelsService;
import ome.logic.AbstractLevel2Service;
import ome.logic.SimpleLifecycle;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.services.util.OmeroAroundInvoke;

/**
 * Implements projection functionality for Pixels sets as declared in {@link
 * IProjection}.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO-Beta3.1
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = true)
@Stateless
@Remote(IProjection.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IProjection"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IProjection",
           clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(IProjection.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IProjection")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class ProjectionBean extends AbstractLevel2Service implements IProjection
{
    /** The logger for this class. */
    private static Log log = LogFactory.getLog(ProjectionBean.class);
    
    /** Reference to the service used to retrieve the pixels metadata. */
    protected transient IPixels iPixels;
    
    /** Reference to the service used to retrieve the pixels data. */
    private transient PixelsService pixelsService;
    
    /** The Pixels set we're currently working on. */
    private Pixels pixels;
    
    /** The Pixel data we're currently working with. */
    private PixelData pixelData;
    
    /**
     * Returns the interface this implementation is for.
     * @see AbstractLevel2Service#getServiceInterface()
     */
    public Class<? extends ServiceInterface> getServiceInterface()
    {
        return IProjection.class;
    }
    
    /**
     * IPixels bean injector. For use during configuration. Can only be called 
     * once.
     */
    public void setIPixels(IPixels iPixels)
    {
        getBeanHelper().throwIfAlreadySet(this.iPixels, iPixels);
        this.iPixels = iPixels;
    }
    
    /**
     * PixelsService bean injector. For use during configuration. Can only be 
     * called once.
     */
    public void setPixelsService(PixelsService pixelsService)
    {
        getBeanHelper().throwIfAlreadySet(this.pixelsService, pixelsService);
        this.pixelsService = pixelsService;
    }
    
    /* (non-Javadoc)
     * @see ome.api.IProjection#projectStack(long, int, int, int, int, int, int)
     */
    @RolesAllowed("user")
    public byte[] projectStack(long pixelsId, int algorithm, int timepoint,
                               int channelIndex, int stepping,
                               int start, int end)
    {
        pixels = iQuery.get(Pixels.class, pixelsId);
        PixelBuffer pixelBuffer = pixelsService.getPixelBuffer(pixels);
        try
        {
            pixelData = pixelBuffer.getStack(channelIndex, timepoint);
            return projectStackMax(pixelData, stepping, start, end);
        }
        catch (IOException e)
        {
            String error = String.format(
                    "I/O error retrieving stack C=%d T=%d: %s",
                    channelIndex, timepoint, e.getMessage());
            log.error(error, e);
            throw new ResourceError(error);
        }
        catch (DimensionsOutOfBoundsException e)
        {
            String error = String.format(
                    "C=%d or T=%d out of range for Pixels Id %d: %s",
                    channelIndex, timepoint, pixels.getId(), e.getMessage());
            log.error(error, e);
            throw new ValidationException(error);
        }
    }

    /* (non-Javadoc)
     * @see ome.api.IProjection#projectPixels(long, int, int, int, java.util.List, int, int, int, java.lang.String)
     */
    @RolesAllowed("user")
    public long projectPixels(long pixelsId, int algorithm, int tStart,
                              int tEnd, List<Integer> channels, int stepping,
                              int zStart, int zEnd, String name)
    {
        return 0;
    }
    
    /**
     * Projects a stack based on the maximum intensity at each XY coordinate.
     * @param stack The raw pixel data from the stack 
     * @param stepping Stepping value to use while calculating the projection.
     * For example, <code>stepping=1</code> will use every optical section from
     * <code>start</code> to <code>end</code> where <code>stepping=2</code> will
     * use every other section from <code>start</code> to <code>end</code> to
     * perform the projection.
     * @param start Optical section to start projecting from.
     * @param end Optical section to finish projecting.
     * @return Projected plane pixel data.
     */
    private byte[] projectStackMax(PixelData stack, int stepping,
                                      int start, int end)
    {
        PixelsType pixelsType = pixels.getPixelsType();
        int planeSizeInPixels = pixels.getSizeX() * pixels.getSizeY();
        int planeSize = 
            planeSizeInPixels * (iPixels.getBitDepth(pixelsType) / 8);
        
        byte[] buf = new byte[planeSize];
        PixelData plane = new PixelData(pixelsType, ByteBuffer.wrap(buf));
        
        int currentPlaneStart;
        double projectedValue, stackValue;
        for (int z = start; z < end; z+=stepping)
        {
            currentPlaneStart = planeSizeInPixels * z;
            for (int i = 0; i < planeSizeInPixels; i++)
            {
                projectedValue = plane.getPixelValue(i);
                stackValue = stack.getPixelValue(currentPlaneStart + i);
                if (stackValue > projectedValue)
                {
                    plane.setPixelValue(i, stackValue);
                }
            }
        }
        return buf;
    }
}
