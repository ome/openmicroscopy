-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

begin;
set constraints all deferred;

insert into experimenter (id,version,omename,firstname,lastname)
        values (0,0,'root','root','root');
insert into event (id,time,status,experimenter) values (0,now(),'BOOTSTRAP',0);
insert into experimentergroup (id,permissions,version,owner_id,group_id,creation_id,update_id,name)
        values (0,-35,0,0,0,0,0,'system');
insert into experimentergroup (id,permissions,version,owner_id,group_id,creation_id,update_id,name)
        values (nextval('seq_experimentergroup'),-35,0,0,0,0,0,'user');
insert into experimentergroup (id,permissions,version,owner_id,group_id,creation_id,update_id,name)
        values (nextval('seq_experimentergroup'),-35,0,0,0,0,0,'default');
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value) values
        (0,-35,0,0,0,'Bootstrap');
insert into groupexperimentermap 
        (id,permissions,version,owner_id,group_id,creation_id,update_id,defaultgrouplink, parent, child) 
		values
		(0,-35,0,0,0,0,0,true,0,0);
insert into groupexperimentermap 
        (id,permissions,version,owner_id,group_id,creation_id,update_id,defaultgrouplink, parent, child) 
		select nextval('seq_groupexperimentermap'),-35,0,0,0,0,0,false,1,0;

		
update event set type = 0;
update event set experimentergroup = 0;

alter table event alter column type set not null;
alter table event alter column experimentergroup set not null;
	

insert into regiontype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_regiontype'),-35,0,0,0,'b';
insert into regiontype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_regiontype'),-35,0,0,0,'a';
insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_arctype'),-35,0,0,0,'Xe';
insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_arctype'),-35,0,0,0,'Hg-Xe';
insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_arctype'),-35,0,0,0,'Other';
insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_arctype'),-35,0,0,0,'Hg';
insert into overlaytype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_overlaytype'),-35,0,0,0,'text';
insert into overlaytype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_overlaytype'),-35,0,0,0,'arrow';
insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dimensionorder'),-35,0,0,0,'XYTCZ';
insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dimensionorder'),-35,0,0,0,'XYCTZ';
insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dimensionorder'),-35,0,0,0,'XYZTC';
insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dimensionorder'),-35,0,0,0,'XYTZC';
insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dimensionorder'),-35,0,0,0,'XYZCT';
insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dimensionorder'),-35,0,0,0,'XYCZT';
insert into irisdiaphragm (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_irisdiaphragm'),-35,0,0,0,'Iris';
insert into irisdiaphragm (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_irisdiaphragm'),-35,0,0,0,'I';
insert into irisdiaphragm (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_irisdiaphragm'),-35,0,0,0,'W/Iris';
insert into renderingmodel (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_renderingmodel'),-35,0,0,0,'hsb';
insert into renderingmodel (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_renderingmodel'),-35,0,0,0,'rgb';
insert into renderingmodel (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_renderingmodel'),-35,0,0,0,'greyscale';
insert into format (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_format'),-35,0,0,0,'STK';
insert into format (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_format'),-35,0,0,0,'DV';
insert into format (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_format'),-35,0,0,0,'TIFF';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'LaserScanningConfocal';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'FluorescenceCorrelationSpectroscopy';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'SlitScanConfocal';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'MultiPhotonMicroscopy';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'StructuredIllumination';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'SpectralImaging';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'SpinningDiskConfocal';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'NearFieldScanningOpticalMicroscopy';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'LaserScanningMicroscopy';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'SecondHarmonicGenerationImaging';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'TotalInternalReflection';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'FluorescenceLifetime';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'SingleMoleculeImaging';
insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_acquisitionmode'),-35,0,0,0,'Wide-field';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'FTIR';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'CCD';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'Analog-Video';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'Life-time-Imaging';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'Photodiode';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'Intensified-CCD';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'Spectroscopy';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'PMT';
insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_detectortype'),-35,0,0,0,'Correlation-Spectroscopy';
insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pulse'),-35,0,0,0,'Q-Switched';
insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pulse'),-35,0,0,0,'CW';
insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pulse'),-35,0,0,0,'Single';
insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pulse'),-35,0,0,0,'Repetitive';
insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pulse'),-35,0,0,0,'Mode-Locked';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Wl';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Gly';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Hl';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Water';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Oel';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Oil';
insert into immersionmedium (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_immersionmedium'),-35,0,0,0,'Wasser';
insert into semiconductorlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_semiconductorlasermedia'),-35,0,0,0,'GaAs';
insert into semiconductorlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_semiconductorlasermedia'),-35,0,0,0,'GaAlAs';
insert into dyelasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dyelasermedia'),-35,0,0,0,'Coumarin-C30';
insert into dyelasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_dyelasermedia'),-35,0,0,0,'Rhodamine-6G';
insert into family (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_family'),-35,0,0,0,'polynomial';
insert into family (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_family'),-35,0,0,0,'linear';
insert into family (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_family'),-35,0,0,0,'exponential';
insert into family (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_family'),-35,0,0,0,'logarithmic';
insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_microscopetype'),-35,0,0,0,'Electrophysiology';
insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_microscopetype'),-35,0,0,0,'Dissection';
insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_microscopetype'),-35,0,0,0,'Upright';
insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_microscopetype'),-35,0,0,0,'Inverted';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'complex';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'int8';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'int16';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'double-complex';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'int32';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'uint16';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'double';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'uint32';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'float';
insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_pixelstype'),-35,0,0,0,'uint8';
insert into correctioncollar (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_correctioncollar'),-35,0,0,0,'W/Corr';
insert into correctioncollar (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_correctioncollar'),-35,0,0,0,'CR';
insert into correctioncollar (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_correctioncollar'),-35,0,0,0,'Corr';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'Xenon';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'HeNe';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'H2O';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'CO';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'HFl';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'HeCd';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'CO2';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'Krypton';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'Nitrogen';
insert into gaslasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_gaslasermedia'),-35,0,0,0,'Argon';
insert into metalvaporlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_metalvaporlasermedia'),-35,0,0,0,'Ag';
insert into metalvaporlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_metalvaporlasermedia'),-35,0,0,0,'Cu';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'XeBr';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'ArFl';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'XeCl';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'KrCl';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'KrFl';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'ArCl';
insert into excimerlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_excimerlasermedia'),-35,0,0,0,'XeFl';
insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_filtertype'),-35,0,0,0,'ShortPass';
insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_filtertype'),-35,0,0,0,'MultiPass';
insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_filtertype'),-35,0,0,0,'BandPass';
insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_filtertype'),-35,0,0,0,'LongPass';
insert into filamenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_filamenttype'),-35,0,0,0,'Incandescent';
insert into filamenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_filamenttype'),-35,0,0,0,'Halogen';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Fluar';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Fluor';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Achromat';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Fluotar';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Neofluar';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Fl';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Apo';
insert into aberrationcorrection (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_aberrationcorrection'),-35,0,0,0,'Achro';
insert into freeelectronlasermedia (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_freeelectronlasermedia'),-35,0,0,0,'e-';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'Fluorescence';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'Phase';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'PolarizedLight';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'ObliqueIllumination';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'DIC';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'Brightfield';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'Darkfield';
insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_contrastmethod'),-35,0,0,0,'HoffmanModulation';
insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_illumination'),-35,0,0,0,'Transmitted';
insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_illumination'),-35,0,0,0,'Epifluorescence';
insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_illumination'),-35,0,0,0,'Oblique';
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_eventtype'),-35,0,0,0,'Test';
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_eventtype'),-35,0,0,0,'Task';
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_eventtype'),-35,0,0,0,'Shoola';
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_eventtype'),-35,0,0,0,'Internal';
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_eventtype'),-35,0,0,0,'Import';
insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_eventtype'),-35,0,0,0,'User';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Photablation';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Time-lapse';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'FISH';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Optical-Trapping';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'PGI/Documentation';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Fluorescence-Lifetime';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Uncaging';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Screen';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Electropyhsiology';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Other';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'FRAP';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Photoactivation';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Immunocytopchemistry';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Immunofluroescence';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Ion-Imaging';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'FRET';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Spectral-Imaging';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'4-D+';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'Colocalization';
insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_experimenttype'),-35,0,0,0,'FP';
insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_photometricinterpretation'),-35,0,0,0,'Monochrome';
insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_photometricinterpretation'),-35,0,0,0,'CMYK';
insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_photometricinterpretation'),-35,0,0,0,'ARGB';
insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_photometricinterpretation'),-35,0,0,0,'RGB';
insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
	select nextval('seq_photometricinterpretation'),-35,0,0,0,'HSV';

create table password ( experimenter_id bigint unique not null REFERENCES experimenter (id) , hash char(24) );
insert into password values (0,'@ROOTPASS@'); -- root can now login with 'ome'

commit;
