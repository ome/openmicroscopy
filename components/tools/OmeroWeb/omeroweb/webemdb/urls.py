from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webemdb import views
import os

urlpatterns = patterns('',
    url( r'^$', views.index, name='webemdb_index' ),
    url( r'^logout/$', views.logout, name='webemdb_logout' ),
    url( r'^loggedout/$', views.loggedout, name='webemdb_loggedout' ),
    
    url( r'^entry/(?P<entryId>[0-9]+)/$', views.entry, name='webemdb_entry' ),
    # bit of a hack - should only need fileId - see views.file for info
    url( r'^entry/(?P<entryId>[0-9]+)/file/(?P<fileId>[0-9]+)/$', views.file, name='webemdb_file' ),
    url( r'^entry/(?P<entryId>[0-9]+)/file/(?P<fileId>[0-9]+)\.bit$', views.file, name='webemdb_bit' ),
    # look up the preview gif file for an entry based on name
    url( r'^entry/(?P<entryId>[0-9]+)/gif/$', views.gif, name='webemdb_gif' ),
    # for downloading whole mrc.map    not working (not used currently)
    url( r'^img/(?P<imageId>[0-9]+)/map/(?P<fileId>[0-9]+)/$', views.map, name='webemdb_map' ),
    
    # page for OpenAstex Viewer. Project name and bit mask fileId
    url( r'^oa_viewer/(?P<entryId>[0-9]+)/file/(?P<fileId>[0-9]+)/$', views.oa_viewer, name='webemdb_oa_viewer' ),
    
    url( r'^viewport/(?P<imageId>[0-9]+)/$', views.viewport, name='webemdb_viewport' ),
    
    # view the associated data for an entry
    url( r'^data/(?P<entryId>[0-9]+)/$', views.data, name='webemdb_data' ),
    
    # browse by annotations. E.g. publications
    url( r'^publications/$', views.publications, name='webemdb_publications' ),
    url( r'^getEntriesByPub/(?P<publicationId>[0-9]+)/$', views.getEntriesByPub, name='webemdb_getEntriesByPub' ),
    
    # define the sym link for media. 
    url( r'appmedia/webemdb/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media', 'webemdb').replace('\\','/') }, name="webemdb" ),
    
)
