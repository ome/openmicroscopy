function tas = getImageTimestampAnnotations(session, ids, varargin)
% GETIMAGETIMESTAMPANNOTATIONS Retrieve timestamp annotations linked to images
%
%    tas = getImageTimestampAnnotations(session, ids) returns all timestamp
%    annotations linked to the images specified by the input identifiers
%    ids and owned by the session user.
%
%    tas = getImageTimestampAnnotations(session, images) returns all 
%    timestamp annotations linked to the input images and owned by the 
%    session user.
%
%    tas = getImageTimestampAnnotations(session,  ids, 'include', include) 
%    only returns timestamp annotations with the input namespace.
%
%    tas = getImageTimestampAnnotations(session,  ids, 'exclude', exclude)
%    excludes timestamp annotations with the input namespace.
%
%    tas = getImageTimestampAnnotations(session, ids, 'owner', ownerid)
%    returns the timestamp annotations owned by the user specified by ownerid.
%    Use -1 to return the tag annotations owned by all users.
%
%    Examples:
%
%        tas = getImageTimestampAnnotations(session, ids)
%        tas = getImageTimestampAnnotations(session, images)
%        tas = getImageTimestampAnnotations(session, ids, 'include', include)
%        tas = getImageTimestampAnnotations(session, ids, 'exclude', exclude)
%        tas = getImageTimestampAnnotations(session, ids, 'owner', -1)
%
% See also: GETIMAGECOMMENTANNOTATIONS, GETIMAGEDOUBLEANNOTATIONS,
% GETIMAGEFILEANNOTATIONS, GETIMAGELONGANNOTATIONS, GETIMAGETAGANNOTATIONS, 
% GETIMAGEXMLANNOTATIONS, GETOBJECTANNOTATIONS

% Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

tas = getObjectAnnotations(session, 'timestamp', 'image', ids, varargin{:});