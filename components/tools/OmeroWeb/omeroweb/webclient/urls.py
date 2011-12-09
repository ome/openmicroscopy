#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008-2011 University of Dundee.
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

import os.path

from django.conf.urls.defaults import *
from django.views.static import serve

from omeroweb.webclient import views

urlpatterns = patterns('django.views.generic.simple',
    
    url( r'^$', views.index, name="webindex" ),
    # render main template
    url( r'^(?P<menu>((?i)userdata|public|history|search|help|usertags))/$', views.load_template, name="load_template" ),

    url( r'^context/$', views.index_context, name="index_context" ),
    url( r'^last_imports/$', views.index_last_imports, name="index_last_imports" ),
    url( r'^most_recent/$', views.index_most_recent, name="index_most_recent" ),
    url( r'^tag_cloud/$', views.index_tag_cloud, name="index_tag_cloud" ),
    
    url( r'^login/$', views.login, name="weblogin" ),
    url( r'^logout/$', views.logout, name="weblogout" ),
    url( r'^active_group/$', views.change_active_group, name="change_active_group" ),
    
    # load basket
    url( r'^basket/empty/$', views.empty_basket, name="empty_basket"),
    url( r'^basket/update/$', views.update_basket, name="update_basket"),
    url( r'^basket/(?:(?P<action>[a-zA-Z]+)/)?$', views.basket_action, name="basket_action"),
    
    # update, display processes, E.g. delete queues, scripts etc.
    url( r'^progress/', views.progress, name="progress"),
    url( r'^status/(?:(?P<action>[a-zA-Z]+)/)?$', views.status_action, name="status"),
    
    # loading data    
    url( r'^load_data/(?:(?P<o1_type>((?i)project|dataset|image|screen|plate|well|orphaned))/)?(?:(?P<o1_id>[0-9]+)/)?(?:(?P<o2_type>((?i)dataset|image|plate|acquisition|well))/)?(?:(?P<o2_id>[0-9]+)/)?(?:(?P<o3_type>((?i)image|well))/)?(?:(?P<o3_id>[0-9]+)/)?$', views.load_data, name="load_data" ),    
    
    # load history
    url( r'^load_calendar/(?:(\d{4})/(\d{1,2})/)?$', views.load_calendar, name="load_calendar"),
    url( r'^load_history/(?:(\d{4})/(\d{1,2})/(\d{1,2})/)?$', views.load_history, name="load_history"),
    
    # load search
    url( r'^load_searching/(?:(?P<form>((?i)form))/)?$', views.load_searching, name="load_searching"),
    
    # load public
    url( r'^load_public/(?:(?P<share_id>[0-9]+)/)?$', views.load_public, name="load_public"),
    
    # metadata
    url( r'^metadata_details/(?:(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/)?(?:(?P<share_id>[0-9]+)/)?$', views.load_metadata_details, name="load_metadata_details" ),
    url( r'^metadata_acquisition/(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.load_metadata_acquisition, name="load_metadata_acquisition" ),
    url( r'^metadata_preview/(?P<imageId>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.load_metadata_preview, name="load_metadata_preview" ),
    url( r'^metadata_hierarchy/(?P<c_type>[a-zA-Z]+)/(?P<c_id>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.load_metadata_hierarchy, name="load_metadata_hierarchy" ),
    url( r'^metadata_details/multiaction/(?:(?P<action>[a-zA-Z]+)/)?$', views.manage_annotation_multi, name="manage_annotation_multi" ),
    
    url( r'^action/(?P<action>[a-zA-Z]+)/(?:(?P<o_type>[a-zA-Z]+)/)?(?:(?P<o_id>[0-9]+)/)?$', views.manage_action_containers, name="manage_action_containers" ),
    url( r'^annotation/(?P<action>[a-zA-Z]+)/(?P<iid>[0-9]+)/$', views.download_annotation, name="download_annotation" ),
    url( r'^archived_files/download/(?P<iid>[0-9]+)/$', views.archived_files, name="archived_files" ),
    
    url( r'^load_tags/(?:(?P<o_type>((?i)tag|dataset))/(?P<o_id>[0-9]+)/)?$', views.load_data_by_tag, name="load_data_by_tag" ),
    url( r'^autocompletetags/$', views.autocomplete_tags, name="autocomplete_tags" ),
    
    # Open Astex Viewer will try to show file as volume, e.g. mrc.map file. 
    url( r'^open_astex_viewer/(?P<obj_type>((?i)image|image_8bit|file))/(?P<obj_id>[0-9]+)/$', views.open_astex_viewer, name='open_astex_viewer' ),  # 'data_url' to load in REQUEST
    url( r'^file/(?P<iid>[0-9]+)\.map$', views.download_annotation, {'action':'download'}, name='open_astex_map' ),# download file
    url( r'^file/(?P<iid>[0-9]+)\.bit$', views.download_annotation, {'action':'download'}, name='open_astex_bit' ),# download file
    url( r'^image_as_map/(?P<imageId>[0-9]+)\.map$', views.image_as_map, name='webclient_image_as_map' ), # convert image to map (full size)
    url( r'^image_as_map/(?P<imageId>[0-9]+)/(?P<maxSize>[0-9]+)\.map$', views.image_as_map, name='webclient_image_as_map' ), # image to map of max Size (side length)
    url( r'^image_as_map/8bit/(?P<imageId>[0-9]+)\.map$', views.image_as_map, {'8bit':True}, name='webclient_image_as_map_8bit' ), # convert image to map
    url( r'^image_as_map/8bit/(?P<imageId>[0-9]+)/(?P<maxSize>[0-9]+)\.map$', views.image_as_map, {'8bit':True}, name='webclient_image_as_map_8bit' ), # image to map
    
    # render bird's eye view
    url( r'^render_birds_eye_view/(?P<iid>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.render_birds_eye_view, name="render_birds_eye_view" ),
    
    # render thumbnails
    url( r'^render_thumbnail/(?P<iid>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.render_thumbnail, name="render_thumbnail" ),
    url( r'^render_thumbnail/size/(?P<size>[0-9]+)/(?P<iid>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$', views.render_thumbnail_resize, name="render_thumbnail_resize" ),
    
    #plate webgateway extention
    url(r'^plate/(?P<pid>[^/]+)/(?:(?P<field>[^/]+)/)?$', views.plateGrid_json, name="web_plategrid_json"),
    
    #image webgateway extention
    url( r'^(?:(?P<share_id>[0-9]+)/)?render_image_region/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$', views.render_image_region, name="web_render_image_region"),
    url( r'^(?:(?P<share_id>[0-9]+)/)?render_image/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$', views.render_image, name="web_render_image"),
    url( r'^(?:(?P<share_id>[0-9]+)/)?img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="web_image_viewer"),
    
    url( r'^(?:(?P<share_id>[0-9]+)/)?img_detail/(?P<iid>[0-9]+)/$', views.image_viewer, name="web_image_viewer"),
    url( r'^(?:(?P<share_id>[0-9]+)/)?imgData/(?P<iid>[0-9]+)/$', views.imageData_json, name="web_imageData_json"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_row_plot/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<y>[^/]+)/(?:(?P<w>[^/]+)/)?$', views.render_row_plot, name="web_render_row_plot"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_col_plot/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<x>[^/]+)/(?:(?P<w>[^/]+)/)?$', views.render_col_plot, name="web_render_col_plot"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_split_channel/(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$', views.render_split_channel, name="web_render_split_channel"),
    
    url( r'^help_search/$', 'direct_to_template', {'template': 'webclient/help/help_search.html'}, name="help_search" ),
    
    url( r'^avatar/(?P<oid>[0-9]+)/$', views.avatar, name="avatar"),
    
    url( r'^spellchecker/$', views.spellchecker, name="spellchecker"), 
    
    # scripting service urls
    url( r'^list_scripts/$', views.list_scripts, name="list_scripts"),  # returns html list of scripts - click to run
    url( r'^script_ui/(?P<scriptId>[0-9]+)/$', views.script_ui, name='script_ui' ), # shows a form for running a script
    url( r'^script_run/(?P<scriptId>[0-9]+)/$', views.script_run, name='script_run' ),  # runs the script - parameters in POST
    url( r'^get_original_file/(?:(?P<fileId>[0-9]+)/)?$', views.get_original_file, name="get_original_file"), # for stderr, stdout etc

)
