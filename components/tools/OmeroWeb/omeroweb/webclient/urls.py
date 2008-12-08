#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
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
#from omeroweb.webclient.feeds import RssShareFeed, AtomShareFeed

'''feeds = {
    'rss': RssShareFeed,
    'atom': AtomShareFeed,
}'''

urlpatterns = patterns('',
    
    ( r'^$', views.index ),
    ( r'^context/$', views.index_context ),
    ( r'^my_data/$', views.index_my_data ),
    ( r'^last_imports/$', views.index_last_imports ),
    ( r'^most_recent/$', views.index_most_recent ),
    
    ( r'^login/$', views.login ),
    ( r'^logout/$', views.logout ),
    ( r'^active_group/$', views.change_active_group ),
    
    ( r'^mydata/$', views.manage_my_data ),
    ( r'^mydata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/$', views.manage_my_data ),
    ( r'^mydata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-z]+)/(?P<o2_id>[0-9]+)/$', views.manage_my_data ),
    ( r'^mydata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-z]+)/(?P<o3_id>[0-9]+)/$', views.manage_my_data ),
    ( r'^userdata/$', views.manage_user_containers ),
    ( r'^userdata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/$', views.manage_user_containers ),
    ( r'^userdata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-z]+)/(?P<o2_id>[0-9]+)/$', views.manage_user_containers ),
    ( r'^userdata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-z]+)/(?P<o3_id>[0-9]+)/$', views.manage_user_containers ),
    ( r'^groupdata/$', views.manage_group_containers ),
    ( r'^groupdata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/$', views.manage_group_containers ),
    ( r'^groupdata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-z]+)/(?P<o2_id>[0-9]+)/$', views.manage_group_containers ),
    ( r'^groupdata/(?P<o1_type>[a-z]+)/(?P<o1_id>[0-9]+)/(?P<o2_type>[a-z]+)/(?P<o2_id>[0-9]+)/(?P<o3_type>[a-z]+)/(?P<o3_id>[0-9]+)/$', views.manage_group_containers ),
    
    ( r'^hierarchy/$', views.manage_container_hierarchy ),
    ( r'^hierarchy/(?P<o_type>[a-z]+)/(?P<o_id>[0-9]+)/$', views.manage_container_hierarchy ),
    ( r'^tree_details/(?P<c_type>[a-z]+)/(?P<c_id>[0-9]+)/$', views.manage_tree_details ),
    
    ( r'^image_zoom/(?P<iid>[0-9]+)/$', views.manage_image_zoom ),
    
    ( r'^action/([a-z]+)/$', views.manage_action_containers ),
    ( r'^action/([a-z]+)/([a-z]+)/([0-9]+)/$', views.manage_action_containers ),    
    
    ( r'^annotations/([a-z]+)/([0-9]+)/$', views.manage_annotations ),
    ( r'^annotation/([a-z]+)/([0-9]+)/$', views.manage_annotation ),
    ( r'^render_thumbnail/(?P<iid>[0-9]+)/$', views.render_thumbnail ),
    ( r'^render_thumbnail/details/(?P<iid>[0-9]+)/$', views.render_thumbnail_details ),
    ( r'^render_thumbnail/(?P<size>[0-9]+)/(?P<iid>[0-9]+)/$', views.render_thumbnail_resize ),
    ( r'^render_thumbnail/big/(?P<iid>[0-9]+)/$', views.render_big_thumbnail ),
    ( r'^share/$', views.manage_shares ),
    ( r'^share/([a-z]+)/$', views.manage_share ),
    ( r'^share/(?P<action>[a-z]+)/(?P<oid>[0-9]+)/$', views.manage_share ),
    ( r'^shared/([a-z]+)/([0-9]+)/$', views.manage_shared ),
    ( r'^share_content/([0-9]+)/$', views.load_share_content ),
    
    ( r'^empty/$', views.empty_basket ),
    ( r'^basket/$', views.basket_action ),
    ( r'^basket/update/$', views.update_basket ),
    ( r'^basket/([a-z]+)/$', views.basket_action ),
    ( r'^basket/(?P<action>[a-z]+)/(?P<oid>[0-9]+)/$', views.basket_action ),
    
    ( r'^search/$', views.search ),
    ( r'^history/(.*)/(\d{4})/(\d{1,2})/$', views.history ),
    ( r'^history/(.*)/(\d{4})/(\d{1,2})/(\d{1,2})/$', views.history_details ),
    ( r'^impexp/(.*)/$', views.impexp ),
    ( r'^myaccount/(.*)/$', views.myaccount ),
    ( r'^help/(.*)/$', views.help ),
    ( r'^static/(?P<path>.*)$', serve ,{ 'document_root': os.path.join(os.path.dirname(__file__), 'media').replace('\\','/') } ),

    (r'^render_image/(?P<iid>[0-9]+)/(?P<z>[0-9]+)/(?P<t>[0-9]+)/$', views.render_image),
    (r'^img_detail/(?P<iid>[0-9]+)/(?:(?P<dsid>[0-9]+)/)?$', views.image_viewer),
    (r'^imgData/(?P<iid>[0-9]+)/$', views.imageData_json),

    (r'^spellchecker/$', views.spellchecker), 
    
    #(r'^feeds/(?P<url>.*)/$', 'django.contrib.syndication.views.feed',{'feed_dict': feeds}),
    
    #( r'^image/(?P<image_id>[0-9]+)/$', views.manage_image ),
    
    #( r'(?P<c_type>[a-z]+)/(?P<c_id>[0-9]+)/$', views.manage_container_data ),
    #( r'(?P<c_type>[a-z]+)/(?P<c_id>[0-9]+)/$', views.manage_container_data ),
    
    

)
