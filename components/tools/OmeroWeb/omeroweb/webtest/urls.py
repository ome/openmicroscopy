from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webtest import views

urlpatterns = patterns('django.views.generic.simple',

    url( r'^statictest/(?P<path>.*)$', serve, {'document_root': 'webtest/media'}, name="statictest"),

    url( r'^$', views.index, name='webtest_index' ),
    url( r'^login/$', views.login, name='webtest_login' ),
    url( r'^logout/$', views.logout, name='webtest_logout' ),
    url( r'^metadata/(?P<iid>[0-9]+)/$', views.metadata, name='webtest_metadata' ),
    url( r'^img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="image_viewer"),
    url( r'^viewport/$', 'direct_to_template', {'template': 'webtest/viewport.html'}, name="viewport" ),

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
    
    # post a comment annotation to images. parameters are in request: imageIds=123,234  comment=blah
    url( r'^add_annotations/$', views.add_annotations, name="webtest_add_annotations"),
)
