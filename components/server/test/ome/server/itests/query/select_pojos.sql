select 
	p.id as prj ,
	d.id as ds ,
	da.id as dann, 
	da.owner_id as dann_owner,
	i.id as img,
	ia.id as iann, 
	ia.owner_id as iann_owner 

from 
	project p 
	
	join projectdatasetlink l on (p.id = l.parent) 
	join dataset d on (l.child = d.id) 
	join datasetannotation da on (d.id = da.dataset)
	join datasetimagelink l2 on (l2.parent = d.id) 
	join image i on (l2.child = i.id) 
	join imageannotation ia on (i.id = ia.image)

;

