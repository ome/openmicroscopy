"""
    OMERO.fs Example implemenetation of a MonitorClient

    
"""

import monitors


class MonitorClientI(monitors.MonitorClient):
    """
        Implementation of the MonitorClient.
        
        The interface of the MonitorClient is defined in omerofs.ice and
        contains the single callback below.
        
    """

    def __init__(self):
        """
            Intialise the instance variables.
        
        """
        #: Reference back to FSServer.
        self.serverProxy = None
        #: Id
        self.id = ''

    def fsEventHappened(self, id, eventList, current=None):
        """
            This is an example callback.
            
            If new files appear on the watch the list is sent as an argument.
            The id should match for the events to be relevant. In this simple 
            exmple the call back outputs the list of new files to stdout.
            
            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Watch created
                    by the OMERO.fs Server.
                      
                eventList : list<string>
                    A list of events, in the current implementation this is 
                    a list of strings representing the full path names of new files.
                    
                current 
                    An ICE context, this parameter is required to be present
                    in an ICE callback.
                           
            :return: No explicit return value.
            
        """                
        if self.id == id:
            for fileInfo in eventList:
                print "%s Event: New file %s" % (fileInfo.type, fileInfo.fileId)
                fileStats = self.serverProxy.getStats(fileInfo.fileId)
                print str(fileStats)
                print
                print "SHA1 is %s" % self.serverProxy.getSHA1(fileInfo.fileId)
                print
                print 'Directory following file event'
                dir = self.serverProxy.getMonitorDirectory(self.id, '', '*')
                for fname in dir:
                    print fname
                print
                print 'Copying file to local system...'
                size = 1024*1024/2 # Needs to be less that 1MB to avoid Ice MemoryLimitException
                localName = 'test.'+self.serverProxy.getBaseName(fileInfo.fileId)
                file = open(localName,'wb')
                offset = 0
                data = self.serverProxy.readBlock(fileInfo.fileId, offset, size, None)
                while len(data) != 0:
                    file.write(data)
                    file.flush()
                    offset += len(data)
                    data = self.serverProxy.readBlock(fileInfo.fileId, offset, size, None)
                file.close()
                print '... %s written' % localName
                
                
                

                
    def setServerProxy(self, serverProxy):
        """
            Setter for serverProxy
            
            :Parameters:
                serverProxy : monitors.MonitorServerPrx
                    proxy to remote server object
                    
            :return: No explicit return value.
            
        """
        #: A string uniquely identifying the OMERO.fs Monitor
        self.serverProxy = serverProxy


    def setId(self, id):
        """
            Setter for id
            
            :Parameters:
                id : string
                    A string uniquely identifying the OMERO.fs Monitor created
                    by the OMERO.fs Server.
                    
            :return: No explicit return value.
            
        """
        #: A string uniquely identifying the OMERO.fs Monitor
        self.id = id
        

