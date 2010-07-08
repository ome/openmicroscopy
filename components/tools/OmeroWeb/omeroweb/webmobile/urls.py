from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webmobile import views

urlpatterns = patterns('',
    url( r'^$', views.index, name='webmobile_index' ),
    url( r'^login/$', views.login, name='webmobile_login' ),
    url( r'^logout/$', views.logout, name='webmobile_logout' ),
    url( r'^img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="image_viewer"),
    
    url( r'^dataset/(?P<id>[0-9]+)/$', views.dataset, name='webmobile_dataset' ),
    
)

