function xas = getPlateXmlAnnotations(session, ids, varargin)
% GETPLATEXMLANNOTATIONS Retrieve xml annotations linked to plates
%
%    xas = getPlateXmlAnnotations(session, ids) returns all xml
%    annotations linked to the plates specified by the input identifiers
%    and owned by the session user.
%
%    xas = getPlateXmlAnnotations(session, plates) returns all xml
%    annotations linked to the input plates and owned by the session user.
%
%    xas = getPlateXmlAnnotations(session,  ids, 'include', include) only
%    returns xml annotations with the input namespace.
%
%    xas = getPlateXmlAnnotations(session,  ids, 'exclude', exclude)
%    excludes xml annotations with the input namespace.
%
%    xas = getPlateXmlAnnotations(session, ids, 'owner', ownerid)
%    returns the xml annotations owned by the user specified by ownerid.
%    Use -1 to return the xml annotations owned by all users.
%
%    Examples:
%
%        xas = getPlateXmlAnnotations(session, ids)
%        xas = getPlateXmlAnnotations(session, plates)
%        xas = getPlateXmlAnnotations(session, ids, 'include', include)
%        xas = getPlateXmlAnnotations(session, ids, 'exclude', exclude)
%        xas = getPlateXmlAnnotations(session, ids, 'owner', -1)
%
% See also: GETOBJECTANNOTATIONS, GETPLATECOMMENTANNOTATIONS,
% GETPLATEDOUBLEANNOTATIONS, GETPLATEFILEANNOTATIONS,
% GETPLATELONGANNOTATIONS, GETPLATETAGANNOTATIONS,
% GETPLATETIMESTAMPANNOTATIONS

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

xas = getObjectAnnotations(session, 'xml', 'plate', ids, varargin{:});