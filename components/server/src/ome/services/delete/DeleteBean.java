/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.annotations.RolesAllowed;
import ome.api.IDelete;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.logic.AbstractLevel2Service;
import ome.model.IObject;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.containers.DatasetImageLink;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import ome.model.display.ChannelBinding;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.internal.Details;
import ome.model.screen.Plate;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.system.EventContext;
import ome.tools.hibernate.SessionFactory;
import ome.util.CBlock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

/**
 * Strict implementation of the {@link IDelete} service interface which will use
 * the {@link SecuritySystem} via
 * {@link ome.security.SecuritySystem#runAsAdmin(AdminAction)} to forcibly
 * delete instances.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see IDelete
 */
@Transactional
public class DeleteBean extends AbstractLevel2Service implements IDelete {

    public final static Log log = LogFactory.getLog(DeleteBean.class);

    /**
     * Loads an {@link Image} graph including: Pixels, Channel, LogicalChannel,
     * StatsInfo, PlaneInfo, Thumbnails, file maps, OriginalFiles, and Settings
     */
    public final static String IMAGE_QUERY = "select i from Image as i "
            + "left outer join fetch i.pixels as p "
            + "left outer join fetch p.channels as c "
            + "left outer join fetch c.logicalChannel as lc "
            + "left outer join fetch lc.channels as c2 "
            + "left outer join fetch c.statsInfo as sinfo "
            + "left outer join fetch p.planeInfo as pinfo "
            + "left outer join fetch p.thumbnails as thumb "
            + "left outer join fetch p.pixelsFileMaps as map "
            + "left outer join fetch map.parent as ofile "
            + "left outer join fetch p.settings as setting "
            + "where i.id = :id";

    public final static String SETTINGS_QUERY = "select r from RenderingDef r "
            + "left outer join fetch r.waveRendering "
            + "left outer join fetch r.quantization "
            + "join r.pixels pix join pix.image img " + "where img.id = :id";

    public final static String PLATEIMAGES_QUERY = "select i from Image i "
            + "join i.wellSamples ws join ws.well w "
            + "join w.plate p where p.id = :id";

    protected final LocalAdmin admin;

    protected final SessionFactory sf;

    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IDelete.class;
    }

    public DeleteBean(LocalAdmin admin, SessionFactory sf) {
        this.admin = admin;
        this.sf = sf;
    }

    // ~ Service Methods
    // =========================================================================

    @RolesAllowed("user")
    public List<IObject> checkImageDelete(final long id, final boolean force) {

        final QueryConstraints constraints = new QueryConstraints(admin,
                iQuery, id, force);
        sec.runAsAdmin(constraints);
        return constraints.getResults();
    }

    /**
     * This uses {@link #IMAGE_QUERY} to load all the subordinate metdata of the
     * {@link Image} which will be deleted.
     */
    @RolesAllowed("user")
    public List<IObject> previewImageDelete(long id, boolean force) {
        final UnloadedCollector delete = new UnloadedCollector(iQuery, admin,
                false);
        Image[] holder = new Image[1];
        getImageAndCount(holder, id, delete);
        return delete.list;
    }

    @RolesAllowed("user")
    public void deleteImage(final long id, final boolean force)
            throws SecurityViolation, ValidationException {

        final List<IObject> constraints = checkImageDelete(id, force);
        if (constraints.size() > 0) {
            throw new ApiUsageException(
                    "Image has following constraints and cannot be deleted:"
                            + constraints
                            + "\nIt is possible to check for a "
                            + "non-empty constraints list via checkImageDelete.");
        }

        final UnloadedCollector delete = new UnloadedCollector(iQuery, admin,
                false);
        final Image[] holder = new Image[1];
        getImageAndCount(holder, id, delete);
        final Image i = holder[0];

        throwSecurityViolationIfNotAllowed(i);

        iQuery.execute(new HibernateCallback() {

            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                session.clear();
                return null;
            }

        });

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                clearPixelsRelatedTo(i);
            }
        });

        for (final IObject object : delete.list) {
            try {
                sec.runAsAdmin(new AdminAction() {
                    public void runAsAdmin() {
                        iUpdate.deleteObject(object);
                    }

                });
            } catch (ValidationException ve) {
                // TODO could collect these and throw at once; on the other
                // hand once one fails, there's probably going to be
                // interrelated
                // issues
                // TODO Could use another exception here
                ValidationException div = new ValidationException(
                        "Delete failed since related object could not be deleted: "
                                + object);
                throw div;
            }
        }

    }

    @RolesAllowed("user")
    public void deleteImages(java.util.Set<Long> ids, boolean force)
            throws SecurityViolation, ValidationException, ApiUsageException {

        if (ids == null || ids.size() == 0) {
            return; // EARLY EXIT!
        }

        for (Long id : ids) {
            try {
                deleteImage(id, force);
            } catch (SecurityViolation sv) {
                throw new SecurityViolation("Error while deleting image " + id
                        + "\n" + sv.getMessage());
            } catch (ValidationException ve) {
                throw new ValidationException("Error while deleting image "
                        + id + "\n" + ve.getMessage());
            } catch (ApiUsageException aue) {
                throw new ApiUsageException("Error while deleting image " + id
                        + "\n" + aue.getMessage());
            }
        }

    };

    @RolesAllowed("user")
    public void deleteImagesByDataset(long datasetId, boolean force)
            throws SecurityViolation, ValidationException, ApiUsageException {

        List<DatasetImageLink> links = iQuery.findAllByQuery(
                "select link from DatasetImageLink link "
                        + "left outer join fetch link.parent "
                        + "left outer join fetch link.child "
                        + "where link.parent.id = :id", new Parameters()
                        .addId(datasetId));
        Set<Long> ids = new HashSet<Long>();
        for (DatasetImageLink link : links) {
            ids.add(link.child().getId());
            link.child().unlinkDataset(link.parent());
            iUpdate.deleteObject(link);
        }
        deleteImages(ids, force);
    };

    @RolesAllowed("user")
    public void deleteSettings(final long imageId) {

        Image i = iQuery.get(Image.class, imageId);
        throwSecurityViolationIfNotAllowed(i);

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                List<RenderingDef> rdefs = iQuery.findAllByQuery(
                        SETTINGS_QUERY, new Parameters().addId(imageId));
                for (RenderingDef renderingDef : rdefs) {
                    QuantumDef quantumDef = renderingDef.getQuantization();
                    iQuery.evict(renderingDef);
                    iQuery.evict(quantumDef);
                    for (ChannelBinding cb : new ArrayList<ChannelBinding>(
                            renderingDef.unmodifiableWaveRendering())) {
                        iQuery.evict(cb);
                        renderingDef.removeChannelBinding(cb);
                        iUpdate.deleteObject(cb);
                    }
                    iUpdate.deleteObject(renderingDef);
                    iUpdate.deleteObject(renderingDef.getQuantization());
                }
                iUpdate.flush();
            }
        });

    }

    @RolesAllowed("user")
    public void deletePlate(final long plateId) {

        Plate p = iQuery.get(Plate.class, plateId);
        throwSecurityViolationIfNotAllowed(p);
        ;

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {

                final List<Image> imagesOnPlate = iQuery.findAllByQuery(
                        PLATEIMAGES_QUERY, new Parameters().addId(plateId));

                final Session session = sf.getSession();
                final StringBuilder sb = new StringBuilder();
                sb.append("Deleting for plate ");
                sb.append(plateId);
                sb.append(" : ");

                Query q; // reused.
                int count; // reused

                if (imagesOnPlate.size() > 0) {
                    Set<Long> imageIdsForPlate = new HashSet<Long>();
                    for (Image img : imagesOnPlate) {
                        imageIdsForPlate.add(img.getId());
                    }
                    sb.append(imageIdsForPlate.size());
                    sb.append(" Image(s); ");

                    // Samples
                    q = session.createQuery("delete WellSampleAnnotationLink "
                            + "where parent.id in (select id from WellSample "
                            + "where image.id in (:ids) )");
                    q.setParameterList("ids", imageIdsForPlate);
                    count = q.executeUpdate();
                    sb.append(count);
                    sb.append(" WellSampleAnnotationLink(s); ");

                    q = session.createQuery("delete WellSample "
                            + "where image.id in (:ids)");
                    q.setParameterList("ids", imageIdsForPlate);
                    count = q.executeUpdate();
                    sb.append(count);
                    sb.append(" WellSample(s); ");

                    // Images
                    deleteImages(imageIdsForPlate, true);
                }

                // Well
                q = session
                        .createQuery("delete WellAnnotationLink where parent.id in "
                                + "(select id from Well where plate.id = :id)");
                q.setParameter("id", plateId);
                count = q.executeUpdate();
                sb.append(count);
                sb.append(" WellAnnotationLink(s);");

                q = session.createQuery("delete Well where plate.id = :id");
                q.setParameter("id", plateId);
                count = q.executeUpdate();
                sb.append(count);
                sb.append(" Well(s);");

                // Plate
                q = session
                        .createQuery("delete PlateAnnotationLink where parent.id = :id");
                q.setParameter("id", plateId);
                count = q.executeUpdate();
                sb.append(count);
                sb.append(" PlateAnnotationLink(s);");

                q = session.createQuery("delete Plate where id = :id");
                q.setParameter("id", plateId);
                q.executeUpdate();

                iUpdate.flush();
            }
        });

    }

    // Implementation
    // =========================================================================

    /**
     * Uses the locally defined query to load an {@link Image} and calls
     * {@link #collect(UnloadedCollector, Image)} in order to define a list of
     * what will be deleted.
     * 
     * This method fulfills the {@link #previewImageDelete(long, boolean)}
     * contract and as such is used by {@link #deleteImage(long, boolean)} in
     * order to fulfill its contract.
     */
    protected void getImageAndCount(final Image[] images, final long id,
            final UnloadedCollector delete) {
        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                images[0] = iQuery.findByQuery(IMAGE_QUERY, new Parameters()
                        .addId(id));
                if (images[0] == null) {
                    throw new ApiUsageException("Cannot find image: " + id);
                }
                collect(delete, images[0]);
            }
        });
    }

    /**
     * Walks the {@link Image} graph collecting unloaded instances of all
     * entities for later delete.
     */
    protected void collect(final UnloadedCollector delete, final Image i) {

        i.collectPixels(new CBlock<Pixels>() {

            public Pixels call(IObject object) {

                if (object == null) {
                    return null; // EARLY EXIT. Happening due to image_index=1
                }

                Pixels p = (Pixels) object;

                p.eachLinkedOriginalFile(delete);
                p.collectPlaneInfo(delete);

                for (RenderingDef rdef : p
                        .collectSettings((CBlock<RenderingDef>) null)) {

                    for (ChannelBinding binding : rdef
                            .unmodifiableWaveRendering()) {
                        delete.call(binding);
                    }
                    delete.call(rdef);
                    delete.call(rdef.getQuantization());
                }

                p.collectThumbnails(delete);

                // Why do we set channel to null here and not waveRendering
                // above?
                List<Channel> channels = p
                        .collectChannels((CBlock<Channel>) null);
                for (int i = 0; i < channels.size(); i++) {
                    Channel channel = channels.set(i, null);
                    delete.call(channel);
                    delete.call(channel.getStatsInfo());

                    LogicalChannel lc = channel.getLogicalChannel();
                    if (lc.sizeOfChannels() < 2) {
                        delete.call(lc);
                    }
                    // delete.call(lc.getLightSource());
                    // // TODO lightsource
                    // delete.call(lc.getAuxLightSource());
                    // // TODO lightsource
                    // delete.call(lc.getOtf());
                    // delete.call(lc.getDetectorSettings());
                    // DetectorSettings ds = lc.getDetectorSettings();
                    // delete.call(ds.getDetector());
                }

                delete.call(p);

                return null;
            }

        });

        for (DatasetImageLink link : i
                .collectDatasetLinks((CBlock<DatasetImageLink>) null)) {
            i.removeDatasetImageLink(link, true);
            delete.call(link);
        }

        for (ImageAnnotationLink link : i
                .collectAnnotationLinks((CBlock<ImageAnnotationLink>) null)) {
            i.removeImageAnnotationLink(link, true);
            delete.call(link);
        }

        delete.call(i);

    }

    private void throwSecurityViolationIfNotAllowed(final IObject i) {

        final String type = i.getClass().getName();
        final Details d = i.getDetails();
        final long user = d.getOwner().getId();
        final long group = d.getGroup().getId();

        final EventContext ec = admin.getEventContext();
        final boolean root = ec.isCurrentUserAdmin();
        final List<Long> leaderof = ec.getLeaderOfGroupsList();
        final boolean pi = leaderof.contains(group);
        final boolean own = ec.getCurrentUserId().equals(user);

        if (!own && !root && !pi) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("User %d attempted to delete " + type
                        + " %d belonging to User %d", ec.getCurrentUserId(), i
                        .getId(), user));
            }
            throw new SecurityViolation(String.format(
                    "User %s cannot delete %s %d ", ec.getCurrentUserName(),
                    type, i.getId()));
        }
    }

    /**
     * Finds all Pixels whose {@link Pixels#getRelatedTo()} field points to a
     * {@link Pixels} which is contained in the given {@link Image} and nulls
     * the relatedTo field.
     * 
     * @param i
     */
    private void clearPixelsRelatedTo(Image i) {
        List<Long> ids = new ArrayList<Long>();
        for (Pixels pixels : i.unmodifiablePixels()) {
            ids.add(pixels.getId());
        }
        if (ids != null && ids.size() > 0) {
            List<Pixels> relatedTo = iQuery.findAllByQuery(
                    "select p from Pixels p "
                            + "where p.relatedTo.id in (:ids)",
                    new Parameters().addIds(ids));
            for (Pixels pixels : relatedTo) {
                pixels.setRelatedTo(null);
                iUpdate.flush();
            }
        }
    }
}
