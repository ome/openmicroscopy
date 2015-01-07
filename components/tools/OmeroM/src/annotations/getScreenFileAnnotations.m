function fas = getScreenFileAnnotations(session, ids, varargin)
% GETSCREENFILEANNOTATIONS Retrieve file annotations linked to screens
%
%    fas = getScreenFileAnnotations(session, ids) returns all file
%    annotations linked to the screens specified by the input identifiers
%    and owned by the session user.
%
%    fas = getScreenFileAnnotations(session, screens) returns all file
%    annotations linked to the input screens and owned by the session user.
%
%    fas = getScreenFileAnnotations(session,  ids, 'include', include) only
%    returns file annotations with the input namespace.
%
%    fas = getScreenFileAnnotations(session,  ids, 'exclude', exclude)
%    excludes file annotations with the input namespace.
%
%    fas = getScreenFileAnnotations(session, ids, 'owner', ownerid)
%    returns the file annotations owned by the user specified by ownerid.
%    Use -1 to return the file annotations owned by all users.
%
%    Examples:
%
%        fas = getScreenFileAnnotations(session, ids)
%        fas = getScreenFileAnnotations(session, screens)
%        fas = getScreenFileAnnotations(session, ids, 'include', include)
%        fas = getScreenFileAnnotations(session, ids, 'exclude', exclude)
%        fas = getScreenFileAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETSCREENCOMMENTANNOTATIONS,
% GETSCREENDOUBLEANNOTATIONS, GETSCREENLONGANNOTATIONS,
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

fas = getObjectAnnotations(session, 'file', 'screen', ids, varargin{:});