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
    
)
