function cas = getPlateCommentAnnotations(session, ids, varargin)
% GETPLATECOMMENTANNOTATIONS Retrieve comment annotations linked to plates
%
%    cas = getPlateCommentAnnotations(session, ids) returns all comment
%    annotations linked to the plates specified by the input identifiers ids
%    and owned by the session user.
%
%    cas = getPlateCommentAnnotations(session, plates) returns all comment
%    annotations linked to the input plates and owned by the session
%    user.
%
%    cas = getPlateCommentAnnotations(session,  ids, 'include', include)
%    only returns comment annotations with the input namespace.
%
%    cas = getPlateCommentAnnotations(session,  ids, 'exclude', exclude)
%    excludes comment annotations with the input namespace.
%
%    cas = getPlateCommentAnnotations(session, ids, 'owner', ownerid)
%    returns the comment annotations owned by the user specified by
%    ownerid. Use -1 to return the comment annotations owned by all users.
%
%    Examples:
%
%        cas = getPlateCommentAnnotations(session, ids)
%        cas = getPlateCommentAnnotations(session, plates)
%        cas = getPlateCommentAnnotations(session, ids, 'include', include)
%        cas = getPlateCommentAnnotations(session, ids, 'exclude', exclude)
%        cas = getPlateCommentAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPLATEDOUBLEANNOTATIONS,
% GETPLATEFILEANNOTATIONS, GETPLATELONGANNOTATIONS, GETPLATETAGANNOTATIONS,
% GETPLATETIMESTAMPANNOTATIONS, GETPLATEXMLANNOTATIONS

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

cas = getObjectAnnotations(session, 'comment', 'plate', ids, varargin{:});