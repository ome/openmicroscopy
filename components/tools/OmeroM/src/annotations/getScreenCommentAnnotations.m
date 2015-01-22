function cas = getScreenCommentAnnotations(session, ids, varargin)
% GETSCREENCOMMENTANNOTATIONS Retrieve comment annotations linked to screens
%
%    cas = getScreenCommentAnnotations(session, ids) returns all comment
%    annotations linked to the screens specified by the input identifiers
%    and owned by the session user.
%
%    cas = getScreenCommentAnnotations(session, screens) returns all
%    comment annotations linked to the input screens and owned by the
%    session user.
%
%    cas = getScreenCommentAnnotations(session,  ids, 'include', include)
%    only returns comment annotations with the input namespace.
%
%    cas = getScreenCommentAnnotations(session,  ids, 'exclude', exclude)
%    excludes comment annotations with the input namespace.
%
%    cas = getScreenCommentAnnotations(session, ids, 'owner', ownerid)
%    returns the comment annotations owned by the user specified by
%    ownerid. Use -1 to return the comment annotations owned by all users.
%
%    Examples:
%
%        cas = getScreenCommentAnnotations(session, ids)
%        cas = getScreenCommentAnnotations(session, ids, 'include', include)
%        cas = getScreenCommentAnnotations(session, ids, 'exclude', exclude)
%        cas = getScreenCommentAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETSCREENDOUBLEANNOTATIONS,
% GETSCREENFILEANNOTATIONS, GETSCREENLONGANNOTATIONS,
% GETSCREENTAGANNOTATIONS, GETSCREENTIMESTAMPANNOTATIONS,
% GETSCREENXMLANNOTATIONS

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

cas = getObjectAnnotations(session, 'comment', 'screen', ids, varargin{:});