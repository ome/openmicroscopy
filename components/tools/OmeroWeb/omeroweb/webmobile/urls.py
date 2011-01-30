from django.conf.urls.defaults import *
from django.views.static import serve
import os

from omeroweb.webmobile import views

urlpatterns = patterns('',
    url( r'^$', views.index, name='webmobile_index' ),
    url( r'^(?P<eid>[0-9]+)/$', views.index, name='webmobile_index' ),
    url( r'^login/$', views.login, name='webmobile_login' ),
    url( r'^logout/$', views.logout, name='webmobile_logout' ),
    url( r'^img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="image_viewer"),
    
    # recent updates
    # group
    url( r'^recent/(?P<obj_type>((?i)all|images|anns|rois))/$', views.recent, name='webmobile_recent' ),
    url( r'^collab_my_data/$', views.collab_annotations, name='webmobile_collab_my_data' ),
    url( r'^collab_my_anns/$', views.collab_annotations, {'myData':False}, name='webmobile_collab_my_anns' ),
    #experimenter
    url( r'^recent/(?P<obj_type>((?i)all|images|anns|rois))/(?P<eid>[0-9]+)/$', views.recent, name='webmobile_recent' ), 
    
    # browsing P/D/I hierarchy
    url( r'^projects/$', views.projects, name='webmobile_projects' ),
    url( r'^projects/(?P<eid>[0-9]+)/$', views.projects, name='webmobile_projects' ),
    url( r'^project/(?P<id>[0-9]+)/$', views.project, name='webmobile_project' ),
    url( r'^project_details/(?P<id>[0-9]+)/$', views.object_details, {"obj_type": "project"}, name='webmobile_project_details' ),
    url( r'^dataset/(?P<id>[0-9]+)/$', views.dataset, name='webmobile_dataset' ),
    url( r'^dataset_details/(?P<id>[0-9]+)/$', views.object_details, {"obj_type": "dataset"}, name='webmobile_dataset_details' ),
    url( r'^image/(?P<imageId>[0-9]+)/$', views.image, name='webmobile_image' ),
    url( r'^viewer/(?P<imageId>[0-9]+)/$', views.viewer, name='webmobile_viewer' ),     # 'full' viewer
    url( r'^orphaned_images/(?P<eid>[0-9]+)/$', views.orphaned_images, name='webmobile_orphaned_images' ),
    
    # browsing S/P/W hierarchy
    url( r'^screens/$', views.screens, name='webmobile_screens' ),
    url( r'^screens/(?P<eid>[0-9]+)/$', views.screens, name='webmobile_screens' ),
    url( r'^plate/(?P<id>[0-9]+)/$', views.plate, name='webmobile_plate' ),
    
    url( r'^groups_members/$', views.groups_members, name='webmobile_groups_members' ),
    # switch group, then redirect to index page
    url( r'^switch_group/(?P<groupId>[0-9]+)/$', views.switch_group, name='webmobile_switch_group' ),
    
    # add comment to 'project', 'dataset' or 'image', then redirect to object page
    url( r'^add_comment/(?P<obj_type>[a-z]+)/(?P<obj_id>[0-9]+)/$', views.add_comment, name='webmobile_add_comment' ),
    # edit name & description of 'project', 'dataset' or 'image', then redirect to object page
    url( r'^edit_object/(?P<obj_type>[a-z]+)/(?P<obj_id>[0-9]+)/$', views.edit_object, name='webmobile_edit_object' ),
    
    url(r'^appmedia/(?P<path>.*)$', 'django.views.static.serve', {'document_root': 'webmobile/media'}, name="mobile_static"),
)

