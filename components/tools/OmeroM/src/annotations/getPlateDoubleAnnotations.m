function das = getPlateDoubleAnnotations(session, ids, varargin)
% GETPLATEDOUBLEANNOTATIONS Retrieve double annotations linked to plates
%
%    das = getPlateDoubleAnnotations(session, ids) returns all double
%    annotations linked to the plates specified by the input identifiers
%    ids and owned by the session user.
%
%    das = getPlateDoubleAnnotations(session, plates) returns all double
%    annotations linked to the input plates and owned by the session user.
%
%    das = getPlateDoubleAnnotations(session,  ids, 'include', include)
%    only returns double annotations with the input namespace.
%
%    das = getPlateDoubleAnnotations(session,  ids, 'exclude', exclude)
%    excludes double annotations with the input namespace.
%
%    das = getPlateDoubleAnnotations(session, ids, 'owner', ownerid)
%    returns the double annotations owned by the user specified by
%    ownerid. Use -1 to return the double annotations owned by all users.
%
%    Examples:
%
%        das = getPlateDoubleAnnotations(session, ids)
%        das = getPlateDoubleAnnotations(session, plates)
%        das = getPlateDoubleAnnotations(session, ids, 'include', include)
%        das = getPlateDoubleAnnotations(session, ids, 'exclude', exclude)
%        das = getPlateDoubleAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPLATECOMMENTANNOTATIONS,
% GETPLATEFILEANNOTATIONS, GETPLATELONGANNOTATIONS, GETPLATETAGANNOTATIONS,
% GETPLATETIMESTAMPANNOTATIONS, GETPLATEXMLANNOTATIONS

% Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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

das = getObjectAnnotations(session, 'double', 'plate', ids, varargin{:});