function cas = getImageCommentAnnotations(session, ids, varargin)
% GETIMAGECOMMENTANNOTATIONS Retrieve comment annotations linked to images
%
%    cas = getImageCommentAnnotations(session, ids) returns all comment
%    annotations linked to the image specified by the input identifiers ids
%    and owned by the session user.
%
%    cas = getImageCommentAnnotations(session, images) returns all comment
%    annotations linked to the input images and owned by the session user.
%
%    cas = getImageCommentAnnotations(session,  ids, 'include', include)
%    only returns comment annotations with the input namespace.
%
%    cas = getImageCommentAnnotations(session,  ids, 'exclude', exclude)
%    excludes comment annotations with the input namespace.
%
%    cas = getImageCommentAnnotations(session, ids, 'owner', ownerid)
%    returns the comment annotations owned by the user specified by
%    ownerid. Use -1 to return the comment annotations owned by all users.
%
%    Examples:
%
%        cas = getImageCommentAnnotations(session, ids)
%        cas = getImageCommentAnnotations(session, images)
%        cas = getImageCommentAnnotations(session, ids, 'include', include)
%        cas = getImageCommentAnnotations(session, ids, 'exclude', exclude)
%        cas = getImageCommentAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETIMAGEDOUBLEANNOTATIONS,
% GETIMAGEFILEANNOTATIONS, GETIMAGELONGANNOTATIONS, GETIMAGETAGANNOTATIONS,
% GETIMAGETIMESTAMPANNOTATIONS, GETIMAGEXMLANNOTATIONS

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

cas = getObjectAnnotations(session, 'comment', 'image', ids, varargin{:});