from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webfigure import views

urlpatterns = patterns('django.views.generic.simple',
    url( r'^$', views.index, name='webfigure_index' ),
    url( r'^login/$', views.login, name='webfigure_login' ),
    url( r'^logout/$', views.logout, name='webfigure_logout' ),
    
    # all these pass arguments in request
    url( r'^split_view_figure/$', views.split_view_figure, name="webfigure_split_view_figure"),
    
    # view a dataset as two panels of images, each with a different rendering setting
    url( r'^dataset_split_view/(?P<datasetId>[0-9]+)/', views.dataset_split_view, name='webfigure_dataset_split_view' ),
    
    # post a comment annotation to images. imageIds=123,234 comment=blah
    url( r'^add_annotations/$', views.add_annotations, name="webfigure_add_annotations"),
    
    # define the sym link for media. 
    #url( r'appmedia/webfigure/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media', 'webfigure').replace('\\','/') }, name="webfigure_media" ),
    
)