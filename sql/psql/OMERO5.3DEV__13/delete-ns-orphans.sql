-- Copyright (C) 2016 University of Dundee. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--
-- This program is free software; you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation; either version 2 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along
-- with this program; if not, write to the Free Software Foundation, Inc.,
-- 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--

---
--- Delete non-sharable orphaned annotations from OMERO 5.3. Optional.
---

DELETE FROM annotation WHERE
    discriminator IN
        ('/basic/bool/', '/basic/num/double/', '/basic/num/long/',
         '/basic/term/', '/basic/time/', '/basic/text/comment/')
    AND (ns IS NULL OR ns = '' OR ns LIKE 'openmicroscopy.org/%')
    AND id NOT IN (SELECT parent FROM annotationannotationlink)
    AND id NOT IN (SELECT child FROM annotationannotationlink)
    AND id NOT IN (SELECT child FROM channelannotationlink)
    AND id NOT IN (SELECT child FROM datasetannotationlink)
    AND id NOT IN (SELECT child FROM detectorannotationlink)
    AND id NOT IN (SELECT child FROM dichroicannotationlink)
    AND id NOT IN (SELECT child FROM experimenterannotationlink)
    AND id NOT IN (SELECT child FROM experimentergroupannotationlink)
    AND id NOT IN (SELECT child FROM filesetannotationlink)
    AND id NOT IN (SELECT child FROM filterannotationlink)
    AND id NOT IN (SELECT child FROM folderannotationlink)
    AND id NOT IN (SELECT child FROM imageannotationlink)
    AND id NOT IN (SELECT child FROM instrumentannotationlink)
    AND id NOT IN (SELECT child FROM lightpathannotationlink)
    AND id NOT IN (SELECT child FROM lightsourceannotationlink)
    AND id NOT IN (SELECT child FROM namespaceannotationlink)
    AND id NOT IN (SELECT child FROM nodeannotationlink)
    AND id NOT IN (SELECT child FROM objectiveannotationlink)
    AND id NOT IN (SELECT child FROM originalfileannotationlink)
    AND id NOT IN (SELECT child FROM planeinfoannotationlink)
    AND id NOT IN (SELECT child FROM plateacquisitionannotationlink)
    AND id NOT IN (SELECT child FROM plateannotationlink)
    AND id NOT IN (SELECT child FROM projectannotationlink)
    AND id NOT IN (SELECT child FROM reagentannotationlink)
    AND id NOT IN (SELECT child FROM roiannotationlink)
    AND id NOT IN (SELECT child FROM screenannotationlink)
    AND id NOT IN (SELECT child FROM sessionannotationlink)
    AND id NOT IN (SELECT child FROM shapeannotationlink)
    AND id NOT IN (SELECT child FROM wellannotationlink);
