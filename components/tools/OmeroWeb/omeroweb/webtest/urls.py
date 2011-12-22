from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webtest import views

import os

urlpatterns = patterns('django.views.generic.simple',

    # tell django where to find media files for webtest. From 'here' they are in /media/
    url(r'^statictest/(?P<path>.*)$', serve, {'document_root': os.path.join(os.path.dirname(__file__), 'media')}, name="statictest"),

    # index 'home page' of the webtest app
    url( r'^$', views.index, name='webtest_index' ),

    # login and logout: preference is to use webclient functionality for this, but these can be used as examples if needed
    url( r'^login/$', views.login, name='webtest_login' ),
    url( r'^logout/$', views.logout, name='webtest_logout' ),

    # some of this functionality is duplicated from webclient or gateway as a stand-alone example
    url( r'^metadata/(?P<iid>[0-9]+)/$', views.metadata, name='webtest_metadata' ),
    url( r'^img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="image_viewer"),
    url( r'^viewport/$', 'direct_to_template', {'template': 'webtest/viewport.html'}, name="viewport" ),

    # 'Hello World' example from tutorial on http://trac.openmicroscopy.org.uk/ome/wiki/OmeroWeb
    url( r'^dataset/(?P<datasetId>[0-9]+)/$', views.dataset ),

    # big image examples
    url( r'^panojs/$', 'direct_to_template', {'template': 'webtest/bigimage/panojs.html'}, name="panojs" ),
    url( r'^kasuari/$', 'direct_to_template', {'template': 'webtest/bigimage/kasuari.html'}, name="kasuari" ),
    
    # roi examples, testing various roi libraries for displaying ROIs. 
    url( r'^roi_viewer/(?:(?P<roi_library>((?i)processing|jquery|raphael))/(?P<imageId>[0-9]+)/)?$', views.roi_viewer, name="webfigure_roi_viewer" ),
    
    # Displays images (one per row) one channel per column in a grid. Params are passed in request, E.g. imageIds
    url( r'^split_view_figure/$', views.split_view_figure, name="webtest_split_view_figure"),
    # View a dataset as two panels of images, each with a different rendering setting
    url( r'^dataset_split_view/(?P<datasetId>[0-9]+)/', views.dataset_split_view, name='webtest_dataset_split_view' ),
    
    # view an image in grid with the Z, C, T dimensions split over the x or y axes as chosen by user. 
    # Also displays SPIM data if available in the http://www.ome-xml.org/wiki/SPIM/InitialSupport format.
    url( r'^image_dimensions/(?P<imageId>[0-9]+)/', views.image_dimensions, name='webtest_image_dimensions' ),

    # overlay individual channels from the same image (or different images) and manipulate them separately..
    # translate, scale etc relative to one-another.
    url( r'^render_channel_overlay/', views.render_channel_overlay, name='webtest_render_channel_overlay' ),

    url( r'^channel_overlay_viewer/(?P<imageId>[0-9]+)/', views.channel_overlay_viewer, name='webtest_channel_overlay_viewer' ),

    # post a comment annotation to images. parameters are in request: imageIds=123,234  comment=blah 
    # ns=Namespace replace=true (replaces existing comment with same ns if found)
    url( r'^add_annotations/$', views.add_annotations, name="webtest_add_annotations"),
    
    # examples of using the 'common' templates
    url(r'^common/(?P<base_template>[a-z0-9_]+)/', views.common_templates, name='common'),
)
