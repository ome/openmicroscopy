#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

from django.conf.urls import url, patterns

from omeroweb.webclient import views
from omeroweb.webgateway import views as webgateway
from omeroweb.webclient.webclient_gateway import defaultThumbnail

urlpatterns = patterns(
    'django.views.generic.simple',

    # Home page is the main 'Data' page
    url(r'^$', views.load_template, {'menu': 'userdata'}, name="webindex"),

    # 'Feed' / 'recent'
    url(r'^feed/$', views.feed, name="web_feed"),

    # render main template
    url(r'^(?P<menu>((?i)userdata|public|history|search|help|usertags))/$',
        views.load_template,
        name="load_template"),
    url(r'^userdata/$',
        views.load_template, {'menu': 'userdata'},
        name="userdata"),
    url(r'^history/$',
        views.load_template, {'menu': 'history'},
        name="history"),

    url(r'^last_imports/$',
        views.index_last_imports,
        name="index_last_imports"),
    url(r'^most_recent/$', views.index_most_recent, name="index_most_recent"),
    url(r'^tag_cloud/$', views.index_tag_cloud, name="index_tag_cloud"),

    url(r'^login/$', views.login, name="weblogin"),
    url(r'^logout/$', views.logout, name="weblogout"),
    url(r'^active_group/$',
        views.change_active_group,
        name="change_active_group"),

    # The content of group/users drop-down menu
    url(r'^group_user_content/$',
        views.group_user_content,
        name="group_user_content"),

    # update, display activities, E.g. delete queues, scripts etc.
    url(r'^activities/', views.activities, name="activities"),
    url(r'^activities_json/',
        views.activities,
        {'template': 'json'},
        name="activities_json"),
    url(r'^activities_update/(?:(?P<action>clean)/)?$',
        views.activities_update,
        name="activities_update"),

    # loading data
    url(r'^load_data/(?:(?P<o1_type>'
        r'((?i)project|dataset|image|screen|plate|well|orphaned))/)'
        r'?(?:(?P<o1_id>[0-9]+)/)'
        r'?(?:(?P<o2_type>((?i)dataset|image|plate|acquisition|well))/)'
        r'?(?:(?P<o2_id>[0-9]+)/)'
        r'?(?:(?P<o3_type>((?i)image|well))/)'
        r'?(?:(?P<o3_id>[0-9]+)/)?$',
        views.load_data,
        name="load_data"),

    # chgrp. Load potential target groups, then load target P/D within chosen
    # group
    url(r'^load_chgrp_groups/$',
        views.load_chgrp_groups,
        name="load_chgrp_groups"),  # Query E.g. ?Image=1,2&Dataset=3
    url(r'^load_chgrp_target/(?P<group_id>[0-9]+)/'
        r'(?P<target_type>((?i)project|dataset|screen))/$',
        views.load_chgrp_target,
        name="load_chgrp_target"),

    # load history
    url(r'^load_calendar/(?:(\d{4})/(\d{1,2})/)?$', views.load_calendar,
        name="load_calendar"),
    url(r'^load_history/(?:(\d{4})/(\d{1,2})/(\d{1,2})/)?$',
        views.load_history, name="load_history"),

    # load search
    url(r'^load_searching/(?:(?P<form>((?i)form))/)?$', views.load_searching,
        name="load_searching"),

    # load public
    url(r'^load_public/(?:(?P<share_id>[0-9]+)/)?$', views.load_public,
        name="load_public"),

    # metadata
    url(r'^metadata_details/(?:(?P<c_type>[a-zA-Z]+)/'
        r'(?P<c_id>[0-9]+)/)?(?:(?P<share_id>[0-9]+)/)?$',
        views.load_metadata_details,
        name="load_metadata_details"),
    url(r'^metadata_acquisition/(?P<c_type>[a-zA-Z]+)/'
        r'(?P<c_id>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$',
        views.load_metadata_acquisition,
        name="load_metadata_acquisition"),
    url(r'^metadata_preview/(?P<c_type>((?i)image|well))/'
        r'(?P<c_id>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$',
        views.load_metadata_preview,
        name="load_metadata_preview"),
    url(r'^metadata_hierarchy/(?P<c_type>[a-zA-Z]+)/'
        r'(?P<c_id>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$',
        views.load_metadata_hierarchy,
        name="load_metadata_hierarchy"),

    url(r'^render_thumbnail/(?P<iid>[0-9]+)/'
        r'(?:(?P<share_id>[0-9]+)/)?$',
        webgateway.render_thumbnail,
        {'w': 80, '_defcb': defaultThumbnail},
        name="render_thumbnail"),
    url(r'^render_thumbnail/size/(?P<w>[0-9]+)/'
        r'(?P<iid>[0-9]+)/(?:(?P<share_id>[0-9]+)/)?$',
        webgateway.render_thumbnail,
        {'_defcb': defaultThumbnail},
        name="render_thumbnail_resize"),
    url(r'^edit_channel_names/(?P<imageId>[0-9]+)/$',
        views.edit_channel_names,
        name="edit_channel_names"),

    # image webgateway extention
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_image_region/'
        r'(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$',
        webgateway.render_image_region,
        name="web_render_image_region"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_birds_eye_view/'
        r'(?P<iid>[^/]+)/(?:(?P<size>[^/]+)/)?$',
        webgateway.render_birds_eye_view,
        name="web_render_birds_eye_view"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_image/(?P<iid>[^/]+)/'
        r'(?:(?P<z>[^/]+)/)?(?:(?P<t>[^/]+)/)?$',
        webgateway.render_image,
        name="web_render_image"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_image_download/'
        r'(?P<iid>[^/]+)/(?:(?P<z>[^/]+)/)?(?:(?P<t>[^/]+)/)?$',
        webgateway.render_image,
        {'download': True},
        name="web_render_image_download"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?img_detail/(?P<iid>[0-9]+)/$',
        views.image_viewer,
        name="web_image_viewer"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?imgData/(?P<iid>[0-9]+)/$',
        webgateway.imageData_json,
        name="web_imageData_json"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_row_plot/(?P<iid>[^/]+)/'
        r'(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<y>[^/]+)/(?:(?P<w>[^/]+)/)?$',
        webgateway.render_row_plot,
        name="web_render_row_plot"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_col_plot/(?P<iid>[^/]+)/'
        r'(?P<z>[^/]+)/(?P<t>[^/]+)/(?P<x>[^/]+)/(?:(?P<w>[^/]+)/)?$',
        webgateway.render_col_plot,
        name="web_render_col_plot"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?render_split_channel/'
        r'(?P<iid>[^/]+)/(?P<z>[^/]+)/(?P<t>[^/]+)/$',
        webgateway.render_split_channel,
        name="web_render_split_channel"),
    url(r'^saveImgRDef/(?P<iid>[^/]+)/$',
        webgateway.save_image_rdef_json,
        name="web_save_image_rdef_json"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?getImgRDef/$',
        webgateway.get_image_rdef_json,
        name="web_get_image_rdef_json"),
    url(r'^(?:(?P<share_id>[0-9]+)/)?copyImgRDef/$',
        webgateway.copy_image_rdef_json,
        name="copy_image_rdef_json"),


    # Fileset query (for delete or chgrp dialogs) obj-types and ids in REQUEST
    # data
    url(r'^fileset_check/(?P<action>((?i)delete|chgrp))/$',
        views.fileset_check,
        name="fileset_check"),

    # Popup for downloading original archived files for images
    url(r'^download_placeholder/$', views.download_placeholder,
        name="download_placeholder"),

    # chgrp - 'group_id', obj-types and ids in POST data
    url(r'^chgrp/$', views.chgrp, name="chgrp"),

    # annotations
    url(r'^action/(?P<action>[a-zA-Z]+)/(?:(?P<o_type>[a-zA-Z]+)/)'
        r'?(?:(?P<o_id>[0-9]+)/)?$',
        views.manage_action_containers,
        name="manage_action_containers"),
    url(r'^batch_annotate/$', views.batch_annotate, name="batch_annotate"),
    url(r'^annotate_tags/$', views.annotate_tags, name="annotate_tags"),
    url(r'^annotate_rating/$',
        views.annotate_rating,
        name="annotate_rating"),
    url(r'^annotate_comment/$',
        views.annotate_comment,
        name="annotate_comment"),
    url(r'^annotate_file/$', views.annotate_file, name="annotate_file"),
    url(r'^annotate_map/$', views.annotate_map, name="annotate_map"),
    url(r'^annotation/(?P<annId>[0-9]+)/$',
        views.download_annotation,
        name="download_annotation"),
    url(r'^load_original_metadata/(?P<imageId>[0-9]+)/'
        r'(?:(?P<share_id>[0-9]+)/)?$',
        views.load_original_metadata,
        name="load_original_metadata"),
    url(r'^download_orig_metadata/(?P<imageId>[0-9]+)/$',
        views.download_orig_metadata,
        name="download_orig_metadata"),

    url(r'^load_tags/(?:(?P<o_type>((?i)tag|dataset))/(?P<o_id>[0-9]+)/)?$',
        views.load_data_by_tag,
        name="load_data_by_tag"),

    # Open Astex Viewer will try to show file as volume, e.g. mrc.map file.
    url(r'^open_astex_viewer/(?P<obj_type>((?i)image|image_8bit|file))/'
        r'(?P<obj_id>[0-9]+)/$',
        views.open_astex_viewer,
        name='open_astex_viewer'),  # 'data_url' to load in REQUEST
    url(r'^file/(?P<annId>[0-9]+)\.map$',
        views.download_annotation,
        name='open_astex_map'),  # download file
    url(r'^file/(?P<annId>[0-9]+)\.bit$',
        views.download_annotation,
        name='open_astex_bit'),  # download file
    url(r'^image_as_map/(?P<imageId>[0-9]+)\.map$',
        views.image_as_map,
        name='webclient_image_as_map'),  # convert image to map (full size)
    url(r'^image_as_map/(?P<imageId>[0-9]+)/(?P<maxSize>[0-9]+)\.map$',
        views.image_as_map,
        name='webclient_image_as_map'),  # image to map of max Size (side
                                         # length)
    url(r'^image_as_map/8bit/(?P<imageId>[0-9]+)\.map$',
        views.image_as_map,
        {'8bit': True},
        name='webclient_image_as_map_8bit'),  # convert image to map
    url(r'^image_as_map/8bit/(?P<imageId>[0-9]+)/(?P<maxSize>[0-9]+)\.map$',
        views.image_as_map,
        {'8bit': True},
        name='webclient_image_as_map_8bit'),  # image to map

    url(r'^avatar/(?P<oid>[0-9]+)/$', views.avatar, name="avatar"),


    # scripting service urls
    url(r'^list_scripts/$',
        views.list_scripts,
        name="list_scripts"),  # returns html list of scripts - click to run
    url(r'^script_ui/(?P<scriptId>[0-9]+)/$',
        views.script_ui,
        name='script_ui'),  # shows a form for running a script
    url(r'^script_run/(?P<scriptId>[0-9]+)/$',
        views.script_run,
        name='script_run'),  # runs the script - parameters in POST
    url(r'^get_original_file/(?:(?P<fileId>[0-9]+)/)?$',
        views.get_original_file,
        name="get_original_file"),  # for stderr, stdout etc
    url(r'^download_original_file/(?:(?P<fileId>[0-9]+)/)?$',
        views.get_original_file,
        {'download': True},
        name="download_original_file"),  # for stderr, stdout etc
    url(r'^figure_script/(?P<scriptName>'
        r'((?i)SplitView|RoiSplit|Thumbnail|MakeMovie))/$',
        views.figure_script,
        name='figure_script'),  # shows a form for running a script

    # ome_tiff_script: generate OME-TIFF and attach to image (use script
    # service). Must be POST
    url(r'^ome_tiff_script/(?P<imageId>[0-9]+)/$',
        views.ome_tiff_script,
        name='ome_tiff_script'),
    url(r'^ome_tiff_info/(?P<imageId>[0-9]+)/$',
        views.ome_tiff_info,
        name='ome_tiff_info'),

    # ping OMERO server to keep session alive
    url(r'^keepalive_ping/$', views.keepalive_ping, name="keepalive_ping"),

)
