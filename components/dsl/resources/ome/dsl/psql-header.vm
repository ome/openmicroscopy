
--
-- GENERATED %(TIME)s from %(DIR)s
--
-- This file was created by the bin/omero db script command
-- and contains an MD5 version of your OMERO root users's password.
-- You should think about deleting it as soon as possible.
--
-- To create your database:
--
--     createdb omero
--     createlang plpgsql omero
--     psql omero < %(SCRIPT)s
--
--
BEGIN;

CREATE DOMAIN nonnegative_int AS INTEGER CHECK (VALUE >= 0);
CREATE DOMAIN positive_int AS INTEGER CHECK (VALUE > 0);
CREATE DOMAIN positive_float AS DOUBLE PRECISION CHECK (VALUE > 0);
CREATE DOMAIN percent_fraction AS DOUBLE PRECISION CHECK (VALUE >= 0 AND VALUE <= 1);

CREATE TYPE UnitsElectricPotential AS ENUM ('YOTTAV','ZETTAV','EXAV','PETAV','TERAV','GIGAV','MEGAV','KV','HV','DAV','V','DV','CV','MV','MICROV','NV','PV','FV','AV','ZV','YV');

CREATE TYPE UnitsFrequency AS ENUM ('YOTTAHZ','ZETTAHZ','EXAHZ','PETAHZ','TERAHZ','GIGAHZ','MEGAHZ','KHZ','HHZ','DAHZ','HZ','DHZ','CHZ','MHZ','MICROHZ','NHZ','PHZ','FHZ','AHZ','ZHZ','YHZ');

CREATE TYPE UnitsLength AS ENUM ('YOTTAM','ZETTAM','EXAM','PETAM','TERAM','GIGAM','MEGAM','KM','HM','DAM','M','DM','CM','MM','MICROM','NM','PM','FM','AM','ZM','YM','ANGSTROM','UA','LY','PC','THOU','LI','IN','FT','YD','MI','PT','PIXEL','REFERENCEFRAME');

CREATE TYPE UnitsPower AS ENUM ('YOTTAW','ZETTAW','EXAW','PETAW','TERAW','GIGAW','MEGAW','KW','HW','DAW','W','DW','CW','MW','MICROW','NW','PW','FW','AW','ZW','YW');

CREATE TYPE UnitsPressure AS ENUM ('YOTTAPA','ZETTAPA','EXAPA','PETAPA','TERAPA','GIGAPA','MEGAPA','KPA','HPA','DAPA','PA','DPA','CPA','MPA','MICROPA','NPA','PPA','FPA','APA','ZPA','YPA','BAR','MEGABAR','KBAR','DBAR','CBAR','MBAR','ATM','PSI','TORR','MTORR','MMHG');

CREATE TYPE UnitsTemperature AS ENUM ('K','DEGREEC','DEGREEF','DEGREER');

CREATE TYPE UnitsTime AS ENUM ('YOTTAS','ZETTAS','EXAS','PETAS','TERAS','GIGAS','MEGAS','KS','HS','DAS','S','DS','CS','MS','MICROS','NS','PS','FS','AS','ZS','YS','MIN','H','D');
