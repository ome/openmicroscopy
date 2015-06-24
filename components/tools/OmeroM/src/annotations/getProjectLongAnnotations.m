function las = getProjectLongAnnotations(session, ids, varargin)
% GETPROJECTLONGANNOTATIONS Retrieve long annotations linked to projects
%
%    las = getProjectLongAnnotations(session, ids) returns all long
%    annotations linked to the projects specified by the input identifiers
%    ids and owned by the session user.
%
%    las = getProjectLongAnnotations(session, projects) returns all long
%    annotations linked to the input projects and owned by the session
%    user.
%
%    las = getProjectLongAnnotations(session,  ids, 'include', include) only
%    returns long annotations with the input namespace.
%
%    las = getProjectLongAnnotations(session,  ids, 'exclude', exclude)
%    excludes long annotations with the input namespace.
%
%    las = getProjectLongAnnotations(session, ids, 'owner', ownerid)
%    returns the long annotations owned by the user specified by ownerid.
%    Use -1 to return the long annotations owned by all users.
%
%    Examples:
%
%        las = getProjectLongAnnotations(session, ids)
%        las = getProjectLongAnnotations(session, projects)
%        las = getProjectLongAnnotations(session, ids, 'include', include)
%        las = getProjectLongAnnotations(session, ids, 'exclude', exclude)
%        las = getProjectLongAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPROJECTCOMMENTANNOTATIONS,
% GETPROJECTDOUBLEANNOTATIONS, GETPROJECTFILEANNOTATIONS,
% GETPROJECTTAGANNOTATIONS, GETPROJECTTIMESTAMPANNOTATIONS,
% GETPROJECTXMLANNOTATIONS

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

las = getObjectAnnotations(session, 'long', 'project', ids, varargin{:});