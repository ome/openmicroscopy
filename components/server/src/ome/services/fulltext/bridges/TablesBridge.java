/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.fulltext.bridges;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ome.io.nio.OriginalFilesService;
import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.screen.Plate;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.services.fulltext.BridgeHelper;
import ome.system.OmeroContext;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.springframework.context.ApplicationEventPublisher;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayStructure;
import ucar.ma2.Index;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers.Member;
import ucar.nc2.NetcdfFile;

/**
 * Bridge for parsing OMERO.tables attached to container types. The column names
 * are taken as field names on each image (or similar) found within the table.
 * For example, if a table is attached to a plate and has an
 * omero.grid.ImageColumn "IMAGE" along with one omero.grid.DoubleColumn named
 * "SIZE", then a row with IMAGE == 1 and SIZE == 0.02 will add a field "SIZE"
 * to the {@link Image} with id 1 so that a Lucene search "SIZE:0.02" will
 * return that object.
 * 
 * This is accomplished by detecting such OMERO.tables on the container and
 * registering each row (above: IMAGE == 1, IMAGE == 2, etc) for later
 * processing. When the element objects are handled, the container is found and
 * the appropriate row processed. This two stage processingis necessary so that
 * later indexing does not overwrite the table values.
 * 
 * @since 4.3
 */
@Deprecated
public class TablesBridge extends BridgeHelper {

    /**
     * Mimetype set on OriginalFile.mimetype (or in previous version,
     * OriginalFile.format.value).
     */
    public final String OMERO_TABLE = "OMERO.tables";

    /* final */OriginalFilesService ofs;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        super.setApplicationEventPublisher(publisher);
        if (publisher instanceof OmeroContext) {
            OmeroContext ctx = (OmeroContext) publisher;
            ofs = ctx.getBean("/OMERO/Files", OriginalFilesService.class);
        } else {
            log.warn("Publisher is " + publisher.getClass().getName());
            log.warn("Cannot configure TablesBridge properly!");
        }
    }

    /**
     * Primary entry point for all bridges.
     */
    @Override
    public void set(String name, Object value, Document document,
            LuceneOptions opts) {

        if (value instanceof Image) {
            handleImage((Image) value, document, opts);
        } else if (value instanceof Plate) {
            handleAnnotated((Plate) value, document, opts);
        } else if (value instanceof Dataset) {
            handleAnnotated((Dataset) value, document, opts);
        }
    }

    /**
     * Processes any annotations attached to the following types which contain
     * this image: Plate, Dataset
     */
    protected void handleImage(Image image, Document document,
            LuceneOptions opts) {

        for (Iterator<WellSample> it = image.iterateWellSamples(); it.hasNext();) {
            WellSample ws = it.next();
            Well well = ws.getWell();
            Plate plate = well.getPlate();
            for (Annotation a : plate.linkedAnnotationList()) {
                // ///////////////////////////////////////////////////
                handleAnnotation(a, new AttachRow(image, document, opts));
                // ///////////////////////////////////////////////////
            }
        }

        for (Dataset ds : image.linkedDatasetList()) {
            for (Annotation a : ds.linkedAnnotationList()) {
                // ///////////////////////////////////////////////////
                handleAnnotation(a, new AttachRow(image, document, opts));
                // ///////////////////////////////////////////////////
            }
        }
    }

    /**
     * Responsible for iterating over any attached OMERO.tables and registering
     * all appropriate row objects for later processing. For example, if the
     * table has an omero.grid.ImageColumn with ids 1, 2, 3, and 4, then 4 image
     * objects will be registered for later processing by
     * {@link #handleImage(Image, Document, LuceneOptions)}.
     */
    protected void handleAnnotated(IAnnotated annotated, Document document,
            LuceneOptions opts) {
        for (Annotation a : annotated.linkedAnnotationList()) {
            // ///////////////////////////////////////////////////
            handleAnnotation(a, new RegisterRow());
            // ///////////////////////////////////////////////////
        }
    }

    /**
     * Detects if the given annotation contains an OMERO.table and if so, passes
     * it off for further processing.
     */
    protected void handleAnnotation(Annotation annotation, RowProcessor proc) {
        annotation = getProxiedObject(annotation);
        if (annotation instanceof FileAnnotation) {
            final OriginalFile file = ((FileAnnotation) annotation).getFile();
            final String mimetype = file.getMimetype();
            final String path = ofs.getFilesPath(file.getId());
            // /////////////////////////////////////////////////
            if (OMERO_TABLE.equals(mimetype)) {
                debug("Handling annotation %s", annotation);
                handleHdf5(path, proc);
            }
            // //////////////////////////////////////////////////
        }
    }

    /**
     * Process a single OMERO.tables file. This method is primarily responsible
     * for iteration and the try/finally logic to guarantee cleanup, etc.
     */
    protected void handleHdf5(String path, RowProcessor proc) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(path);
            Table table = new Table(ncfile);
            if (!proc.initialize(table)) {
                debug("Skipping %s", path);
                return;
            }

            debug("Handling %s with %s rows", path, table.rows);
            StructureData sData = null;
            for (int x = 0; x < table.rows; x++) {
                // ////////////a/////////////////////////////
                sData = (StructureData) table.structure.getObject(x);
                if (!proc.processRow(x, sData)) {
                    break; // Permit break out.
                }
                // //////////////////////////////////////////
            }

        } catch (IOException ioe) {
            log.error("trying to open " + path, ioe);
        } finally {
            if (null != ncfile) {
                try {
                    ncfile.close();
                } catch (IOException ioe) {
                    log.error("trying to close " + path, ioe);
                }
            }
        }
    }

    private void debug(String format, Object... vals) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, vals));
        }
    }

    private void trace(String format, Object... vals) {
        if (log.isTraceEnabled()) {
            log.trace(String.format(format, vals));
        }
    }
    
    // //////////////////////////////////////////////////////////////////////////

    abstract class RowProcessor {
        int targetCol;
        IObject targetType;

        public boolean initialize(Table table) {
            targetCol = table.getFinestColumn();
            if (targetCol < 0) {
                log.info("No column found.");
                return false;
            }
            targetType = table.getObjectForColumn(targetCol);
            return true;
        }

        public abstract boolean processRow(int row, StructureData sData);
        
        protected long getLong(Array array) {
            Index index = array.getIndex();
            index.set(0);
            long targetId = array.getLong(index);
            return targetId;
        }

        protected Object getObject(Array array) {
            Index index = array.getIndex();
            index.set(0);
            return array.getObject(index);
        }

    }

    /**
     * Attaches all rows matching the IObject instance to the given Document
     * argument.
     */
    class AttachRow extends RowProcessor {
        final IObject object;
        final Document document;
        final LuceneOptions opts;

        AttachRow(IObject object, Document document, LuceneOptions opts) {
            this.object = object;
            this.document = document;
            this.opts = opts;
        }

        /**
         * Primary processing method responsible for adding the value of each
         * column in "members" to the Document.
         */
        public boolean processRow(int row, StructureData sData) {
            List<Member> members = sData.getMembers();
            Array targetArray = sData.getArray(members.get(targetCol));
            long targetId = getLong(targetArray);
            if (targetId != object.getId().longValue()) {
                return true; // Keep going.
            }
            for (int i = 0; i < members.size(); i++) {
                if (i == targetCol) {
                    continue;
                }
                final Member member = members.get(i);
                final String name = member.getName();
                final Array array = sData.getArray(member);
                final String str = getObject(array).toString();
                trace("Add %s:%s to %s", name, str, object);
                add(document, name, str, opts);
            }
            return true;
        }

    }

    class RegisterRow extends RowProcessor {
        public boolean processRow(int row, StructureData sData) {
            List<Member> members = sData.getMembers();
            Array targetArray = sData.getArray(members.get(targetCol));
            long targetId = getLong(targetArray);
            // Object reused since the id is copied in EventLogLoader
            targetType.setId(targetId);
            reindex(targetType);
            return true;
        }
    }

    // //////////////////////////////////////////////////////////////////////////

    /**
     * Wraps a NetCDF/HDF5 file conforming to the first version of OMERO.tables.
     */
    private class Table {

        public final static String COLUMN_BASE = "::omero::grid::";
        public final static String IMAGE_COL = COLUMN_BASE + "ImageColumn";
        public final static String WELL_COL = COLUMN_BASE + "WellColumn";
        public final static String PLATE_COL = COLUMN_BASE + "PlateColumn";

        final private NetcdfFile f;

        final ArrayStructure structure;

        final long rows;

        final List<String> types;

        Table(NetcdfFile f) throws IOException {
            this.f = f;
            this.structure = structure();
            this.rows = structure.getSize();
            this.types = getColTypes();
            trace("Column types: %s", this.types);
        }

        public IObject getObjectForColumn(int targetCol) {
            String type = types.get(targetCol);
            if (type.startsWith(IMAGE_COL)) {
                return new Image();
            } else if (type.startsWith(WELL_COL)) {
                return new Well();
            } else if (type.startsWith(PLATE_COL)) {
                return new Plate();
            } else {
                throw new RuntimeException("Unsupported type:" + type);
            }
        }

        /**
         * Returns
         * 
         * @return
         */
        public int getFinestColumn() {

            final List<Integer> plates = new ArrayList<Integer>();
            final List<Integer> wells = new ArrayList<Integer>();
            final List<Integer> images = new ArrayList<Integer>();

            for (int i = 0; i < types.size(); i++) {
                final String type = types.get(i);
                if (type.startsWith(IMAGE_COL)) {
                    images.add(i);
                } else if (type.startsWith(WELL_COL)) {
                    wells.add(i);
                } else if (type.startsWith(PLATE_COL)) {
                    plates.add(i);
                } else {
                }
            }

            if (images.size() == 1) {
                return images.get(0);
            } else if (images.size() > 1) {
                log.warn("Multiple image columns found.");
                return -1;
            }

            if (wells.size() == 1) {
                return wells.get(0);
            } else if (wells.size() > 1) {
                log.warn("Multiple well columns found.");
                return -2;
            }

            if (plates.size() == 1) {
                return plates.get(0);
            } else if (plates.size() > 1) {
                log.warn("Multiple plate columns found.");
                return -3;
            }

            return -4;
        }

        /**
         * For the current version of OMERO.tables lookup the primary data
         * structure ("/OME/Measurements").
         */
        private ArrayStructure structure() throws IOException {
            return (ArrayStructure) f.findVariable("/OME/Measurements").read();
        }

        /**
         * For the current version of OMERO.tables lookup the stored Ice class
         * names of each column, e.g. "::omero::grid::LongColumn"
         */
        private List<String> getColTypes() throws IOException {
            ArrayChar typeArray = (ArrayChar) f
                    .findVariable("/OME/ColumnTypes").read();
            char[][] obj = (char[][]) typeArray.copyToNDJavaArray();
            List<String> types = new ArrayList<String>();
            for (int i = 0; i < obj.length; i++) {
                types.add(new String(obj[i]));
            }
            return types;
        }

    }
}
