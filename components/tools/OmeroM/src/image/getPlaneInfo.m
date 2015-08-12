function planeInfo = getPlaneInfo(session, image, varargin)
% GETPLANEINFO Retrieve plane information from an image on the OMERO server
%
%   planeInfo = getPlaneInfo(session, image) returns the information for
%   all planes of the input image.
%
%   planeInfo = getPlaneInfo(session, imageID) returns the information for
%   all planes of the input image identifier.
%
%   planeInfo = getPlaneInfo(session, image, z, c, t) returns the
%   information for the planes at the input z, c, t coordinates of the
%   input image.
%
%   planeInfo = getPlaneInfo(session, imageID, z, c, t) returns the
%   information for the plane at the input z, c, t coordinates of the input
%   image identifier.
%
%   Examples:
%
%      planeInfo = getPlaneInfo(session, image);
%      planeInfo = getPlaneInfo(session, imageID);
%      planeInfo = getPlaneInfo(session, image, z, c, t);
%      planeInfo = getPlaneInfo(session, imageID, z, c, t);
%
% See also: GETPLANE

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

% Check image input
isValidImageInput = @(x) isscalar(x) && ...
    (isa(x, 'omero.model.ImageI') || isnumeric(x));
ip = inputParser;
ip.addRequired('image', isValidImageInput);
ip.parse(image);

% Retrieve image object if image identifier is input
if isnumeric(ip.Results.image)
    image = getImages(session, ip.Results.image);
    assert(numel(image) == 1, 'No image found with ID: %u', ip.Results.image);
end

% Read z, c, t dimensions
pixels = image.getPrimaryPixels;
sizeZ = pixels.getSizeZ().getValue();
sizeC = pixels.getSizeC().getValue();
sizeT = pixels.getSizeT().getValue();
isValidZ = @(x) isscalar(x) && ismember(x, 0 : sizeZ - 1);
isValidC = @(x) isscalar(x) && ismember(x, 0 : sizeC - 1);
isValidT = @(x) isscalar(x) && ismember(x, 0 : sizeT - 1);

% Check optional z, c, t input
ip = inputParser;
ip.addOptional('z', [],  @(x) isempty(x) || isValidZ(x));
ip.addOptional('c', [],  @(x) isempty(x) || isValidC(x));
ip.addOptional('t', [],  @(x) isempty(x) || isValidT(x));
ip.parse(varargin{:});

% Buld plane info query
query = java.lang.StringBuilder('select info from PlaneInfo as info ');
query.append('where pixels.id = :id');

params = omero.sys.ParametersI();
params.addId(image.getPrimaryPixels.getId);

if (ip.Results.z >= 0)
    query.append(' and info.theZ = :z');
    params.add(java.lang.String('z'), rint(ip.Results.z));
end

if (ip.Results.t >= 0)
    query.append(' and info.theT = :t');
    params.add(java.lang.String('t'), rint(ip.Results.t));
end

if (ip.Results.c >= 0)
    query.append(' and info.theC = :c');
    params.add(java.lang.String('c'), rint(ip.Results.c));
end

% Retrieve plane info
context = java.util.HashMap;
context.put('omero.group', '-1');
planeInfo = session.getQueryService().findAllByQuery(...
    query.toString, params, context);
planeInfo = toMatlabList(planeInfo);
