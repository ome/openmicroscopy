/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

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
import ome.model.display.RenderingDef;
import ome.model.internal.Details;
import ome.model.screen.Plate;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.system.EventContext;
import ome.tools.hibernate.SessionFactory;
import ome.util.CBlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
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

    public final static Logger log = LoggerFactory.getLogger(DeleteBean.class);

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

    public final static String SETTINGSID_QUERY = "select r.id, q.id from RenderingDef r "
        + "join r.quantization q "
        + "join r.pixels pix "
        + "join pix.image img where img.id = :id";

    public final static String CHANNELID_QUERY = "select ch.id, si.id, lc.id "
        + "from Channel ch "
        + "join ch.statsInfo si "
        + "join ch.logicalChannel lc "
        + "join ch.pixels.image img where img.id = :id";

    public final static String PLATEIMAGES_QUERY = "select i.id from Image i "
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
     * This uses {@link #IMAGE_QUERY} to load all the subordinate metadata of the
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

        final Image i = iQuery.get(Image.class, id);

        throwSecurityViolationIfNotAllowed(i);
        final Session session = sf.getSession();
        session.clear();

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                clearRois(session, i);
            }
        });

        /*
        Previously, the IMAGE_QUERY query was used to load all the objects
        attached to an Image for deletion. This, unfortunately, led to memory
        issues (ticket:1708). Now, instead, we are deleting the objects in
        the same order, but without loading them.
         */

        execute(session, id, "update Pixels set relatedTo = null where id in" +
			"(select p.id from Pixels p where p.relatedTo.image.id = :id)");

        execute(session, id, "delete PixelsOriginalFileMap where id in" +
			"(select m.id from PixelsOriginalFileMap m where m.child.image.id = :id)");

        execute(session, id, "delete PlaneInfo where id in " +
			"(select pi.id from PlaneInfo pi where pi.pixels.image.id = :id)");

        deleteSettings(id);
        deleteChannels(id);

        execute(session, id, "delete Thumbnail where id in " +
			"(select tb.id from Thumbnail tb where tb.pixels.image.id = :id)");

        execute(session, id, "delete Pixels where id in " +
			"(select pix.id from Pixels pix where pix.image.id = :id)");

        execute(session, id, "delete ImageAnnotationLink where id in " +
			"(select link.id from ImageAnnotationLink link where link.parent.id = :id)");

        execute(session, id, "delete DatasetImageLink where id in " +
			"(select link.id from DatasetImageLink link where link.child.id = :id)");

        execute(session, id,
                "delete Image img where img.id = :id");

        session.clear(); // ticket:1708

    }

    private int execute(final Session session, final long id, String str) {
        Query q;
        q = session.createQuery(str);
        q.setParameter("id", id);
        return q.executeUpdate();
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

        List<Object[]> links = iQuery.projection(
                "select link.id, c.id from DatasetImageLink link "
                        + "join link.parent p "
                        + "join link.child c "
                        + "where p.id = :id", new Parameters()
                        .addId(datasetId));
        Set<Long> ids = new HashSet<Long>();
        for (Object[] link_child : links) {
            ids.add((Long)link_child[1]);
            iUpdate.deleteObject(new DatasetImageLink((Long)link_child[0], false));
        }
        deleteImages(ids, force);
    };

    @RolesAllowed("user")
    public void deleteSettings(final long imageId) {

        Image i = iQuery.get(Image.class, imageId);
        throwSecurityViolationIfNotAllowed(i);
        final Session session = sf.getSession();

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                List<Object[]> rdefs = iQuery.projection(
                        SETTINGSID_QUERY, new Parameters().addId(imageId));
                for (Object[] rv: rdefs) {
                    Long rid = (Long) rv[0];
                    Long qid = (Long) rv[1];

                    Query q = session.createQuery("delete ChannelBinding cb where cb.renderingDef.id = :rid");
                    q.setParameter("rid", rid);
                    q.executeUpdate();

                    q = session.createQuery("delete RenderingDef r where r.id = :rid");
                    q.setParameter("rid", rid);
                    q.executeUpdate();

                    q = session.createQuery("delete QuantumDef q where q.id = :qid");
                    q.setParameter("qid", qid);
                    q.executeUpdate();
                }
            }
        });
    }

    @RolesAllowed("user")
    public void deleteChannels(final long imageId) {

        Image i = iQuery.get(Image.class, imageId);
        throwSecurityViolationIfNotAllowed(i);
        final Session session = sf.getSession();

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                List<Object[]> channels = iQuery.projection(
                        CHANNELID_QUERY, new Parameters().addId(imageId));
                for (Object[] rv: channels) {
                    Long chid = (Long) rv[0];
                    Long siid = (Long) rv[1];
                    Long lcid = (Long) rv[2];

                    execute(session, chid, "delete Channel ch where ch.id = :id");
                    execute(session, siid, "delete StatsInfo si where si.id = :id");

                    List<Object[]> remainingChannels = iQuery.projection(
                            "select ch.id from LogicalChannel lc join lc.channels ch " +
                            "where lc.id = :id",  new Parameters().addId(lcid));

                    if (remainingChannels.size() == 0) {
                        execute(session, lcid, "delete LogicalChannel lc where lc.id = :id");
                    }

                }
            }
        });
    }

    @RolesAllowed("user")
    public void deletePlate(final long plateId) {

        Plate p = iQuery.get(Plate.class, plateId);
        throwSecurityViolationIfNotAllowed(p);

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {

                final List<Object[]> imagesOnPlate = iQuery.projection(
                        PLATEIMAGES_QUERY, new Parameters().addId(plateId));

                final Session session = sf.getSession();
                final StringBuilder sb = new StringBuilder();
                sb.append("Delete for plate ");
                sb.append(plateId);
                sb.append(" : ");

                Query q; // reused.
                int count; // reused

                if (imagesOnPlate.size() > 0) {
                    Set<Long> imageIdsForPlate = new HashSet<Long>();
                    for (Object[] objs: imagesOnPlate) {
                        imageIdsForPlate.add((Long)objs[0]);
                    }
                    sb.append(imageIdsForPlate.size());
                    sb.append(" Image(s); ");

                    // Samples
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

                // Plate annotations
                q = session
                .createQuery("delete PlateAnnotationLink where parent.id = :id");
                q.setParameter("id", plateId);
                count = q.executeUpdate();
                sb.append(count);
                sb.append(" PlateAnnotationLink(s);");

                // Screen links
                q = session
                .createQuery("delete ScreenPlateLink where child.id = :id");
                q.setParameter("id", plateId);
                count = q.executeUpdate();
                sb.append(count);
                sb.append(" ScreenPlateLink(s);");

                // Finally, the plate.
                q = session.createQuery("delete Plate where id = :id");
                q.setParameter("id", plateId);
                q.executeUpdate();

                iUpdate.flush();

                log.info(sb.toString());
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

        final EventContext ec = getSecuritySystem().getEventContext();
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
     * Uses bulk update
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/1654">ticket:1654</a>
     */
    private void clearRois(Session session, Image i) {

        int shapeCount = execute(session, i.getId(),
                "delete from Shape where roi.id in " +
                "(select id from Roi roi where roi.image.id = :id)");
        int roiAnnCount = execute(session, i.getId(),
                "delete from RoiAnnotationLink where parent.id in " +
                "(select id from Roi roi where roi.image.id = :id)");
        int roiCount = execute(session, i.getId(),
                "delete from Roi where image.id = :id");

        if (shapeCount > 0 || roiAnnCount > 0 || roiCount > 0) {
            log.info(String.format("Roi delete for image %s :" +
                    " %s rois, %s shapes, %s annotations",
                    i.getId(), roiCount, shapeCount, roiAnnCount));
        }
    }
}
