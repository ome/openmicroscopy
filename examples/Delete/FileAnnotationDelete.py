"""
Uses the default {@link DeleteCallbackI} instance.
to delete a FileAnnotation along with its associated
OriginalFile and any annotation links.
"""

import omero
import omero.callbacks

c = omero.client()
ice_config = c.getProperty("Ice.Config")

from omero.rtypes import *
from omero.model import *

try:
    s = c.createSession()
    d = DatasetI()
    d.setName(rstring("FileAnnotationDelete"))
    fa = FileAnnotationI()
    file = c.upload(ice_config)
    fa.setFile(file)
    d.linkAnnotation(fa)
    d = s.getUpdateService().saveAndReturnObject(d)
    fa = d.linkedAnnotationList()[0]

    deleteServicePrx = s.getDeleteService();
    dc = omero.api.delete.DeleteCommand("/Annotation", fa.id.val, None)
    deleteHandlePrx = deleteServicePrx .queueDelete([dc])
    cb = omero.callbacks.DeleteCallbackI(c, deleteHandlePrx)

    try:

        try:
            cb.loop(10, 500)
        except omero.LockTimeout:
            print "Not finished in 5 seconds. Cancelling..."
            if not deleteHandlePrx.cancel():
                print "ERROR: Failed to cancel"

        reports = deleteHandlePrx.report()
        r = reports[0] # We only sent one command
        print "Report:error=%s,warning=%s,deleted=%s" % \
            (r.error, r.warning, r.actualDeletes)

    finally:
        cb.close()

finally:
    c.closeSession()
