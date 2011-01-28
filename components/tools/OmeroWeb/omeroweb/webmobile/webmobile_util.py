
from datetime import datetime, timedelta
from django.core.urlresolvers import reverse

import omero
from omero.rtypes import rlong, rint, rtime
from omero.gateway import ImageWrapper, ProjectWrapper, AnnotationWrapper, BlitzObjectWrapper, DatasetWrapper
from webclient.webclient_gateway import OmeroWebGateway


        
def listMostRecentObjects(conn, limit, obj_types=None, eid=None):
    """
    Get the most recent objects supported by the timeline service: obj_types are 
    ['Project', 'Dataset', 'Image', 'Annotation'), Specifying the 
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
    for r in recent: # 'Image', 'Project', 'Dataset', 'Annotation', 'RenderingSettings'
        if obj_types and r not in obj_types:
            continue
        for value in recent[r]:
            if r == 'Annotation':
                recentItems.append(AnnotationWrapper._wrap(conn, value))
            elif r in types:
                recentItems.append( types[r](conn, value) )
            else:
                pass    # RenderingSettings
                # recentItems.append( BlitzObjectWrapper(conn, value) )  
    
    if obj_types == None or 'Annotation' in obj_types:
        anns = listMostRecentAnnotations(conn, limit, eid)
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
    
    # get ALL parent-types, child-types, namespaces:
    annTypes = ['LongAnnotation', 'TagAnnotation', 'CommentAnnotation'] # etc. Only query 1 at at time! 
    for at in annTypes:
        print at
        for a in tm.getMostRecentAnnotationLinks(None, [at], None, p):
            # TODO: maybe load parent for each link here
            wrapper = AnnotationWrapper._wrap(conn, a.child, a)
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
        
        
class RecentEvent(object):
    
    def __init__(self, blitzWrapper):
        
        self.obj = blitzWrapper
        
        # Pick a suitable display name for the object. E.g. 'Image', 'Comment' etc. 
        self.display_type = None
        annTypes = {omero.gateway.CommentAnnotationWrapper:'Comment', 
                omero.gateway.FileAnnotationWrapper:'File', 
                omero.gateway.TimestampAnnotationWrapper:'Timestamp',
                omero.gateway.BooleanAnnotationWrapper:'Boolean',
                omero.gateway.TagAnnotationWrapper:'Tag',
                omero.gateway.LongAnnotationWrapper:'Long',
                omero.gateway.DoubleAnnotationWrapper:'Double'}
        if self.obj.__class__ in annTypes:
            self.display_type = annTypes[self.obj.__class__]
            # Handle special case of Long annotations being 'Ratings'
            if self.display_type == 'Long' and self.obj.ns:
                if self.obj.ns == "openmicroscopy.org/omero/insight/rating":
                    self.display_type = 'Rating'
        else:
            self.display_type = blitzWrapper.OMERO_CLASS
            
        # Create a suitable link, either to Object itself, or to the Parent (for comments, rating)
        self.url = None
        if self.display_type == 'Image':
            self.url = reverse('webmobile_image', kwargs={'imageId':blitzWrapper.id})
        elif self.display_type in ['Project', 'Dataset']:
            self.url = reverse('webmobile_%s_details' % self.display_type.lower(), kwargs={'id':blitzWrapper.id})
        elif self.display_type in ['Rating', 'Comment']:
            if blitzWrapper.link:
                parent = blitzWrapper.link.parent
                print parent.id
                print parent.__class__
        
        
        self.timeAgo = formatTimeAgo(blitzWrapper.updateEventDate())
        
        

    