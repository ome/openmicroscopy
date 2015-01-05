function xas = getImageXmlAnnotations(session, ids, varargin)
% GETIMAGEXMLANNOTATIONS Retrieve xml annotations linked to images
%
%    xas = getImageXmlAnnotations(session, ids) returns all xml
%    annotations linked to the image specified by the input identifiers ids
%    and owned by the session user.
%
%    xas = getImageXmlAnnotations(session, images) returns all xml
%    annotations linked to the input images and owned by the session user.
%
%    xas = getImageXmlAnnotations(session,  ids, 'include', include) only
%    returns xml annotations with the input namespace.
%
%    xas = getImageXmlAnnotations(session,  ids, 'exclude', exclude)
%    excludes xml annotations with the input namespace.
%
%    xas = getImageXmlAnnotations(session, ids, 'owner', ownerid)
%    returns the xml annotations owned by the user specified by ownerid.
%    Use -1 to return the xml annotations owned by all users.
%
%    Examples:
%
%        xas = getImageXmlAnnotations(session, ids)
%        xas = getImageXmlAnnotations(session, images)
%        xas = getImageXmlAnnotations(session, ids, 'include', include)
%        xas = getImageXmlAnnotations(session, ids, 'exclude', exclude)
%        xas = getImageXmlAnnotations(session, ids, 'owner', -1)
%
% See also: GETIMAGECOMMENTANNOTATIONS, GETIMAGEDOUBLEANNOTATIONS,
% GETIMAGEFILEANNOTATIONS, GETIMAGELONGANNOTATIONS, GETIMAGETAGANNOTATIONS, 
% GETIMAGETIMESTAMPANNOTATIONS, GETOBJECTANNOTATIONS, 

% Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

xas = getObjectAnnotations(session, 'xml', 'image', ids, varargin{:});