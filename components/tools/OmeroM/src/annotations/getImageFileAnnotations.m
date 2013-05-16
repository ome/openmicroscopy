function fas = getImageFileAnnotations(session, ids, varargin)
% GETIMAGEFILEANNOTATIONS Retrieve file annotations linked to images
%
%    fas = getImageFileAnnotations(session, ids) returns all file
%    annotations linked to the image specified by the input identifiers ids
%    and owned by the session user.
%
%    fas = getImageFileAnnotations(session, images) returns all file
%    annotations linked to the input images and owned by the session user.
%
%    fas = getImageFileAnnotations(session,  ids, 'include', include) only
%    returns file annotations with the input namespace.
%
%    fas = getImageFileAnnotations(session,  ids, 'exclude', exclude)
%    excludes file annotations with the input namespace.
%
%    Examples:
%
%        fas = getImageFileAnnotations(session, ids)
%        fas = getImageFileAnnotations(session, images)
%        fas = getImageFileAnnotations(session, ids, 'include', include)
%        fas = getImageFileAnnotations(session, ids, 'exclude', exclude)
%
% See also: GETOBJECTANNOTATIONS, GETIMAGECOMMENTANNOTATIONS,
% GETIMAGETAGANNOTATIONS, GETIMAGEXMLANNOTATIONS

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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

fas = getObjectAnnotations(session, 'file', 'image', ids, varargin{:});