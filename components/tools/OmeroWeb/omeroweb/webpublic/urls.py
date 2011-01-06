from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webpublic import views

urlpatterns = patterns('django.views.generic.simple',
    url( r'^$', views.index, name='webpublic_index' ),
    url( r'^publicise$', views.publicise, name='webpublic_publicise' ),
    url( r'^de_publicise/(?P<id>\d+)$', views.de_publicise, name='webpublic_de_publicise' ),
    url( r'^user_listing$', views.user_listing, name='webpublic_user_listing' ),
    url( r'^(?P<base_62>[^/]+)$', views.tinyurl, name='webpublic_tinyurl' ),
    url( r'^appmedia/(?P<path>.*)$', serve, {'document_root': 'webpublic/media'}, name='webpublic_static' ),

)
