/*
 *   $Id$
 *
 *
 */

#ifndef OMERO_FS
#define OMERO_FS

#include <Ice/BuiltinSequences.ice>
#include <omero/ServerErrors.ice>

module omero {
 module grid {
  module monitors {

    /*
     *  ==========================
     *  MonitorClient Declarations
     *  ==========================
     */

    /*
     *   Data declarations
     *   =================
     */

    /* ENUMERATIONS */

    /**
     * Enumeration for Monitor event types returned.
     *
     * Create, event is file or directory creation.
     * Modify, event is file or directory modification.
     * Delete, event is file or directory deletion.
     * System, used to flag a system notification, info in fileId.
     *
     **/
    enum EventType { Create, Modify, Delete, System };

    /* STRUCTURES */
    /**
     * The id and type of an event. The file's basename is included for convenience,
     * other stats are not included since they may be unavailable for some event types.
     **/
    struct EventInfo {
        string fileId;
        EventType type;
    };

    /* SEQUENCES */

    sequence<EventInfo> EventList;


    /**
     *  Interface declaration
     *  =====================
     **/

    /**
     *  This interface must be implemented by a client that
     *  wishes to subscribe to an OMERO.fs server.
     **/
    interface MonitorClient {

        /**
         * Callback, called by the monitor upon the proxy of the OMERO.fs client.
         *
         * @param id, monitor Id from which the event was reported (string).
         * @param el, list of events (EventList).
         * @return, no explicit return value.
         **/
        void fsEventHappened(string id, EventList el) throws omero::ServerError;

    }; /* end interface MonitorClient */

    /*
     *      ==========================
     *      MonitorServer Declarations
     *      ==========================
     */

    /*
     *   Data declarations
     *   =================
     */

    /* ENUMERATIONS */
    /*
     * Enumeration for Monitor types.
     *
     */
    enum MonitorType { Persistent, OneShot, Inactivity };

    /*
     * Enumeration for Monitor file types.
     *
     */
    enum FileType { File, Dir, Link, Mount, Unknown };

    /**
     * Enumeration for Monitor path modes.
     *
     * Flat, monitor the specified directory but not its subdirectories.
     * Recursive, monitor the specified directory and its subdirectories.
     * Follow,  monitor as Recursive but with new directories being added
     * to the monitor if they are created.
     *
     * Not all path modes may be implemented for a given operating system.
     **/
    enum PathMode { Flat, Recurse, Follow };

    /**
     * Enumeration for event types to watch.
     *
     * Create, notify on file creation only.
     * Modify, notify on file modification only.
     * Delete, notify on file deletion only.
     * All, notify on all vents in the enumeration that apply to a given OS.
     *
     * Not all event types may be implemented for a given operating system.
     **/
     enum WatchEventType { Creation, Modification, Deletion, All }; /* MoveIn, MoveOut, (removed from interface at present) */

    /**
     * Enumeration for Monitor state.
     *
     * Stopped, a monitor exists but is not actively monitoring.
     * Started, a monitor exists and is actively monitoring.
     *
     **/
    enum MonitorState { Stopped, Started };

    /* STRUCTURES */
    /**
     * File stats.
     *
     * What stats are likely to be needed? Could this struct be trimmed down
     * or does it need any further attributes?
     **/
    struct FileStats {
        string baseName;
        string owner;
        long size;
        float mTime;
        float cTime;
        float aTime;
        FileType type;
    };

    /* SEQUENCES */

    sequence<WatchEventType> WatchEventList;
    sequence<FileStats> FileStatsList;

    /*
     *   Interface declarations
     *   ======================
     */

    interface FileServer {

        /*
         * Directory level methods
         * -----------------------
         */

        /**
         * Get an absolute directory from an OMERO.fs server.
         *
         * The returned list will contain just the file names for each directory entry.
         *
         * An exception will be raised if the path does not exist or is inaccessible to the
         * OMERO.fs server. An exception will be raised if directory list cannot be
         * returned for any other reason.
         *
         * @param absPath, an absolute path on the monitor's watch path (string).
         * @param filter, a filter to apply to the listing, cf. ls (string).
         * @return, a directory listing (Ice::StringSeq).
         * @throws omero::OmeroFSError
         **/
        idempotent Ice::StringSeq getDirectory(string absPath, string filter)
            throws omero::OmeroFSError;

        /**
         * Get an absolute directory from an OMERO.fs server.
         *
         * The returned list will contain the file stats for each directory entry.
         *
         * An exception will be raised if the path does not exist or is inaccessible to the
         * OMERO.fs server. An exception will be raised if directory list cannot be
         * returned for any other reason.
         *
         * @param absPath, an absolute path on the monitor's watch path (string).
         * @param filter, a filter to apply to the listing, cf. ls (string).
         * @return, a directory listing (FileStatsList).
         * @throws omero::OmeroFSError
         **/
        idempotent FileStatsList getBulkDirectory(string absPath, string filter)
            throws omero::OmeroFSError;
        /*
         * File level methods
         * ------------------
         *
         *   fileId is used for file level operations, fileId is the absolute path of a file.
         */

        /**
         * Query the existence of a file
         *
         * An exception will be raised if the method fails to determine the existence.
         *
         * @param fileId, see above.
         * @return existence of file.
         * @throws omero::OmeroFSError
         **/
        idempotent bool fileExists(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get base name of a file, this is the name
         * stripped of any path, e.g. file.ext
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return base name.
         * @throws omero::OmeroFSError
         **/
        idempotent string getBaseName(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get all FileStats of a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return file stats (FileStats).
         * @throws omero::OmeroFSError
         **/
        idempotent FileStats getStats(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get size of a file in bytes
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return byte size of file (long).
         * @throws omero::OmeroFSError
         **/
        idempotent long getSize(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get owner of a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return owner of file (string).
         * @throws omero::OmeroFSError
         **/
        idempotent string getOwner(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get ctime of a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return ctime of file (float).
         * @throws omero::OmeroFSError
         **/
        idempotent float getCTime(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get mtime of a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return mtime of file (float).
         * @throws omero::OmeroFSError
         **/
        idempotent float getMTime(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get atime of a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return atime of file (float).
         * @throws omero::OmeroFSError
         **/
        idempotent float getATime(string fileId)
            throws omero::OmeroFSError;

        /**
         * Query whether file is a directory
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return true is directory (bool).
         * @throws omero::OmeroFSError
         **/
        idempotent bool isDir(string fileId)
            throws omero::OmeroFSError;

        /**
         * Query whether file is a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         *
         * @param fileId, see above.
         * @return true if file (bool).
         * @throws omero::OmeroFSError
         **/
        idempotent bool isFile(string fileId)
            throws omero::OmeroFSError;

        /**
         * Get SHA1 of a file
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         * An exception will be raised if the SHA1 cannot be generated for any reason.
         *
         * @param fileId, see above.
         * @return SHA1 hex hash digest of file (string).
         * @throws omero::OmeroFSError
         **/
        idempotent string getSHA1(string fileId)
            throws omero::OmeroFSError;

        /**
         * readBlock should open, read size bytes from offset
         * and then close the file.
         *
         * An exception will be raised if the file no longer exists or is inaccessible.
         * An exception will be raised if the file read fails for any other reason.
         *
         * @param fileId, see above.
         * @param offset, byte offset into file from where read should begin (long).
         * @param size, number of bytes that should be read (int).
         * @return byte sequence of upto size bytes.
         * @throws omero::OmeroFSError
         **/
        idempotent Ice::ByteSeq readBlock(string fileId, long offset, int size)
            throws omero::OmeroFSError;

    }; /* end interface FileSystem */


    interface MonitorServer {

        /*
         * Monitor creation and control methods
         * ------------------------------------
         */

        /**
         * Create a monitor of events.
         *
         * A exception will be raised if the event type or path mode is not supported by
         * the Monitor implementation for a given OS. An exception will be raised if the
         * path does not exist or is inaccessible to the monitor. An exception will be raised
         * if a monitor cannot be created for any other reason.
         *
         * @param mType, type of monitor to create (MonitorType).
         * @param eTypes, a sequence of watch event type to monitor (WatchEventTypeList).
         * @param pathString, full path of directory of interest (string).
         * @param whitelist, list of files or extensions of interest (Ice::StringSeq).
         * @param blacklist, list of directories, files or extensions that are not of interest (Ice::StringSeq).
         * @param pMode, path mode of monitor (PathMode).
         * @param proxy, a proxy of the client to which notifications will be sent (MonitorClient*).
         * @param timeout, time in seconds fo monitor to time out (float).
         * @param blockSize, the number of events to pack into each notification (int).
         * @param ignoreSysFiles, ignore system files or not (bool).
         * @param ignoreDirEvents, ignore directory events (bool).
         * @param platformCheck, if true strictly check platform (bool).
         * @return monitorId, a uuid1 (string).
         * @throws omero::OmeroFSError
         **/
        string createMonitor(MonitorType mType,
                                WatchEventList eTypes,
                                PathMode pMode,
                                string pathString,
                                Ice::StringSeq whitelist,
                                Ice::StringSeq blacklist,
                                float timeout,
                                int blockSize,
                                bool ignoreSysFiles,
                                bool ignoreDirEvents,
                                bool platformCheck,
                                MonitorClient* proxy)
            throws omero::OmeroFSError;

        /**
         * Start an existing monitor.
         *
         * An exception will be raised if the id does not correspond to an existing monitor.
         * An exception will be raised if a monitor cannot be started for any other reason,
         * in this case the monitor's state cannot be assumed.
         *
         * @param id, monitor id (string).
         * @return, no explicit return value.
         * @throws omero::OmeroFSError
         **/
        idempotent void startMonitor(string id)
            throws omero::OmeroFSError;

        /**
         * Stop an existing monitor.
         *
         * Attempting to stop a monitor that is not running raises no exception.
         * An exception will be raised if the id does not correspond to an existing monitor.
         * An exception will be raised if a monitor cannot be stopped for any other reason,
         * in this case the monitor's state cannot be assumed.
         *
         * @param id, monitor id (string).
         * @return, no explicit return value.
         * @throws omero::OmeroFSError
         **/
        idempotent void stopMonitor(string id)
            throws omero::OmeroFSError;

        /**
         * Destroy an existing monitor.
         *
         * Attempting to destroy a monitor that is running will try to first stop
         * the monitor and then destroy it.
         * An exception will be raised if the id does not correspond to an existing monitor.
         * An exception will be raised if a monitor cannot be destroyed (or stopped and destroyed)
         * for any other reason, in this case the monitor's state cannot be assumed.
         *
         * @param id, monitor id (string).
         * @return, no explicit return value.
         * @throws omero::OmeroFSError
         **/
        idempotent void destroyMonitor(string id)
            throws omero::OmeroFSError;


        /**
         * Get the state of an existing monitor.
         *
         * An exception will be raised if the id does not correspond to an existing monitor.
         *
         * @param id, monitor id (string).
         * @return, the monitor state (MonitorState).
         * @throws omero::OmeroFSError
         **/
        idempotent MonitorState getMonitorState(string id)
            throws omero::OmeroFSError;



    }; /* end interface MonitorServer */
  }; /* end module monitors */
 }; /* end module grid */
}; /* end module omero */

#endif
