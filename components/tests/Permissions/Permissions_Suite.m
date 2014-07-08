%Init
Clear all;Close all;

%Sequence of scripts : Each of these can be independently run as well

%Check TagAnnoations
resvec_tag = Permissions_Test_Annotations('tag');

%Check FileAnnotations
resvec_file = Permissions_Test_Annotations('file');

%Check CommentAnnotations
resvec_comment = Permissions_Test_Annotations('comment');

%Check XmlAnnoations
resvec_xml = Permissions_Test_Annotations('xml');

%Check move
[Move_level1,Move_level2,Move_level3]= permissions_test_move;