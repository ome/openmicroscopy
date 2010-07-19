from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webemdb import views
import os

urlpatterns = patterns('',
    url( r'^$', views.index, name='webemdb_index' ),
    url( r'^logout/$', views.logout, name='webemdb_logout' ),
    
    url( r'^entry/(?P<entryId>[0-9]+)/$', views.entry, name='webemdb_entry' ),
    url( r'^entry/(?P<entryId>[0-9]+)/file/(?P<fileId>[0-9]+)/$', views.file, name='webemdb_file' ),
    url( r'^img/(?P<imageId>[0-9]+)/map/(?P<fileId>[0-9]+)/$', views.map, name='webemdb_map' ),
    
    # page for OpenAstex Viewer. Project name and bit mask fileId
    url( r'^oa_viewer/(?P<entryId>[0-9]+)/file/(?P<fileId>[0-9]+)/$', views.oa_viewer, name='webemdb_oa_viewer' ),
    
    # define the sym link for media. 
    url( r'appmedia/webemdb/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media', 'webemdb').replace('\\','/') }, name="webemdb" ),
    
)
