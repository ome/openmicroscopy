function tas = getProjectTagAnnotations(session, ids, varargin)
% GETPROJECTTAGANNOTATIONS Retrieve tag annotations linked to projects
%
%    tas = getProjectTagAnnotations(session, ids) returns all tag
%    annotations linked to the projects specified by the input identifiers
%    ids and owned by the session user.
%
%    tas = getProjectTagAnnotations(session, projects) returns all tag
%    annotations linked to the input projects and owned by the session
%    user.
%
%    tas = getProjectTagAnnotations(session,  ids, 'include', include) only
%    returns tag annotations with the input namespace.
%
%    tas = getProjectTagAnnotations(session,  ids, 'exclude', exclude)
%    excludes tag annotations with the input namespace.
%
%    tas = getProjectTagAnnotations(session, ids, 'owner', ownerid)
%    returns the tag annotations owned by the user specified by ownerid.
%    Use -1 to return the tag annotations owned by all users.
%
%    Examples:
%
%        tas = getProjectTagAnnotations(session, ids)
%        tas = getProjectTagAnnotations(session, projects)
%        tas = getProjectTagAnnotations(session, ids, 'include', include)
%        tas = getProjectTagAnnotations(session, ids, 'exclude', exclude)
%        tas = getProjectTagAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPROJECTCOMMENTANNOTATIONS,
% GETPROJECTDOUBLEANNOTATIONS, GETPROJECTFILEANNOTATIONS,
% GETPROJECTLONGANNOTATIONS, GETPROJECTTIMESTAMPANNOTATIONS,
% GETPROJECTXMLANNOTATIONS

% Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
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

tas = getObjectAnnotations(session, 'tag', 'project', ids, varargin{:});