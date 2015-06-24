function las = getDatasetLongAnnotations(session, ids, varargin)
% GETDATASETLONGANNOTATIONS Retrieve long annotations linked to datasets
%
%    las = getDatasetLongAnnotations(session, ids) returns all long
%    annotations linked to the datasets specified by the input identifiers
%    ids and owned by the session user.
%
%    las = getDatasetLongAnnotations(session, datasets) returns all long
%    annotations linked to the input datasets and owned by the session
%    user.
%
%    las = getDatasetLongAnnotations(session,  ids, 'include', include) only
%    returns long annotations with the input namespace.
%
%    las = getDatasetLongAnnotations(session,  ids, 'exclude', exclude)
%    excludes long annotations with the input namespace.
%
%    las = getDatasetLongAnnotations(session, ids, 'owner', ownerid)
%    returns the long annotations owned by the user specified by ownerid.
%    Use -1 to return the long annotations owned by all users.
%
%    Examples:
%
%        las = getDatasetLongAnnotations(session, ids)
%        las = getDatasetLongAnnotations(session, datasets)
%        las = getDatasetLongAnnotations(session, ids, 'include', include)
%        las = getDatasetLongAnnotations(session, ids, 'exclude', exclude)
%        las = getDatasetLongAnnotations(session, ids, 'owner', -1)
%
% See also: GETDATASETCOMMENTANNOTATIONS, GETDATASETDOUBLEANNOTATIONS,
% GETDATASETFILEANNOTATIONS, GETDATASETTAGANNOTATIONS,
% GETDATASETTIMESTAMPANNOTATIONS, GETDATASETXMLANNOTATIONS,
% GETOBJECTANNOTATIONS

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

las = getObjectAnnotations(session, 'long', 'dataset', ids, varargin{:});