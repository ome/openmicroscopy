function das = getImageDoubleAnnotations(session, ids, varargin)
% GETIMAGEDOUBLEANNOTATIONS Retrieve double annotations linked to images
%
%    das = getImageDoubleAnnotations(session, ids) returns all double
%    annotations linked to the image specified by the input identifiers ids
%    and owned by the session user.
%
%    das = getImageDoubleAnnotations(session, images) returns all double
%    annotations linked to the input images and owned by the session user.
%
%    das = getImageDoubleAnnotations(session,  ids, 'include', include)
%    only returns double annotations with the input namespace.
%
%    das = getImageDoubleAnnotations(session,  ids, 'exclude', exclude)
%    excludes double annotations with the input namespace.
%
%    das = getImageDoubleAnnotations(session, ids, 'owner', ownerid)
%    returns the double annotations owned by the user specified by
%    ownerid. Use -1 to return the double annotations owned by all users.
%
%    Examples:
%
%        das = getImageDoubleAnnotations(session, ids)
%        das = getImageDoubleAnnotations(session, images)
%        das = getImageDoubleAnnotations(session, ids, 'include', include)
%        das = getImageDoubleAnnotations(session, ids, 'exclude', exclude)
%        das = getImageDoubleAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETIMAGECOMMENTANNOTATIONS,
% GETIMAGEFILEANNOTATIONS, GETIMAGELONGANNOTATIONS, GETIMAGETAGANNOTATIONS,
% GETIMAGETIMESTAMPANNOTATIONS, GETIMAGEXMLANNOTATIONS

% Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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

das = getObjectAnnotations(session, 'double', 'image', ids, varargin{:});