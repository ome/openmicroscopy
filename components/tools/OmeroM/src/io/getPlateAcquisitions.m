function pas = getPlateAcquisitions(session, varargin)
% GETPLATEACQUISITIONS Retrieve plate acquisition objects from the OMERO server
%
%   pas = getPlateAcquisitions(session) returns all the plateruns owned
%   by the session user in the context of the session group.
%
%   pas = getPlateAcquisitions(session, ids) returns all the plate runs
%   identified by the input ids owned by the session user in the context of
%   the session group.
%
%   pas = getPlateAcquisitions(session, ids, 'owmer', ownerId) returns all
%   the plate runs identified by the input ids owned by the input owner in
%   the context of the session group.
%
%   Examples:
%
%      pas = getPlateAcquisitions(session);
%      pas = getPlateAcquisitions(session, ids);
%      pas = getPlateAcquisitions(session, 'owner', ownerId);
%      pas = getPlateAcquisitions(session, ids, 'owner', ownerId);
%
% See also: GETOBJECTS, GETPLATES

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
userId = session.getAdminService().getEventContext().userId;
ip = inputParser;
ip.addOptional('ids', [], @(x) isempty(x) || (isvector(x) && isnumeric(x)));
ip.addParamValue('owner', userId, @isscalar);
ip.parse(varargin{:});

pas = getObjects(session, ip.Results.ids, 'plateacquisition',...
    'owner', ip.Results.owner);