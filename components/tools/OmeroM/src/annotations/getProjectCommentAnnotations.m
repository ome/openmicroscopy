function cas = getProjectCommentAnnotations(session, ids, varargin)
% GETPROJECTCOMMENTANNOTATIONS Retrieve comment annotations linked to projects
%
%    cas = getProjectCommentAnnotations(session, ids) returns all comment
%    annotations linked to the projects specified by the input identifiers
%    and owned by the session user.
%
%    cas = getProjectCommentAnnotations(session, projects) returns all
%    comment annotations linked to the input projects and owned by the
%    session user.
%
%    cas = getProjectCommentAnnotations(session,  ids, 'include', include)
%    only returns comment annotations with the input namespace.
%
%    cas = getProjectCommentAnnotations(session,  ids, 'exclude', exclude)
%    excludes comment annotations with the input namespace.
%
%    cas = getProjectCommentAnnotations(session, ids, 'owner', ownerid)
%    returns the comment annotations owned by the user specified by
%    ownerid. Use -1 to return the comment annotations owned by all users.
%
%    Examples:
%
%        cas = getProjectCommentAnnotations(session, ids)
%        cas = getProjectCommentAnnotations(session, projects)
%        cas = getProjectCommentAnnotations(session, ids, 'include', include)
%        cas = getProjectCommentAnnotations(session, ids, 'exclude', exclude)
%        cas = getProjectCommentAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPROJECTDOUBLEANNOTATIONS,
% GETPROJECTFILEANNOTATIONS, GETPROJECTTAGANNOTATIONS,
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

cas = getObjectAnnotations(session, 'comment', 'project', ids, varargin{:});