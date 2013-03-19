function stack = getStack(session, image, c, t)
% GETSTACK Retrieve stack from an image on the OMERO server
%
%   stack = getStack(session, image, c, t) returns the stack from input
%   image at the input c, t coordinates.
%
%   stack = getStack(session, imageID, z, c, t) returns the stack from
%   input image identifier at the input c, t coordinates.
%
%
%   Examples:
%
%      stack = getStack(session, image, c, t);
%      stack = getStack(session, imageID, c, t);
%
% See also: GETRAWPIXELSSTORE, GETPLANE, GETTILE

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

% Initialize raw pixels store
[store, pixels] = getRawPixelsStore(session, image);
sizeC = pixels.getSizeC().getValue();
sizeT = pixels.getSizeT().getValue();

% Input check
ip = inputParser;
isposint = @(x) isnumeric(x) & x >= 0 & abs(round(x)) == x;
ip.addRequired('c', @(x) isposint(x) && x < sizeC);
ip.addRequired('t', @(x) isposint(x) && x < sizeT);
ip.parse(c, t);

% Read stack
stack = store.getStack(c, t);
stack = permute(toMatrix(stack, pixels), [2 1 3]);

% Close the store
store.close();