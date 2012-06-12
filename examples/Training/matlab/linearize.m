function [value] = linearize(z, t, sizeZ)
% Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
% All rights reversed.
% Use is subject to license terms supplied in LICENSE.txt
value = sizeZ*t+z;