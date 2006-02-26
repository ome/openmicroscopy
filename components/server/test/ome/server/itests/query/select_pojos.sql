select 
	'Project' as Project ,
	p.id as prj ,
	p_o.id as p_owner ,
	p_g.id as p_group ,
	'Dataset' as Dataset ,
	d.id as ds ,
	d_o.id as d_owner ,
	d_g.id as d_group ,
	da.id as dann, 
	da.owner_id as dann_owner,
	da.group_id as dann_group,
	'Image' as Image,
	i.id as img,
	i_o.id as i_owner ,
	i_g.id as i_group ,
	ia.id as iann, 
	ia.owner_id as iann_owner ,
	ia.group_id as iann_group 

from 
	project p
	
	join experimenter p_o on (p_o.id = p.owner_id)
	join experimentergroup p_g on (p_g.id = p.group_id)
	join projectdatasetlink l on (p.id = l.parent) 
	join dataset d on (l.child = d.id) 
	join experimenter d_o on (d.owner_id = d_o.id) 
	join experimentergroup d_g on (d.group_id = d_g.id) 
	join datasetannotation da on (d.id = da.dataset)
	join datasetimagelink l2 on (l2.parent = d.id) 
	join image i on (l2.child = i.id) 
	join experimenter i_o on (i.owner_id = i_o.id)
	join experimentergroup i_g on (i.group_id = i_g.id)
	join imageannotation ia on (i.id = ia.image)

where 
	p.id between 9000 and 9999

;

