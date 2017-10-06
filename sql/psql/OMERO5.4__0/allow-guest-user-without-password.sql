-- Removes password protection from user with ID 1 and name "guest".

UPDATE password SET hash = ''
 WHERE experimenter_id = 1 AND
       EXISTS (SELECT FROM experimenter
                WHERE id = 1 AND omename = 'guest');
