function images = getImages(session, varargin)
% GETIMAGES Retrieve image objects from the OMERO server
%
%   images = getImages(session) returns all the images owned by the session
%   user in the context of the session group.
%
%   images = getImages(session, ids) returns all the images identified by
%   the input ids owned by the session user in the context of the session
%   group.
%
%   Examples:
%
%      images = getImages(session);
%      images = getImages(session, ids);
%
% See also: GETOBJECTS, GETPROJECTS, GETDATASETS, GETPLATES

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

% Input check
ip = inputParser;
ip.addOptional('ids', [], @isvector);
ip.parse(varargin{:});

images = getObjects(session, 'image', ip.Results.ids);