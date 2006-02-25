begin;
COPY experimenter (id, "version", permissions, middlename, institution, omename, lastname, email, firstname) FROM stdin with null as 'null';
10000	0	null	null	null	tester	Allen	blah	Mr.
\.

COPY project 
(id, 	"version", owner_id, group_id, creation_id, update_id, permissions, name, description) FROM stdin with null as 'null';
9090	0	0	0	0	null	null	'hi' 	'root project without links'
9091	0	0	0	0	null	null	'hi'	'root project with own annotations'
9092	0	0	0	0	null	null	'hi'	'root project with foreign annotations'
9990	0	10000	0	0	null	null	'hi'	'user project without links'
9991	0	10000	0	0	null	null	'hi'	'user project with own annotations'
9992	0	10000	0	0	null	null	'hi'	'user project with foreign annotations'
\.


COPY dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) FROM stdin with null as 'null';
7070	0	0	0	0	null	null	'hi' 	'root data without links'
7071	0	0	0	0	null	null	'hi'	'root data with own annotations'
7072	0	0	0	0	null	null	'hi'	'root data with foreign annotations'
7770	0	10000	0	0	null	null	'hi'	'user data without links'
7771	0	10000	0	0	null	null	'hi'	'user data with own annotations'
7772	0	10000	0	0	null	null	'hi'	'user data with foreign annotations'
\.

COPY projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) FROM stdin with null as 'null';
8081	0	0	0	0	null	null	7071	9091
8082	0	0	0	0	null	null	7072	9091
8083	0	0	0	0	null	null	7071	9092
8084	0	0	0	0	null	null	7072	9092
8881	0	0	0	0	null	null	7771	9991
8882	0	0	0	0	null	null	7772	9991
8883	0	0	0	0	null	null	7771	9992
8884	0	0	0	0	null	null	7772	9992
\.

COPY image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, "position", activepixels, acquisition, setup, condition) FROM stdin with null as 'null';
5050	0	0	0	0	null	null	040305_ABCF-GFP01_R3D_D3D.dv		null	null	null	null	null	null
5051	0	0	0	0	null	null	040305_ABCF-GFP01_R3D_D3D.dv		null	null	null	null	null	null
5052	0	0	0	0	null	null	040305_ABCF-GFP01_R3D_D3D.dv		null	null	null	null	null	null
5550	0	10000	0	0	null	null	040305_ABCF-GFP01_R3D_D3D.dv		null	null	null	null	null	null
5551	0	10000	0	0	null	null	040305_ABCF-GFP01_R3D_D3D.dv		null	null	null	null	null	null
5552	0	10000	0	0	null	null	040305_ABCF-GFP01_R3D_D3D.dv		null	null	null	null	null	null
\.

COPY datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, parent, child) FROM stdin with null as 'null';
6061	0	0	0	0	null	null	7071	5051
6062	0	10000	0	0	null	null	7072	5051
6063	0	0	0	0	null	null	7071	5052
6064	0	10000	0	0	null	null	7072	5052
6661	0	0	0	0	null	null	7771	5551
6662	0	10000	0	0	null	null	7772	5551
6663	0	0	0	0	null	null	7771	5552
6664	0	10000	0	0	null	null	7772	5552
\.

COPY datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) FROM stdin with null as 'null';
4041	0	0	0	0	null	null	7071	'roots annotation'
4442	0	10000	0	0	null	null	7072	'user annotation'
4043	0	0	0	0	null	null	7771	'roots annotation'
4444	0	10000	0	0	null	null	7772	'user annotation'
\.

COPY imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, image, content) FROM stdin with null as 'null';
3031	0	0	0	0	null	null	5051	'roots annotation'
3332	0	10000	0	0	null	null	5052	'user annotation'
3033	0	0	0	0	null	null	5551	'roots annotation'
3334	0	10000	0	0	null	null	5552	'user annotation'
\.
commit;
