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
     
    /*
     *   The path should be a fully qualified path.
     */
    interface MonitorServer {
    
        // a (will be unique) string id is returned
        string createMonitor(string pathString, Ice::StringSeq wl, 
                        Ice::StringSeq bl, MonitorClient* proxy);
        
        /*
         *   id is used to control monitors 
         */
        bool startMonitor(string id);
        bool stopMonitor(string id);
        bool destroyMonitor(string id);

        // id and relative path are used for directory level operations.
        Ice::StringSeq getDirectory(string id, string path, string filter);
        
        /*
         *   id and fileId are used for file level operations.
         */
        string getBaseName(string id, string fileId);
        long getSize(string id, string fileId);
        string getOwner(string id, string fileId);
        float getCTime(string id, string fileId);
        float getMTime(string id, string fileId);
        float getATime(string id, string fileId);
        
        // readBlock should open, read size bytes from offset and then close the file.
        Ice::ByteSeq readBlock(string id, string fileId, long offset, int size);
        
    }; // end interface MonitorServer
  
    /*
     *    Initially let the event be a simple path string.
     *    A more complex structure may need to de defined
     *    if information other than the file name is needed.
     */
    interface  MonitorClient {
    
        void fsEventHappened(string id, Ice::StringSeq el);
    
    }; // end interface MonitorClient
    
}; // end module monitors

#endif
