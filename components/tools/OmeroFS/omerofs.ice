/*
 *   $Id$
 * 
 *
 */

#ifndef OMERO_FS
#define OMERO_FS

#include <Ice/BuiltinSequences.ice>

module monitors {
    
    /*
     *   Forward declarations.
     */
    interface MonitorClient;

    struct FileStats {
        string owner;
        long size;
        float mTime;
        float cTime;
        float aTime;
    };
     
    /*
     *   The path should be a fully qualified path.
     */
    interface MonitorServer {
    
        // a (will be unique) string id is returned
        string createMonitor(string pathString, Ice::StringSeq wl, 
                        Ice::StringSeq bl, MonitorClient* proxy);
        
        /*
         *   id is used to control monitors 
         *   id is a uuid1 at present.
         */
        bool startMonitor(string id);
        bool stopMonitor(string id);
        bool destroyMonitor(string id);

        // directory level operations.
        Ice::StringSeq getMonitorDirectory(string id, string relPath, string filter);
        Ice::StringSeq getDirectory(string absPath, string filter);
        
        /*
         *   fileId is used for file level operations.
         *   fileId is omero-fs://url/path/to/file.ext
         */
        string getBaseName(string fileId);
        FileStats getStats(string fileId);       

        long getSize(string fileId);
        string getOwner(string fileId);
        float getCTime(string fileId);
        float getMTime(string fileId);
        float getATime(string fileId);
        
        
        // readBlock should open, read size bytes from offset and then close the file.
        Ice::ByteSeq readBlock(string fileId, long offset, int size);
        
    }; // end interface MonitorServer
  
    
    struct FileInfo {
        string fileId; //see above.
        string baseName;
        long size;
        float mTime;
    };
    
    sequence<FileInfo> EventList;
    
    /*
     *    Initially let the event be a simple path string.
     *    A more complex structure may need to de defined
     *    if information other than the file name is needed.
     *
     *    See above for id
     */
    interface  MonitorClient {
    
        void fsEventHappened(string id, EventList el);
    
    }; // end interface MonitorClient
    
}; // end module monitors

#endif
