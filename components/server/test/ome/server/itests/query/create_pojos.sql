begin;

-- USERS 
INSERT INTO experimenter (id, "version", permissions, firstname, middlename, omename, institution, email, lastname) VALUES (10000, 0, NULL, 'Mr.', NULL, 'tester', NULL, 'blah', 'Allen');
INSERT INTO experimentergroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, description, name) VALUES (10000, 0, 0, 0, 0, NULL, NULL, NULL, 'foo');
INSERT INTO groupexperimentermap (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (10000, 0, 0, 0, 0, NULL, NULL, 10000, 10000);

-- PDI
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9090, 0, 0, 0, 0, NULL, NULL, 'hi'' ', 'root project without links');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9091, 0, 0, 0, 0, NULL, NULL, 'hi', 'root project with own annotations');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9092, 0, 0, 0, 0, NULL, NULL, 'hi', 'root project with foreign annotations');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9990, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user project without links');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9991, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user project with own annotations');
INSERT INTO project (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9992, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user project with foreign annotations');

INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7070, 0, 0, 0, 0, NULL, NULL, 'hi'' ', 'root data without links');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7071, 0, 0, 0, 0, NULL, NULL, 'hi', 'root data with own annotations');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7072, 0, 0, 0, 0, NULL, NULL, 'hi', 'root data with foreign annotations');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7770, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user data without links');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7771, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user data with own annotations');
INSERT INTO dataset (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7772, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user data with foreign annotations');

INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8081, 0, 0, 0, 0, NULL, NULL, 7071, 9091);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8082, 0, 0, 0, 0, NULL, NULL, 7072, 9091);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8083, 0, 0, 0, 0, NULL, NULL, 7071, 9092);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8084, 0, 0, 0, 0, NULL, NULL, 7072, 9092);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8881, 0, 0, 0, 0, NULL, NULL, 7771, 9991);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8882, 0, 0, 0, 0, NULL, NULL, 7772, 9991);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8883, 0, 0, 0, 0, NULL, NULL, 7771, 9992);
INSERT INTO projectdatasetlink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8884, 0, 0, 0, 0, NULL, NULL, 7772, 9992);

INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5050, 0, 0, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5051, 0, 0, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5052, 0, 0, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5550, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5551, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5552, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
-- (for CGCPaths)
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5580, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5581, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5582, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5583, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5584, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5585, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5586, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5587, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO image (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description, context, setup, acquisition, "position", condition) VALUES (5588, 0, 10000, 0, 0, NULL, NULL, '040305_ABCF-GFP01_R3D_D3D.dv', '', NULL, NULL, NULL, NULL, NULL);

INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6061, 0, 0, 0, 0, NULL, NULL, 5051, 7071);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6062, 0, 10000, 0, 0, NULL, NULL, 5051, 7072);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6063, 0, 0, 0, 0, NULL, NULL, 5052, 7071);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6064, 0, 10000, 0, 0, NULL, NULL, 5052, 7072);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6661, 0, 0, 0, 0, NULL, NULL, 5551, 7771);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6662, 0, 10000, 0, 0, NULL, NULL, 5551, 7772);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6663, 0, 0, 0, 0, NULL, NULL, 5552, 7771);
INSERT INTO datasetimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6664, 0, 10000, 0, 0, NULL, NULL, 5552, 7772);

-- ANNOTATIONS
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4041, 0, 0, 0, 0, NULL, NULL, 7071, 'roots annotation');
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4442, 0, 10000, 0, 0, NULL, NULL, 7072, 'user annotation');
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4043, 0, 0, 0, 0, NULL, NULL, 7771, 'roots annotation');
INSERT INTO datasetannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, dataset, content) VALUES (4444, 0, 10000, 0, 0, NULL, NULL, 7772, 'user annotation');

INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3031, 0, 0, 0, 0, NULL, NULL, 'roots annotation', 5051);
INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3332, 0, 10000, 0, 0, NULL, NULL, 'user annotation', 5052);
INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3033, 0, 0, 0, 0, NULL, NULL, 'roots annotation', 5551);
INSERT INTO imageannotation (id, "version", owner_id, group_id, creation_id, update_id, permissions, content, image) VALUES (3334, 0, 10000, 0, 0, NULL, NULL, 'user annotation', 5552);

-- CGCI 
-- CategeoryGroup: 9***
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9090, 0, 0, 0, 0, NULL, NULL, 'hi'' ', 'root categorygroup without links');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9091, 0, 0, 0, 0, NULL, NULL, 'hi', 'root categorygroup with own annotations');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9092, 0, 0, 0, 0, NULL, NULL, 'hi', 'root categorygroup with foreign annotations');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9990, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user categorygroup without links');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9991, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user categorygroup with own annotations');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9992, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user categorygroup with foreign annotations');
-- (for CGCPaths) 9*8*
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9980, 0, 10000, 0, 0, NULL, NULL, 'hi', 'empty categorygroup');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9981, 0, 10000, 0, 0, NULL, NULL, 'hi', 'categorygroup with one category');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9982, 0, 10000, 0, 0, NULL, NULL, 'hi', 'categorygroup with another category');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9983, 0, 10000, 0, 0, NULL, NULL, 'hi', 'categorygroup with two categories');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9984, 0, 10000, 0, 0, NULL, NULL, 'hi', 'categorygroup with two different categories');
INSERT INTO categorygroup (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (9985, 0, 10000, 0, 0, NULL, NULL, 'hi', 'categorygroup with one empty category');

-- Category: 7***
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7070, 0, 0, 0, 0, NULL, NULL, 'hi'' ', 'root category without links');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7071, 0, 0, 0, 0, NULL, NULL, 'hi', 'root category with own annotations');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7072, 0, 0, 0, 0, NULL, NULL, 'hi', 'root category with foreign annotations');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7770, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category without links');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7771, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category with own annotations');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7772, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category with foreign annotations');
-- (for CGCPaths 7*8*)
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7780, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category alone');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7781, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category alone in cg');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7782, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category alone in another cg');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7783, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category paired in a cg I');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7784, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category paired in a cg II');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7785, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category paired in another cg I');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7786, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category paired in another cg II');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7787, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category WITH an image.         ');
INSERT INTO category (id, "version", owner_id, group_id, creation_id, update_id, permissions, name, description) VALUES (7788, 0, 10000, 0, 0, NULL, NULL, 'hi', 'user category WITHOUT an image.      ');

INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8081, 0, 0, 0, 0, NULL, NULL, 7071, 9091);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8082, 0, 0, 0, 0, NULL, NULL, 7072, 9091);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8083, 0, 0, 0, 0, NULL, NULL, 7071, 9092);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8084, 0, 0, 0, 0, NULL, NULL, 7072, 9092);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8881, 0, 0, 0, 0, NULL, NULL, 7771, 9991);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8882, 0, 0, 0, 0, NULL, NULL, 7772, 9991);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8883, 0, 0, 0, 0, NULL, NULL, 7771, 9992);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8884, 0, 0, 0, 0, NULL, NULL, 7772, 9992);
-- (for CGCPaths)
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8885, 0, 0, 0, 0, NULL, NULL, 7781, 9981);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8886, 0, 0, 0, 0, NULL, NULL, 7782, 9982);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8887, 0, 0, 0, 0, NULL, NULL, 7783, 9983);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8888, 0, 0, 0, 0, NULL, NULL, 7784, 9983);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8889, 0, 0, 0, 0, NULL, NULL, 7785, 9984);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8890, 0, 0, 0, 0, NULL, NULL, 7786, 9984);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8891, 0, 0, 0, 0, NULL, NULL, 7787, 9985);
INSERT INTO categorygroupcategorylink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (8892, 0, 0, 0, 0, NULL, NULL, 7788, 9985);

INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6061, 0, 0, 0, 0, NULL, NULL, 5051, 7071);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6062, 0, 10000, 0, 0, NULL, NULL, 5051, 7072);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6063, 0, 0, 0, 0, NULL, NULL, 5052, 7071);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6064, 0, 10000, 0, 0, NULL, NULL, 5052, 7072);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6661, 0, 0, 0, 0, NULL, NULL, 5551, 7771);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6662, 0, 10000, 0, 0, NULL, NULL, 5551, 7772);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6663, 0, 0, 0, 0, NULL, NULL, 5552, 7771);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6664, 0, 10000, 0, 0, NULL, NULL, 5552, 7772);
-- (for CGCPaths)
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6665, 0, 10000, 0, 0, NULL, NULL, 5580, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6666, 0, 10000, 0, 0, NULL, NULL, 5581, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6667, 0, 10000, 0, 0, NULL, NULL, 5582, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6668, 0, 10000, 0, 0, NULL, NULL, 5583, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6669, 0, 10000, 0, 0, NULL, NULL, 5584, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6670, 0, 10000, 0, 0, NULL, NULL, 5585, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6671, 0, 10000, 0, 0, NULL, NULL, 5586, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6672, 0, 10000, 0, 0, NULL, NULL, 5587, 7782);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6673, 0, 10000, 0, 0, NULL, NULL, 5580, 7783);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6674, 0, 10000, 0, 0, NULL, NULL, 5581, 7783);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6675, 0, 10000, 0, 0, NULL, NULL, 5582, 7784);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6676, 0, 10000, 0, 0, NULL, NULL, 5583, 7784);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6677, 0, 10000, 0, 0, NULL, NULL, 5584, 7785);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6678, 0, 10000, 0, 0, NULL, NULL, 5585, 7785);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6679, 0, 10000, 0, 0, NULL, NULL, 5586, 7786);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6680, 0, 10000, 0, 0, NULL, NULL, 5587, 7786);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6681, 0, 10000, 0, 0, NULL, NULL, 5580, 7786);
INSERT INTO categoryimagelink (id, "version", owner_id, group_id, creation_id, update_id, permissions, child, parent) VALUES (6682, 0, 10000, 0, 0, NULL, NULL, 5588, 7787);
commit;
