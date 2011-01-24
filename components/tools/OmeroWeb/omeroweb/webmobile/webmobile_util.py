
from datetime import datetime, timedelta

import omero
from omero.rtypes import rlong, rint, rtime
from omero.gateway import ImageWrapper, ProjectWrapper, AnnotationWrapper, BlitzObjectWrapper, DatasetWrapper
from webclient.webclient_gateway import OmeroWebGateway


        
def listMostRecentObjects(conn, limit, eid=None):
    """
    Get the most recent objects supported by the timeline service (Project, Dataset, Image), Specifying the 
    number of each you want 'limit', belonging to the specified experimenter 'eid'
    or current Group if 'eid' is None.
    """
    
    from datetime import datetime
    now = datetime.now()
    
    tm = conn.getTimelineService()
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    if eid:
        f.ownerId = rlong(eid)
    else:
        f.groupId = rlong(conn.getEventContext().groupId)
    f.limit = rint(limit)
    p.theFilter = f

    types = {'Image':ImageWrapper, 'Project':ProjectWrapper, 'Dataset':DatasetWrapper}
    recent = tm.getMostRecentObjects(None, p, False)

    recentItems = []
    for r in recent:
        for value in recent[r]:
            if r == 'Annotation':
                recentItems.append(AnnotationWrapper._wrap(conn, value))
            elif r in types:
                recentItems.append( types[r](conn, value) )
            else:
                pass    # RenderingSettings
                # recentItems.append( BlitzObjectWrapper(conn, value) )  
    
    anns = listMostRecentAnnotations(conn, limit, eid)
    for e in anns:
        print ""
        print type(e.link.parent)
        print type(e)
        print e.updateEventDate()
        
    recentItems.extend(anns)
    recentItems.sort(key = lambda x: x.updateEventDate())
    recentItems.reverse()
    return recentItems
    

def listMostRecentAnnotations (conn, limit, eid=None):
    """
    Retrieve most recent annotations available
    
    @return:    Generator yielding BlitzObjectWrapper
    @rtype:     L{BlitzObjectWrapper} generator
    """
    
    tm = conn.getTimelineService()
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    if eid:
        f.ownerId = rlong(eid)
    else:
        f.groupId = rlong(conn.getEventContext().groupId)
    f.limit = rint(limit)
    p.theFilter = f
    anns = []
    types = {omero.gateway.CommentAnnotationWrapper:'Comment', 
            omero.gateway.FileAnnotationWrapper:'File', 
            omero.gateway.TimestampAnnotationWrapper:'Timestamp',
            omero.gateway.BooleanAnnotationWrapper:'Boolean',
            omero.gateway.TagAnnotationWrapper:'Tag',
            omero.gateway.LongAnnotationWrapper:'Long',
            omero.gateway.DoubleAnnotationWrapper:'Double'}
    # get ALL parent-types, child-types, namespaces:
    annTypes = ['LongAnnotation', 'TagAnnotation', 'CommentAnnotation'] # etc. Only query 1 at at time! 
    for at in annTypes:
        print at
        for a in tm.getMostRecentAnnotationLinks(None, [at], None, p):
            # TODO: maybe load parent for each link here
            wrapper = AnnotationWrapper._wrap(conn, a.child, a)
            if wrapper.__class__ in types:
                # Abuse of the OMERO_CLASS attribute (normally None for annotations)
                wrapper.OMERO_CLASS = types[wrapper.__class__]
                if types[wrapper.__class__] == 'Long' and a.child.ns:
                    if a.child.ns.val == "openmicroscopy.org/omero/insight/rating":
                        wrapper.OMERO_CLASS = 'Rating'
            anns.append(wrapper)
    return anns

        
def formatTimeAgo(eventDate):
    """
    Formats a datetime wrt 'now'. 
    E.g. '3 hours 25 mins ago', 'Yesterday at 18:30', 'Friday 14 Jan at 10.44'
    """
    
    now = datetime.now()
    year = now.year
    month = now.month
    day = now.day
    one_day = timedelta(1)
    yesterday_start = datetime(year, month, day-1)
    ago = now - eventDate
    
    if ago < one_day:
        hh, rem = divmod(ago.seconds, 3600)
        mm, rem = divmod(rem, 60)
        return "%s hours %s mins ago" % (hh, mm)
    elif eventDate > yesterday_start:
        return "Yesterday at %s" % eventDate.strftime("%H:%M")
    else:
        return "%s" % eventDate.strftime("%A, %d %b at %H:%M")
    