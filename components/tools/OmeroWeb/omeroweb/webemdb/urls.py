from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webemdb import views
import os

urlpatterns = patterns('',
    url( r'^$', views.index, name='webemdb_index' ),
    url( r'^logout/$', views.logout, name='webemdb_logout' ),
    url( r'^loggedout/$', views.loggedout, name='webemdb_loggedout' ),
    
    url( r'^entry/(?P<entryId>[0-9]+)/$', views.entry, name='webemdb_entry' ),
    
    # serve original files, identified by annotation ID. Additional urls for defined ext (.bit, .pdb.gz, .map etc) for OA-viewer
    url( r'^file/(?P<fileId>[0-9]+)/$', views.getFile, name='webemdb_file' ),
    url( r'^file/(?P<fileId>[0-9]+)\.bit$', views.getFile, name='webemdb_bit' ),
    url( r'^file/(?P<fileId>[0-9]+)\.pdb\.gz$', views.getFile, name='webemdb_pdb' ),
    url( r'^file/(?P<fileId>[0-9]+)\.map$', views.getFile, name='webemdb_map' ),
    
    # look up the preview gif file for an entry based on name
    url( r'^entry/(?P<entryId>[0-9]+)/gif/$', views.gif, name='webemdb_gif' ),
    
    # page for OpenAstex Viewer. Project name and bit mask fileId
    url( r'^oa_viewer/file/(?P<fileId>[0-9]+)/$', views.oa_viewer, name='webemdb_oa_viewer' ),
    
    url( r'^viewport/(?P<imageId>[0-9]+)/$', views.viewport, name='webemdb_viewport' ),
    
    # view the associated data for an entry
    url( r'^data/(?P<entryId>[0-9]+)/$', views.data, name='webemdb_data' ),
    url( r'^dataset/(?P<datasetId>[0-9]+)/$', views.dataset, name='webemdb_dataset' ),
    url( r'^image/(?P<imageId>[0-9]+)/$', views.image, name='webemdb_image' ),
    
    # browse by annotations. E.g. publications
    url( r'^publications/$', views.publications, name='webemdb_publications' ),
    url( r'^getEntriesByPub/(?P<publicationId>[0-9]+)/$', views.getEntriesByPub, name='webemdb_getEntriesByPub' ),
    
    # view an EMAN2 filter on an image
    url( r'^eman2_filter/(?P<imageId>[0-9]+)/fft/$', views.eman, {"filter": "fft"}, name='webemdb_eman_fft' ),
    url( r'^eman2_filter/(?P<imageId>[0-9]+)/median/$', views.eman, {"filter": "median"}, name='webemdb_eman_median' ),
    url( r'^eman2_filter/(?P<imageId>[0-9]+)/median/(?P<radius>[0-9]+)/$', views.eman, {"filter": "median"}, name='webemdb_eman_median' ),
    url( r'^eman2_filter/(?P<imageId>[0-9]+)/log/$', views.eman, {"filter": "log"}, name='webemdb_eman_log' ),
    
    # define the sym link for media. 
    url( r'appmedia/webemdb/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media', 'webemdb').replace('\\','/') }, name="webemdb" ),
    
    # urls for scripting service
    url( r'^script_form/(?P<scriptId>[0-9]+)/$', views.script_form, name='webemdb_script_form' ),
    url( r'^script_run/(?P<scriptId>[0-9]+)/$', views.script_run, name='webemdb_script_run' ),
)
