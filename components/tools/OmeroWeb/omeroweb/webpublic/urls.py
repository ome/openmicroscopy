#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
#                    All rights reserved.
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
