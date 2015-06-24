function tas = getImageTagAnnotations(session, ids, varargin)
% GETIMAGETAGANNOTATIONS Retrieve tag annotations linked to images
%
%    tas = getImageTagAnnotations(session, ids) returns all tag annotations
%    linked to the image specified by the input identifiers ids and owned 
%    by the session user.
%
%    tas = getImageTagAnnotations(session, images) returns all tag
%    annotations linked to the input images and owned by the session user.
%
%    tas = getImageTagAnnotations(session,  ids, 'include', include) only
%    returns tag annotations with the input namespace.
%
%    tas = getImageTagAnnotations(session,  ids, 'exclude', exclude)
%    excludes tag annotations with the input namespace.
%
%    tas = getImageTagAnnotations(session, ids, 'owner', ownerid)
%    returns the tag annotations owned by the user specified by ownerid.
%    Use -1 to return the tag annotations owned by all users.
%
%    Examples:
%
%        tas = getImageTagAnnotations(session, ids)
%        tas = getImageTagAnnotations(session, images)
%        tas = getImageTagAnnotations(session, ids, 'include', include)
%        tas = getImageTagAnnotations(session, ids, 'exclude', exclude)
%        tas = getImageTagAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETIMAGECOMMENTANNOTATIONS,
% GETIMAGEDOUBLEANNOTATIONS, GETIMAGEFILEANNOTATIONS,
% GETIMAGELONGANNOTATIONS, GETIMAGETIMESTAMPANNOTATIONS,
% GETIMAGEXMLANNOTATIONS

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

tas = getObjectAnnotations(session, 'tag', 'image', ids, varargin{:});