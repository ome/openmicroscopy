/*
 *   $Id$
 * 
 *
 */

#ifndef OMERO_FS
#define OMERO_FS

module monitors {
    
    /*
     * Forward declarations.
     */
    interface MonitorClient;
    
    /*
     * A Whitelist is a sequence of file extensions that should be monitored.
     * Each extension should be of the form, ".ext"
     */
    sequence<string> Whitelist;
    
    /*
     * A Blacklist is a sequence of file paths that should be excluded from
     * the monitoring. Each path should be relative to the path being 
     * monitored.
     */
    sequence<string> Blacklist;
    
    /*
     * The path should be a fully qualified path.
     */
    interface MonitorServer {
    
        string createMonitor(string pathString, Whitelist wl, 
                        Blacklist bl, MonitorClient* proxy);
        bool startMonitor(string id);
        bool stopMonitor(string id);
        bool destroyMonitor(string id);
        
    }; // end interface MonitorServer
  
    /*
     * Initially let the event be a simple path string.
     * A more complex structure may need to de defined
     * if information other than the file name is needed.
     */
    sequence<string> EventList;

    interface  MonitorClient {
    
        void fsEventHappened(string id, EventList el);
    
    }; // end interface MonitorClient
    
}; // end module monitors

#endif
