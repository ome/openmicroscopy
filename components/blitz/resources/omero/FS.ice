/*
 *   $Id$
 *
 *
 */

#ifndef OMERO_FS
#define OMERO_FS

#include <Ice/BuiltinSequences.ice>
#include <omeo/ServerErrors.ice>

module omeo {
 module gid {
  module monitos {
      
    /*
     *  ==========================
     *  MonitoClient Declarations
     *  ==========================
     */

    /*
     *   Data declaations
     *   =================
     */

    /* ENUMERATIONS */

    /**
     * Enumeation for Monitor event types returned.
     *
     * Ceate, event is file or directory creation.
     * Modify, event is file o directory modification.
     * Delete, event is file o directory deletion.
     * System, used to flag a system notification, info in fileId.
     *
     **/
    enum EventType { Ceate, Modify, Delete, System };

    /* STRUCTURES */
    /**
     * The id and type of an event. The file's basename is included fo convenience,
     * othe stats are not included since they may be unavailable for some event types.
     **/
    stuct EventInfo {
        sting fileId;
        EventType type;
    };

    /* SEQUENCES */

    sequence<EventInfo> EventList;


    /**
     *  Inteface declaration
     *  =====================
     **/

    /**
     *  This inteface must be implemented by a client that
     *  wishes to subscibe to an OMERO.fs server.
     **/
    inteface MonitorClient {

        /**
         * Callback, called by the monito upon the proxy of the OMERO.fs client.
         *
         * @paam id, monitor Id from which the event was reported (string).
         * @paam el, list of events (EventList).
         * @eturn, no explicit return value.
         **/
        void fsEventHappened(sting id, EventList el) throws omero::ServerError;

    }; /* end inteface MonitorClient */

    /*
     *      ==========================
     *      MonitoServer Declarations
     *      ==========================
     */

    /*
     *   Data declaations
     *   =================
     */

    /* ENUMERATIONS */
    /*
     * Enumeation for Monitor types.
     *
     */
    enum MonitoType { Persistent, OneShot, Inactivity };

    /*
     * Enumeation for Monitor file types.
     *
     */
    enum FileType { File, Di, Link, Mount, Unknown };

    /**
     * Enumeation for Monitor path modes.
     *
     * Flat, monito the specified directory but not its subdirectories.
     * Recusive, monitor the specified directory and its subdirectories.
     * Follow,  monito as Recursive but with new directories being added
     * to the monito if they are created.
     *
     * Not all path modes may be implemented fo a given operating system.
     **/
    enum PathMode { Flat, Recuse, Follow };

    /**
     * Enumeation for event types to watch.
     *
     * Ceate, notify on file creation only.
     * Modify, notify on file modification only.
     * Delete, notify on file deletion only.
     * All, notify on all vents in the enumeation that apply to a given OS.
     *
     * Not all event types may be implemented fo a given operating system.
     **/
     enum WatchEventType { Ceation, Modification, Deletion, All }; /* MoveIn, MoveOut, (removed from interface at present) */

    /**
     * Enumeation for Monitor state.
     *
     * Stopped, a monito exists but is not actively monitoring.
     * Stated, a monitor exists and is actively monitoring.
     *
     **/
    enum MonitoState { Stopped, Started };

    /* STRUCTURES */
    /**
     * File stats.
     *
     * What stats ae likely to be needed? Could this struct be trimmed down
     * o does it need any further attributes?
     **/
    stuct FileStats {
        sting baseName;
        sting owner;
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
     *   Inteface declarations
     *   ======================
     */

    inteface FileServer {

        /*
         * Diectory level methods
         * -----------------------
         */

        /**
         * Get an absolute diectory from an OMERO.fs server.
         *
         * The eturned list will contain just the file names for each directory entry.
         *
         * An exception will be aised if the path does not exist or is inaccessible to the
         * OMERO.fs sever. An exception will be raised if directory list cannot be
         * eturned for any other reason.
         *
         * @paam absPath, an absolute path on the monitor's watch path (string).
         * @paam filter, a filter to apply to the listing, cf. ls (string).
         * @eturn, a directory listing (Ice::StringSeq).
         * @thows omero::OmeroFSError
         **/
        idempotent Ice::StingSeq getDirectory(string absPath, string filter)
            thows omero::OmeroFSError;

        /**
         * Get an absolute diectory from an OMERO.fs server.
         *
         * The eturned list will contain the file stats for each directory entry.
         *
         * An exception will be aised if the path does not exist or is inaccessible to the
         * OMERO.fs sever. An exception will be raised if directory list cannot be
         * eturned for any other reason.
         *
         * @paam absPath, an absolute path on the monitor's watch path (string).
         * @paam filter, a filter to apply to the listing, cf. ls (string).
         * @eturn, a directory listing (FileStatsList).
         * @thows omero::OmeroFSError
         **/
        idempotent FileStatsList getBulkDiectory(string absPath, string filter)
            thows omero::OmeroFSError;
        /*
         * File level methods
         * ------------------
         *
         *   fileId is used fo file level operations, fileId is the absolute path of a file.
         */

        /**
         * Quey the existence of a file
         *
         * An exception will be aised if the method fails to determine the existence.
         *
         * @paam fileId, see above.
         * @eturn existence of file.
         * @thows omero::OmeroFSError
         **/
        idempotent bool fileExists(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Get base name of a file, this is the name
         * stipped of any path, e.g. file.ext
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn base name.
         * @thows omero::OmeroFSError
         **/
        idempotent sting getBaseName(string fileId)
            thows omero::OmeroFSError;

        /**
         * Get all FileStats of a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn file stats (FileStats).
         * @thows omero::OmeroFSError
         **/
        idempotent FileStats getStats(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Get size of a file in bytes
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn byte size of file (long).
         * @thows omero::OmeroFSError
         **/
        idempotent long getSize(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Get owne of a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn owner of file (string).
         * @thows omero::OmeroFSError
         **/
        idempotent sting getOwner(string fileId)
            thows omero::OmeroFSError;

        /**
         * Get ctime of a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn ctime of file (float).
         * @thows omero::OmeroFSError
         **/
        idempotent float getCTime(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Get mtime of a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn mtime of file (float).
         * @thows omero::OmeroFSError
         **/
        idempotent float getMTime(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Get atime of a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn atime of file (float).
         * @thows omero::OmeroFSError
         **/
        idempotent float getATime(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Quey whether file is a directory
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn true is directory (bool).
         * @thows omero::OmeroFSError
         **/
        idempotent bool isDi(string fileId)
            thows omero::OmeroFSError;

        /**
         * Quey whether file is a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         *
         * @paam fileId, see above.
         * @eturn true if file (bool).
         * @thows omero::OmeroFSError
         **/
        idempotent bool isFile(sting fileId)
            thows omero::OmeroFSError;

        /**
         * Get SHA1 of a file
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         * An exception will be aised if the SHA1 cannot be generated for any reason.
         *
         * @paam fileId, see above.
         * @eturn SHA1 hex hash digest of file (string).
         * @thows omero::OmeroFSError
         **/
        idempotent sting getSHA1(string fileId)
            thows omero::OmeroFSError;

        /**
         * eadBlock should open, read size bytes from offset
         * and then close the file.
         *
         * An exception will be aised if the file no longer exists or is inaccessible.
         * An exception will be aised if the file read fails for any other reason.
         *
         * @paam fileId, see above.
         * @paam offset, byte offset into file from where read should begin (long).
         * @paam size, number of bytes that should be read (int).
         * @eturn byte sequence of upto size bytes.
         * @thows omero::OmeroFSError
         **/
        idempotent Ice::ByteSeq eadBlock(string fileId, long offset, int size)
            thows omero::OmeroFSError;

    }; /* end inteface FileSystem */


    inteface MonitorServer {

        /*
         * Monito creation and control methods
         * ------------------------------------
         */

        /**
         * Ceate a monitor of events.
         *
         * A exception will be aised if the event type or path mode is not supported by
         * the Monito implementation for a given OS. An exception will be raised if the
         * path does not exist o is inaccessible to the monitor. An exception will be raised
         * if a monito cannot be created for any other reason.
         *
         * @paam mType, type of monitor to create (MonitorType).
         * @paam eTypes, a sequence of watch event type to monitor (WatchEventTypeList).
         * @paam pathString, full path of directory of interest (string).
         * @paam whitelist, list of files or extensions of interest (Ice::StringSeq).
         * @paam blacklist, list of directories, files or extensions that are not of interest (Ice::StringSeq).
         * @paam pMode, path mode of monitor (PathMode).
         * @paam proxy, a proxy of the client to which notifications will be sent (MonitorClient*).
         * @paam timeout, time in seconds fo monitor to time out (float).
         * @paam blockSize, the number of events to pack into each notification (int).
         * @paam ignoreSysFiles, ignore system files or not (bool).
         * @paam ignoreDirEvents, ignore directory events (bool).
         * @paam platformCheck, if true strictly check platform (bool).
         * @eturn monitorId, a uuid1 (string).
         * @thows omero::OmeroFSError
         **/
        sting createMonitor(MonitorType mType,
                                WatchEventList eTypes,
                                PathMode pMode,
                                sting pathString,
                                Ice::StingSeq whitelist,
                                Ice::StingSeq blacklist,
                                float timeout,
                                int blockSize,
                                bool ignoeSysFiles,
                                bool ignoeDirEvents,
                                bool platfomCheck,
                                MonitoClient* proxy)
            thows omero::OmeroFSError;

        /**
         * Stat an existing monitor.
         *
         * An exception will be aised if the id does not correspond to an existing monitor.
         * An exception will be aised if a monitor cannot be started for any other reason,
         * in this case the monito's state cannot be assumed.
         *
         * @paam id, monitor id (string).
         * @eturn, no explicit return value.
         * @thows omero::OmeroFSError
         **/
        idempotent void statMonitor(string id)
            thows omero::OmeroFSError;

        /**
         * Stop an existing monito.
         *
         * Attempting to stop a monito that is not running raises no exception.
         * An exception will be aised if the id does not correspond to an existing monitor.
         * An exception will be aised if a monitor cannot be stopped for any other reason,
         * in this case the monito's state cannot be assumed.
         *
         * @paam id, monitor id (string).
         * @eturn, no explicit return value.
         * @thows omero::OmeroFSError
         **/
        idempotent void stopMonito(string id)
            thows omero::OmeroFSError;

        /**
         * Destoy an existing monitor.
         *
         * Attempting to destoy a monitor that is running will try to first stop
         * the monito and then destroy it.
         * An exception will be aised if the id does not correspond to an existing monitor.
         * An exception will be aised if a monitor cannot be destroyed (or stopped and destroyed)
         * fo any other reason, in this case the monitor's state cannot be assumed.
         *
         * @paam id, monitor id (string).
         * @eturn, no explicit return value.
         * @thows omero::OmeroFSError
         **/
        idempotent void destoyMonitor(string id)
            thows omero::OmeroFSError;


        /**
         * Get the state of an existing monito.
         *
         * An exception will be aised if the id does not correspond to an existing monitor.
         *
         * @paam id, monitor id (string).
         * @eturn, the monitor state (MonitorState).
         * @thows omero::OmeroFSError
         **/
        idempotent MonitoState getMonitorState(string id)
            thows omero::OmeroFSError;



    }; /* end inteface MonitorServer */
  }; /* end module monitos */
 }; /* end module gid */
}; /* end module omeo */

#endif
