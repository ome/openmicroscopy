function das = getScreenDoubleAnnotations(session, ids, varargin)
% GETSCREENDOUBLEANNOTATIONS Retrieve double annotations linked to screens
%
%    das = getScreenDoubleAnnotations(session, ids) returns all double
%    annotations linked to the screens specified by the input identifiers
%    ids and owned by the session user.
%
%    das = getScreenDoubleAnnotations(session, screens) returns all double
%    annotations linked to the input screens and owned by the session user.
%
%    das = getScreenDoubleAnnotations(session,  ids, 'include', include)
%    only returns double annotations with the input namespace.
%
%    das = getScreenDoubleAnnotations(session,  ids, 'exclude', exclude)
%    excludes double annotations with the input namespace.
%
%    das = getScreenDoubleAnnotations(session, ids, 'owner', ownerid)
%    returns the double annotations owned by the user specified by
%    ownerid. Use -1 to return the double annotations owned by all users.
%
%    Examples:
%
%        das = getScreenDoubleAnnotations(session, ids)
%        das = getScreenDoubleAnnotations(session, screens)
%        das = getScreenDoubleAnnotations(session, ids, 'include', include)
%        das = getScreenDoubleAnnotations(session, ids, 'exclude', exclude)
%        das = getScreenDoubleAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETSCREENCOMMENTANNOTATIONS,
% GETSCREENFILEANNOTATIONS, GETSCREENLONGANNOTATIONS,
% GETSCREENTAGANNOTATIONS, GETSCREENTIMESTAMPANNOTATIONS,
% GETSCREENXMLANNOTATIONS

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

das = getObjectAnnotations(session, 'double', 'screen', ids, varargin{:});