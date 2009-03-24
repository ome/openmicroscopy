"""
    OMERO.fs  DropBox implementation of a MonitorClient

    
"""
import logging
import fsLogger
log = logging.getLogger("fs.DropBoxMonitorClient")
from traceback import format_exc

import string
import subprocess as sp
import os
import uuid

import omero
import Ice
import IceGrid
import Glacier2

import monitors
import fsConfig as config

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
        self.master = None
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
            
            try:
                ic = Ice.initialize(["--Ice.Config=etc/internal.cfg"])
                query = ic.stringToProxy("IceGrid/Query")
                query = IceGrid.QueryPrx.checkedCast(query)
                blitz = query.findAllObjectsByType("::Glacier2::SessionManager")[0]
                blitz = Glacier2.SessionManagerPrx.checkedCast(blitz)
                sf = blitz.create("root", None, {"omero.client.uuid":str(uuid.uuid1())})
                sf = omero.api.ServiceFactoryPrx.checkedCast(sf)
                sf.detachOnDestroy()
                sf.destroy()
                sessionUuid = sf.ice_getIdentity().name

                root = omero.client(config.host, config.port)
                root.joinSession(sessionUuid) 
                                   
                for fileInfo in eventList:
                    log.info("New file %s", fileInfo.fileId)
                    fileParts = fileInfo.fileId.split("/")
                    base = fileParts.index(config.dropBoxDir)
                    try:
                        exName = fileParts[base+2]
                        fileParts[base+3] 
                        if exName not in config.excludedUsers:                 
                            exp = root.sf.getAdminService().lookupExperimenter(exName)
                            if exName == exp._omeName._val:
                                p = omero.sys.Principal()
                                p.name  = exName
                                p.group = "user"
                                p.eventType = "Test"

                                sess = root.sf.getSessionService().createSessionWithTimeout(p, 60000L)

                                client = omero.client(config.host, config.port)
                                user_sess = client.createSession(sess.uuid, sess.uuid)

                                key = user_sess.getAdminService().getEventContext().sessionUuid
                                log.info("Importing file using session key = %s", key)
                                command = [config.climporter +
                                            " -s " + config.host +
                                            " -k " + key +
                                            " " + fileInfo.fileId]
                                output = sp.Popen(command, stdout=sp.PIPE, stderr=sp.PIPE, shell=True).communicate()
                                if log.getEffectiveLevel() == logging.DEBUG:
                                    log.debug("Import call finished. Result:")
                                    log.debug("***** start of output from importer-cli *****")
                                    log.debug("To stdout: ")
                                    for line in output[0].split('\n'):
                                        log.debug(line)
                                    log.debug("To stderr: ")
                                    for line in output[1].split('\n'):
                                        log.debug(line)
                                    log.debug("***** end *****")
                                log.info("Import completed on session key = %s", key)
                            else:
                                log.info("File not imported: user unknown: %s", exName)
                        else:
                            log.info("File not imported: user excluded: %s", exName)
                           
                    except IndexError:
                        log.info("File not imported: file not copied into user level directory")
                    except OSError:
                        log.exception("Import failed: ")
                    except:
                        log.exception("Import failed: ")
            except:
                log.exception("Failed to get or join session: ")              
        else:
            log.error("Unknown fs server id: %s", id)

                
    def setMaster(self, master):
        """
            Setter for FSDropBox 

            :Parameters:
                master : DropBox
                    DropBox Server

            :return: No explicit return value.

        """
        self.master = master


    def setServerProxy(self, serverProxy):
        """
            Setter for serverProxy

            :Parameters:
                serverProxy : monitors.MonitorServerPrx
                    proxy to remote server object

            :return: No explicit return value.

        """
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
        

