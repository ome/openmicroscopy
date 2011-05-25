--
-- Copyright 2011 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- This SQL script provides some minor data fixes to allow
-- the OMERO4.2__0.sql upgrade script to complete successfully.
-- This is primarily due to the checks added for ticket #4822,
-- guaranteeing that some columns are within given ranges.
--

BEGIN;

UPDATE pixels SET sizeZ = 1 WHERE sizeZ = 0;

UPDATE logicalchannel SET excitationWave = null WHERE excitationWave <= 0;

UPDATE logicalchannel SET emissionWave = null WHERE emissionWave <= 0;

COMMIT;
