from django.conf.urls.defaults import *

from omeroweb.webgallery import views

urlpatterns = patterns('django.views.generic.simple',

    # index 'home page' of the webgallery app
    url( r'^$', views.index, name='webgallery_index' ),

    # group view
    url( r'show_group/(?P<groupId>[0-9]+)/$', views.show_group, name='webgallery_show_group' ),

    # project view
    url( r'show_project/(?P<projectId>[0-9]+)/$', views.show_project, name='webgallery_show_project' ),

    # dataset view
    url( r'show_dataset/(?P<datasetId>[0-9]+)/$', views.show_dataset, name='webgallery_show_dataset' ),
    # use the same dataset view, with a different template that only shows thumbnails
    url( r'dataset_thumbs/(?P<datasetId>[0-9]+)/$',
            views.show_dataset,
            {'template': 'webgallery/dataset_thumbs.html'},
            name='webgallery_dataset_thumbs' ),

    # image view
    url( r'show_image/(?P<imageId>[0-9]+)/$', views.show_image, name='webgallery_show_image' ),

)
