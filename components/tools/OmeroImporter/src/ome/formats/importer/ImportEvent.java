/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import omero.model.IObject;

/**
 * Utility class which configures the Import.
 * 
 * @since Beta4.1
 */
public class ImportEvent {

    private static class __COUNT_EVENT extends ImportEvent {
        public final String shortName;
        public final Integer index;
        public final Integer numDone;
        public final Integer total;

        __COUNT_EVENT(String shortName, Integer index, Integer numDone,
                Integer total) {
            this.shortName = shortName;
            this.index = index;
            this.numDone = numDone;
            this.total = total;
        }
    }

    private static class __TARGET_EVENT extends ImportEvent {
        public final IObject target;
        public final Long pixId;
        public final int series;
        public final ImportSize size;

        __TARGET_EVENT(IObject target, Long pixId, int series, ImportSize size) {
            this.target = target;
            this.pixId = pixId;
            this.series = series;
            this.size = size;
        }
    }

    // Data-less events

    public static class ADD extends ImportEvent {
    }

    public static class ERRORS_PENDING extends ImportEvent {
    }

    public static class ERRORS_SEND extends ImportEvent {

    }

    // misc-events

    public static class DEBUG_SEND extends ImportEvent {
        public final boolean sendFiles;

        public DEBUG_SEND(boolean sendFiles) {
            this.sendFiles = sendFiles;
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
    }

    // count-events

    public static class LOADING_IMAGE extends __COUNT_EVENT {
        public LOADING_IMAGE(String shortName, Integer index, Integer numDone,
                Integer total) {
            super(shortName, index, numDone, total);
        }
    }

    public static class LOADED_IMAGE extends __COUNT_EVENT {
        public LOADED_IMAGE(String shortName, Integer index, Integer numDone,
                Integer total) {
            super(shortName, index, numDone, total);
        }
    }

    // target-events

    public static class DATASET_STORED extends __TARGET_EVENT {
        public DATASET_STORED(IObject target, Long pixId, int series,
                ImportSize size) {
            super(target, pixId, series, size);
        }
    }

    public static class DATA_STORED extends __TARGET_EVENT {
        public DATA_STORED(IObject target, Long pixId, int series,
                ImportSize size) {
            super(target, pixId, series, size);
        }
    }

    public static class IMPORT_ARCHIVING extends __TARGET_EVENT {
        public IMPORT_ARCHIVING(IObject target, Long pixId, int series,
                ImportSize size) {
            super(target, pixId, series, size);
        }
    }

    public static class IMPORT_THUMBNAILING extends __TARGET_EVENT {
        public IMPORT_THUMBNAILING(IObject target, Long pixId, int series,
                ImportSize size) {
            super(target, pixId, series, size);
        }
    }

    public static class IMPORT_DONE extends __TARGET_EVENT {
        public IMPORT_DONE(IObject target, Long pixId, int series,
                ImportSize size) {
            super(target, pixId, series, size);
        }
    }

    public static final String REMOVE = "remove";
    public static final String IMPORT = "import";
    public static final String LOGIN = "login";
    public static final String LOGIN_CANCELLED = "login_cancelled";
    public static final String REFRESH = "refresh";
    public static final String CLEARDONE = "clear_done";
    public static final String CLEARFAILED = "clear_failed";
    public static final String HISTORYSEARCH = "history_search";
    public static final String CLEARHISTORY = "history_clear";
    public static final String LOADED_IMAGE = "loaded_image";
    public static final String DATASET_STORED = "dataset_stored";
    public static final String DATA_STORED = "data_stored";
    public static final String IMPORT_DONE = "import_done";
    public static final String HISTORYREIMPORT = "history_reimport";
    public static final String IO_EXCEPTION = "java.io.IOException";
    public static final String IMPORT_THUMBNAILING = "import_thumbnailing";
    public static final String FILE_UPLOAD_STARTED = "file_upload_started";
    public static final String FILE_UPLOAD_COMPLETE = "file_upload_complete";
    public static final String FILE_UPLOAD_FAILED = "file_upload_failed";
    public static final Object FILE_UPLOAD_ERROR = "file_upload_error";
    public static final Object FILE_UPLOAD_BYTES = "file_upload_bytes";
    public static final Object FILE_UPLOAD_FINSIHED = "file_upload_finished";
    public static final Object ERRORS_UPLOAD_CANCELLED = "errors_upload_cancelled";
    public static final Object ERRORS_COMPLETE = "errors_complete";
    public static final String SHOW_LOG_FILE_LOCATION = "show_log_file_location";

}
