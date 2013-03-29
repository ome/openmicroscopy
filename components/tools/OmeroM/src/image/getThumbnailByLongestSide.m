function thumbnail = getThumbnailByLongestSide(session, image, varargin)
% GETTHUMBNAILBYLONGESTSIDE Retrieve thumbnail from an image on the OMERO server
%
%   thumbnail = getThumbnailByLongestSide(session, image) returns the
%   thumbnail from the input image where the aspect ratio of the original
%   image is preserved.
%
%   thumbnail = getThumbnailByLongestSide(session, image, size) also sets
%   the size of of the longest side.
%
%   thumbnail = getThumbnailByLongestSide(session, imageID) returns the
%   thumbnail from the input image identifier where the aspect ratio of the
%   original image is preserved.
%
%   thumbnail = getThumbnailByLongestSide(session, imageID, size) also sets
%   the size of of the longest side.
%
%   Examples:
%
%      thumbnail = getThumbnailByLongestSide(session, image);
%      thumbnail = getThumbnailByLongestSide(session, image, size);
%      thumbnail = getThumbnailByLongestSide(session, imageID);
%      thumbnail = getThumbnailByLongestSide(session, imageID, size);
%
% See also: GETTHUMBNAILSTORE, GETTHUMBNAIL

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
ip.addOptional('size', [], isposint);
ip.parse(varargin{:});

% Initialize raw pixels store
store = getThumbnailStore(session, image);

% Retrieve thumbnail set
if ~isempty(ip.Results.size), size = rint(ip.Results.size); else size = []; end
byteArray = store.getThumbnailByLongestSide(size);
store.close();

% Convert byteArray into Matlab image
stream = java.io.ByteArrayInputStream(byteArray);
image = javax.imageio.ImageIO.read(stream);
stream.close();
thumbnail = JavaImageToMatlab(image);