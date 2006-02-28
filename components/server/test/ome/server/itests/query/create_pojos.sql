begin;

-- USERS 
INSERT INTO experimenter (id, "version", permissions, firstname, middlename, omename, institution, email, lastname) VALUES (10000, 0, NULL, 'Mr.', NULL, 'tester', NULL, 'blah', 'Allen');
INSERT INTO experimentergroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, description, name) VALUES (10000, 0, 0, 0, 0, NULL, NULL, NULL, 'foo');
INSERT INTO groupexperimentermap (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (10000, 0, 0, 0, 0, NULL, NULL, 10000, 10000);

-- PDI
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9090, 0, 0, 0, 0, NULL, NULL, '''hi'' ', '''root project without links''');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9091, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root project with own annotations''');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9092, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root project with foreign annotations''');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9990, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user project without links''');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9991, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user project with own annotations''');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9992, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user project with foreign annotations''');

INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7070, 0, 0, 0, 0, NULL, NULL, '''hi'' ', '''root data without links''');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7071, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root data with own annotations''');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7072, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root data with foreign annotations''');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7770, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user data without links''');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7771, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user data with own annotations''');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7772, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user data with foreign annotations''');

INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8081, 0, 0, 0, 0, NULL, NULL, 7071, 9091);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8082, 0, 0, 0, 0, NULL, NULL, 7072, 9091);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8083, 0, 0, 0, 0, NULL, NULL, 7071, 9092);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8084, 0, 0, 0, 0, NULL, NULL, 7072, 9092);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8881, 0, 0, 0, 0, NULL, NULL, 7771, 9991);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8882, 0, 0, 0, 0, NULL, NULL, 7772, 9991);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8883, 0, 0, 0, 0, NULL, NULL, 7771, 9992);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8884, 0, 0, 0, 0, NULL, NULL, 7772, 9992);

INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", activepixels, condition) VALUES (5050, 0, 0, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", activepixels, condition) VALUES (5051, 0, 0, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", activepixels, condition) VALUES (5052, 0, 0, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", activepixels, condition) VALUES (5550, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", activepixels, condition) VALUES (5551, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", activepixels, condition) VALUES (5552, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6061, 0, 0, 0, 0, NULL, NULL, 5051, 7071);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6062, 0, 10000, 0, 0, NULL, NULL, 5051, 7072);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6063, 0, 0, 0, 0, NULL, NULL, 5052, 7071);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6064, 0, 10000, 0, 0, NULL, NULL, 5052, 7072);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6661, 0, 0, 0, 0, NULL, NULL, 5551, 7771);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6662, 0, 10000, 0, 0, NULL, NULL, 5551, 7772);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6663, 0, 0, 0, 0, NULL, NULL, 5552, 7771);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6664, 0, 10000, 0, 0, NULL, NULL, 5552, 7772);

-- ANNOTATIONS
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4041, 0, 0, 0, 0, NULL, NULL, 7071, '''roots annotation''');
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4442, 0, 10000, 0, 0, NULL, NULL, 7072, '''user annotation''');
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4043, 0, 0, 0, 0, NULL, NULL, 7771, '''roots annotation''');
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4444, 0, 10000, 0, 0, NULL, NULL, 7772, '''user annotation''');

INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3031, 0, 0, 0, 0, NULL, NULL, '''roots annotation''', 5051);
INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3332, 0, 10000, 0, 0, NULL, NULL, '''user annotation''', 5052);
INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3033, 0, 0, 0, 0, NULL, NULL, '''roots annotation''', 5551);
INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3334, 0, 10000, 0, 0, NULL, NULL, '''user annotation''', 5552);

-- CGCI
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9090, 0, 0, 0, 0, NULL, NULL, '''hi'' ', '''root categorygroup without links''');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9091, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root categorygroup with own annotations''');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9092, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root categorygroup with foreign annotations''');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9990, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user categorygroup without links''');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9991, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user categorygroup with own annotations''');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9992, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user categorygroup with foreign annotations''');

INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7070, 0, 0, 0, 0, NULL, NULL, '''hi'' ', '''root category without links''');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7071, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root category with own annotations''');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7072, 0, 0, 0, 0, NULL, NULL, '''hi''', '''root category with foreign annotations''');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7770, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user category without links''');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7771, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user category with own annotations''');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7772, 0, 10000, 0, 0, NULL, NULL, '''hi''', '''user category with foreign annotations''');

INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8081, 0, 0, 0, 0, NULL, NULL, 7071, 9091);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8082, 0, 0, 0, 0, NULL, NULL, 7072, 9091);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8083, 0, 0, 0, 0, NULL, NULL, 7071, 9092);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8084, 0, 0, 0, 0, NULL, NULL, 7072, 9092);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8881, 0, 0, 0, 0, NULL, NULL, 7771, 9991);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8882, 0, 0, 0, 0, NULL, NULL, 7772, 9991);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8883, 0, 0, 0, 0, NULL, NULL, 7771, 9992);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8884, 0, 0, 0, 0, NULL, NULL, 7772, 9992);

INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6061, 0, 0, 0, 0, NULL, NULL, 5051, 7071);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6062, 0, 10000, 0, 0, NULL, NULL, 5051, 7072);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6063, 0, 0, 0, 0, NULL, NULL, 5052, 7071);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6064, 0, 10000, 0, 0, NULL, NULL, 5052, 7072);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6661, 0, 0, 0, 0, NULL, NULL, 5551, 7771);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6662, 0, 10000, 0, 0, NULL, NULL, 5551, 7772);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6663, 0, 0, 0, 0, NULL, NULL, 5552, 7771);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6664, 0, 10000, 0, 0, NULL, NULL, 5552, 7772);


commit;
