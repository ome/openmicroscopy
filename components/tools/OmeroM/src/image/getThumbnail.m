function thumbnail = getThumbnail(session, image, varargin)
% GETTHUMBNAIL Retrieve thumbnail from an image on the OMERO server
%
%   thumbnail = getThumbnail(session, image) returns the thumbnail from the
%   input image.
%
%   thumbnail = getThumbnail(session, image, x, y) also returns a thumbnail
%   of dimensions (x, y). 
%
%   thumbnail = getThumbnail(session, imageID) returns the thumbnail from
%   the input image identifier.
%
%   thumbnail = getThumbnail(session, imageID, x, y) also returns a
%   thumbnail of dimensions (x, y). 
%
%   Examples:
%
%      thumbnail = getThumbnail(session, image);
%      thumbnail = getThumbnail(session, image, x, y);
%      thumbnail = getThumbnail(session, imageID);
%      thumbnail = getThumbnail(session, imageID, x, y);
%
% See also: GETTHUMBNAILSTORE

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
isposint = @(x) isscalar(x) && x > 0 && round(x) == x;
ip = inputParser();
ip.addOptional('x', [], isposint);
ip.addOptional('y', [], isposint);
ip.parse(varargin{:});

% Initialize raw pixels store
store = getThumbnailStore(session, image);

% Retrieve thumbnail set
if ~isempty(ip.Results.x), x = rint(ip.Results.x); else x = []; end
if ~isempty(ip.Results.y), y = rint(ip.Results.x); else y = []; end
byteArray = store.getThumbnail(x, y);
store.close();

% Convert byteArray into Matlab image
stream = java.io.ByteArrayInputStream(byteArray);
image = javax.imageio.ImageIO.read(stream);
stream.close();
thumbnail = JavaImageToMatlab(image);