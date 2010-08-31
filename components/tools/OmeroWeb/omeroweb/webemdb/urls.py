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
    url( r'^mapmodel/(?P<imageId>[0-9]+)/$', views.mapmodel, name='webemdb_mapmodel' ),     # uses OMERO Image-Id
    url( r'^mapmodelemdb/(?P<entryId>[0-9]+)/$', views.mapmodelemdb, name='webemdb_mapmodelemdb' ), # uses EMDB entry-Id
    
    # render the image as a projection jpeg, maximum-intensity (default), mean or sum. 
    url( r'^projection/(?P<imageId>[0-9]+)/$', views.projection, {"projkey": "intmax"}, name='webemdb_projection' ),
    url( r'^projection/(?P<imageId>[0-9]+)/mean/$', views.projection, {"projkey": "intmean"}, name='webemdb_meanprojection' ),
    url( r'^projection/(?P<imageId>[0-9]+)/max/$', views.projection, {"projkey": "intmax"}, name='webemdb_maxprojection' ),
    url( r'^projection/(?P<imageId>[0-9]+)/sum/$', views.projection, {"projkey": "intsum"}, name='webemdb_sumprojection' ),
    # sum projection of x,y,z axes. Uses numpy and EMAN2
    url( r'^projection_axis/(?P<imageId>[0-9]+)/(?P<axis>[xyz])/$', views.projection_axis, name='webemdb_projection_axis' ),
    url( r'^slice_axis/(?P<imageId>[0-9]+)/(?P<axis>[xyz])/$', views.projection_axis, {"get_slice": True}, name='webemdb_slice_axis' ),
    
    # browse by annotations. E.g. publications
    url( r'^publications/$', views.publications, name='webemdb_publications' ),
    url( r'^getEntriesByPub/(?P<publicationId>[0-9]+)/$', views.getEntriesByPub, name='webemdb_getEntriesByPub' ),
    url( r'^entries/$', views.entries, name='webemdb_entries' ),
    # auto-complete search - json methods
    url( r'^autocompleteQuery/$', views.autocompleteQuery, name='webemdb_autocompleteQuery' ), # returns list of ("1024", "Title")
    # full text search, using search service. Search term in 'get'
    url( r'^search/$', views.search, name='webemdb_search' ),
    
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
    url( r'^script_results/(?P<jobId>[0-9]+)/$', views.script_results, name='webemdb_script_results' ),
)
