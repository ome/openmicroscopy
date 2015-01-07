function tas = getPlateTimestampAnnotations(session, ids, varargin)
% GETPLATETIMESTAMPANNOTATIONS Retrieve timestamp annotations linked to plates
%
%    tas = getPlateTimestampAnnotations(session, ids) returns all timestamp
%    annotations linked to the plates specified by the input identifiers
%    ids and owned by the session user.
%
%    tas = getPlateTimestampAnnotations(session, datasets) returns all 
%    timestamp annotations linked to the input plates and owned by the 
%    session user.
%
%    tas = getPlateTimestampAnnotations(session,  ids, 'include', include) 
%    only returns timestamp annotations with the input namespace.
%
%    tas = getPlateTimestampAnnotations(session,  ids, 'exclude', exclude)
%    excludes timestamp annotations with the input namespace.
%
%    tas = getPlateTimestampAnnotations(session, ids, 'owner', ownerid)
%    returns the timestamp annotations owned by the user specified by ownerid.
%    Use -1 to return the tag annotations owned by all users.
%
%    Examples:
%
%        tas = getPlateTimestampAnnotations(session, ids)
%        tas = getPlateTimestampAnnotations(session, plates)
%        tas = getPlateTimestampAnnotations(session, ids, 'include', include)
%        tas = getPlateTimestampAnnotations(session, ids, 'exclude', exclude)
%        tas = getPlateTimestampAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPLATECOMMENTANNOTATIONS,
% GETPLATEDOUBLEANNOTATIONS, GETPLATEFILEANNOTATIONS,
% GETPLATELONGANNOTATIONS, GETPLATETAGANNOTATIONS, GETPLATEXMLANNOTATIONS

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

tas = getObjectAnnotations(session, 'timestamp', 'plate', ids, varargin{:});