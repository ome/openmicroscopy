/*
 *   $Id$
 *
 *   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import omero.model.Fileset;
import omero.model.IObject;
import omero.model.Pixels;

/**
 * Simple event base-class used by {@link IObservable} and {@link IObserver}
 * implementations.
 *
 * @since Beta4.1
 */
public class ImportEvent {

    // enums for GROUP_SET types
    public static final int GROUP_PUBLIC = 0;
    public static final int GROUP_COLLAB_READ = 1;
    public static final int GROUP_COLLAB_READ_LINK = 2;
    public static final int GROUP_PRIVATE = 3;
    public static final int GROUP_SYSTEM = 4;

    public String toLog() {
        return getClass().getSimpleName();
    }

    // Base classes

    public static class COUNT_EVENT extends ImportEvent {
        public final String shortName;
        public final Integer index;
        public final Integer numDone;
        public final Integer total;

        COUNT_EVENT(String shortName, Integer index, Integer numDone,
                Integer total) {
            this.shortName = shortName;
            this.index = index;
            this.numDone = numDone;
            this.total = total;
        }

    }

    public static class PROGRESS_EVENT extends ImportEvent {
        public final int index;
        public final String filename;
        public final IObject target;
        public final Long pixId;
        public final int series;
        public final ImportSize size;
        public final Integer numDone;
        public final Integer total;

        public PROGRESS_EVENT(int index, String filename, IObject target, Long pixId,
                int series, ImportSize size, Integer numDone, Integer total) {
            this.index = index;
            this.filename = filename;
            this.target = target;
            this.pixId = pixId;
            this.series = series;
            this.size = size;
            this.numDone = numDone;
            this.total= total;
        }

        @Override
        public String toLog() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toLog());
            sb.append(" ");
            return sb.toString();
        }
    }

    public static class POST_UPLOAD_EVENT extends PROGRESS_EVENT {
        public final Long logFileId;

        public POST_UPLOAD_EVENT(int index, String filename, IObject target, Long pixId,
                int series, ImportSize size, Integer numDone, Integer total, Long logFileId) {
            super(index, filename, target, pixId, series, size, numDone, total);
            this.logFileId = logFileId;
        }

        @Override
        public String toLog() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toLog());
            sb.append(String.format("Step: %d of %d  Logfile: %d",
                    numDone, total, logFileId));
            return sb.toString();
        }
    }

    public static class FILE_UPLOAD_EVENT extends ImportEvent {
        public final String filename;
        public final int fileIndex;
        public final int fileTotal;
        public final Long uploadedBytes;
        public final Long contentLength;
        public final Exception exception;

        FILE_UPLOAD_EVENT(String filename, int fileIndex, int fileTotal,
                Long uploadedBytes, Long contentLength, Exception exception) {
            this.filename = filename;
            this.fileIndex = fileIndex;
            this.fileTotal = fileTotal;
            this.uploadedBytes = uploadedBytes;
            this.contentLength = contentLength;
            this.exception = exception;
        }
    }

    // Data-less events

    public static class ADD extends ImportEvent {
    }

    public static class ERRORS_PENDING extends ImportEvent {
    }

    public static class ERRORS_COMPLETE extends ImportEvent {

    }

    public static class ERRORS_FAILED extends ImportEvent {

    }

    public static class REIMPORT extends ImportEvent {

    }

    public static class LOGGED_IN extends ImportEvent {

    }

    public static class LOGGED_OUT extends ImportEvent {

    }

    // file-upload events

    public static class FILE_UPLOAD_STARTED extends FILE_UPLOAD_EVENT {
        public FILE_UPLOAD_STARTED(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILE_UPLOAD_BYTES extends FILE_UPLOAD_EVENT {
        public Long timeLeft;
        public FILE_UPLOAD_BYTES(String filename, int fileIndex, int fileTotal,
                Long uploadedBytes, Long contentLength, Long timeLeft,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
            this.timeLeft = timeLeft;
        }

        @Override
        public String toLog() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toLog());
            sb.append(String.format(" uploaded: %d of: %d bytes (ETA: %s)",
                    uploadedBytes, contentLength, msToString(timeLeft)));
            return sb.toString();
        }

        private String msToString(long millis) {
            return String.format("%d' %d\"",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                );
        }

    }

    public static class FILE_UPLOAD_COMPLETE extends FILE_UPLOAD_EVENT {
        public FILE_UPLOAD_COMPLETE(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILE_UPLOAD_FAILED extends FILE_UPLOAD_EVENT {
        public FILE_UPLOAD_FAILED(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILE_UPLOAD_ERROR extends FILE_UPLOAD_EVENT {
        public FILE_UPLOAD_ERROR(String filename, int fileIndex, int fileTotal,
                Long uploadedBytes, Long contentLength, Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILESET_UPLOAD_PREPARATION extends FILE_UPLOAD_EVENT {
        public FILESET_UPLOAD_PREPARATION(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILESET_UPLOAD_START extends FILE_UPLOAD_EVENT {
        public FILESET_UPLOAD_START(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILESET_UPLOAD_END extends FILE_UPLOAD_EVENT {
        public final String[] srcFiles;
        public final List<String> checksums;
        public final Map<Integer, String> failingChecksums;
        public FILESET_UPLOAD_END(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                String[] srcFiles, List<String> checksums,
                Map<Integer, String> failingChecksums, Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
            this.srcFiles = srcFiles;
            this.checksums = checksums;
            this.failingChecksums = failingChecksums;
        }
    }

    public static class FILE_UPLOAD_FINISHED extends FILE_UPLOAD_EVENT {
        public FILE_UPLOAD_FINISHED(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    public static class FILE_UPLOAD_CANCELLED extends FILE_UPLOAD_EVENT {
        public FILE_UPLOAD_CANCELLED(String filename, int fileIndex,
                int fileTotal, Long uploadedBytes, Long contentLength,
                Exception exception) {
            super(filename, fileIndex, fileTotal, uploadedBytes, contentLength,
                    exception);
        }
    }

    // misc-events

    public static class DEBUG_SEND extends ImportEvent {
        public final boolean sendFiles;
        public final boolean sendLogs;

        public DEBUG_SEND(boolean sendFiles, boolean sendLogs) {
            this.sendFiles = sendFiles;
            this.sendLogs = sendLogs;
        }
    }

    public static class IMPORT_STEP extends ImportEvent {
        public final int step;
        public final int series;
        public final int seriesCount;

        public IMPORT_STEP(int step, int series, int seriesCount) {
            this.step = step;
            this.series = series;
            this.seriesCount = seriesCount;
        }

        @Override
        public String toLog() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toLog());
            sb.append(" ");
            sb.append(String.format("Image: %d Series: %d Total Series: %d",
                    step, series, seriesCount));
            return sb.toString();
        }
    }

    public static class IMPORT_SUMMARY extends ImportEvent {
        public final long importTime;
        public final int errorCount;

        public IMPORT_SUMMARY(long importTime, int errorCount) {
            this.importTime = importTime;
            this.errorCount = errorCount;
        }
    }

    // count-events

    public static class LOADING_IMAGE extends COUNT_EVENT {
        public LOADING_IMAGE(String shortName, Integer index, Integer numDone,
                Integer total) {
            super(shortName, index, numDone, total);
        }

        @Override
        public String toLog() {
            return String.format("%s: %s", super.toLog(), shortName);
        }
    }

    public static class LOADED_IMAGE extends COUNT_EVENT {
        public LOADED_IMAGE(String shortName, Integer index, Integer numDone,
                Integer total) {
            super(shortName, index, numDone, total);
        }

        @Override
        public String toLog() {
            return String.format("%s: %s", super.toLog(), shortName);
        }
    }

    //
    // Progress-based events: these are used by the FileQueueTable (and others)
    // to know which file index is currently in which state. They should
    // possibly
    // be moved closer to the classes using them.
    //

    public static class BEGIN_POST_PROCESS extends PROGRESS_EVENT {
        public BEGIN_POST_PROCESS(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class END_POST_PROCESS extends PROGRESS_EVENT {
        public END_POST_PROCESS(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class BEGIN_SAVE_TO_DB extends PROGRESS_EVENT {
        public BEGIN_SAVE_TO_DB(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class END_SAVE_TO_DB extends PROGRESS_EVENT {
        public END_SAVE_TO_DB(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class DATASET_STORED extends PROGRESS_EVENT {
        public DATASET_STORED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size, Integer numDone, Integer total) {
            super(index, filename, target, pixId, series, size, numDone, total);
        }
    }

    public static class DATA_STORED extends PROGRESS_EVENT {
        public DATA_STORED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class IMPORT_ARCHIVING extends PROGRESS_EVENT {
        public IMPORT_ARCHIVING(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class IMPORT_OVERLAYS extends PROGRESS_EVENT {
        public IMPORT_OVERLAYS(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    public static class IMPORT_PROCESSING extends PROGRESS_EVENT {
        public IMPORT_PROCESSING(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size) {
            super(index, filename, target, pixId, series, size, null, null);
        }
    }

    // These extra PROGRESS_EVENT classes are added to allow some meaningful
    // event reporting under FS rather than abusing the ones above

    public static class METADATA_IMPORTED extends POST_UPLOAD_EVENT {
        public METADATA_IMPORTED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size,
                Integer numDone, Integer total, Long fsId) {
            super(index, filename, target, pixId, series, size, numDone, total, fsId);
        }
    }

    public static class THUMBNAILS_GENERATED extends POST_UPLOAD_EVENT {
        public THUMBNAILS_GENERATED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size,
                Integer numDone, Integer total, Long fsId) {
            super(index, filename, target, pixId, series, size, numDone, total, fsId);
        }
    }

    public static class PIXELDATA_PROCESSED extends POST_UPLOAD_EVENT {
        public PIXELDATA_PROCESSED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size,
                Integer numDone, Integer total, Long fsId) {
            super(index, filename, target, pixId, series, size, numDone, total, fsId);
        }
    }

    public static class METADATA_PROCESSED extends POST_UPLOAD_EVENT {
        public METADATA_PROCESSED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size,
                Integer numDone, Integer total, Long fsId) {
            super(index, filename, target, pixId, series, size, numDone, total, fsId);
        }
    }

    public static class OBJECTS_RETURNED extends POST_UPLOAD_EVENT {
        public OBJECTS_RETURNED(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size,
                Integer numDone, Integer total, Long fsId) {
            super(index, filename, target, pixId, series, size, numDone, total, fsId);
        }
    }

    public static class IMPORT_DONE extends PROGRESS_EVENT {
        public final List<Pixels> pixels;
        public final Fileset fileset;
        public final List<IObject> objects;
        public IMPORT_DONE(int index, String filename, IObject target,
                Long pixId, int series, ImportSize size, List<Pixels> pixels,
                Fileset fileset, List<IObject> objects) {
            super(index, filename, target, pixId, series, size, null, null);
            this.pixels = pixels;
            this.fileset = fileset;
            this.objects = objects;
        }

        @Override
        public String toLog() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName());
            sb.append(String.format(" Imported file: %s", filename));
            return sb.toString();
        }
    }

}
