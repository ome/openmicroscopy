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
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.api.IPixels;
import ome.api.ServiceInterface;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;
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
@RemoteBinding(jndiBinding = "omero/remote/ome.api.IPixels")
@Local(IPixels.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IPixels")
@SecurityDomain("OmeroSecurity")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
class PixelsImpl extends AbstractLevel2Service implements IPixels {

    protected transient PixelsService pixelsData;

    public final void setPixelsData(PixelsService pixelsData) {
        beanHelper.throwIfAlreadySet(this.pixelsData, pixelsData);
        this.pixelsData = pixelsData;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return IPixels.class;
    }

    // ~ Service methods
    // =========================================================================

    @RolesAllowed("user")
    public Pixels retrievePixDescription(long pixId) {
        Pixels p = iQuery.findByQuery("select p from Pixels as p "
                + "left outer join fetch p.pixelsType as pt "
                + "left outer join fetch p.channels as c "
                + "left outer join fetch p.pixelsDimensions "
                + "left outer join fetch c.colorComponent "
                + "left outer join fetch c.logicalChannel as lc "
                + "left outer join fetch c.statsInfo "
                + "left outer join fetch lc.photometricInterpretation "
                + "where p.id = :id",
                new Parameters().addId(pixId));
        return p;
    }

    // TODO we need to validate and make sure only one RndDef per user.
    @RolesAllowed("user")
    public RenderingDef retrieveRndSettings(final long pixId) {

        final Long userId = getSecuritySystem().getEventContext()
                .getCurrentUserId();

        return (RenderingDef) iQuery
                .findByQuery(
                        "select rdef from RenderingDef as rdef "
                                + "left outer join fetch rdef.quantization "
                                + "left outer join fetch rdef.model "
                                + "left outer join fetch rdef.waveRendering as cb "
                                + "left outer join fetch cb.color "
                                + "left outer join fetch cb.family "
                                + "left outer join fetch rdef.spatialDomainEnhancement where "
                                + "rdef.pixels.id = :pixid and rdef.details.owner.id = :ownerid",
                        new Parameters().addLong("pixid", pixId).addLong(
                                "ownerid", userId));
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void saveRndSettings(RenderingDef rndSettings) {
        iUpdate.saveObject(rndSettings);
    }

    @RolesAllowed("user")
    public int getBitDepth(PixelsType pixelsType) {
        return PixelBuffer.getBitDepth(pixelsType);
    }

    @RolesAllowed("user")
    public <T extends IObject> T getEnumeration(Class<T> klass, String value) {
        return iQuery.findByString(klass, "value", value);
    }

    @RolesAllowed("user")
    public <T extends IObject> List<T> getAllEnumerations(Class<T> klass) {
        return iQuery.findAll(klass, null);
    }
}
