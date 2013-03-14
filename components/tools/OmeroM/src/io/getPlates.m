function plates = getPlates(session, varargin)
% GETPLATES Retrieve plate objects from the OMERO server
%
%   plates = getPlates(session) returns all the plates owned by the
%   session user in the context of the session group.
%
%   plates = getPlates(session, ids) returns all the plates identified by
%   the input ids owned by the session user in the context of the session
%   group.
%
%   Examples:
%
%      plates = getPlates(session);
%      plates = getPlates(session, ids);
%
% See also: GETOBJECTS, GETSCREENS, GETIMAGES

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
ip.addOptional('ids', [], @(x) isempty(x) || isvector(x));
ip.parse(varargin{:});

plates = getObjects(session, ip.Results.ids, 'plate');