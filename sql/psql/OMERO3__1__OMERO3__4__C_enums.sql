 begin;

 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'8x8';
 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'4x4';
 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'2x2';
 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'1x1';

 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'UV';
 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'SuperFluor';
 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'PlanFluor';
 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'PlanApo';

 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x4';
 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x3';
 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x2';
 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x1';

 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Wl';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Water';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Wasser';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Oil';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Oel';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Hl';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Gly';

 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Waiting';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Submitted';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Running';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Resubmitted';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Requeued';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Queued';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Finished';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Error';

 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'lasermediumeFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'lasermediumeCl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'lasermediumeBr';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'lasermediume';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Rhodamine-5G';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'N';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'KrFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'KrCl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Kr';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'HFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'HeNe';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'HeCd';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'H2O';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'GaAs';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'GaAlAs';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'e-';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Cu';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Coumaring-C30';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'CO2';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'CO';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'ArFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'ArCl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Ar';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Ag';

 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'SolidState';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Semiconductor';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'MetalVapor';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Gas';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'FreeElectron';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Excimer';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Dye';

 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Water';
 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Oil';
 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Glycerol';
 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Air';

 insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_photometricinterpretation'),-35,0,0,0,'ColorMap';

 commit;
