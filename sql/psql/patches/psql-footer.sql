--
-- Copyright 2006-2016 University of Dundee. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR, increment int4) RETURNS INT8 AS '
DECLARE
      Lid  int4;
      nv   int8;
BEGIN
      SELECT id INTO Lid FROM _lock_ids WHERE name = seq;
      IF Lid IS NULL THEN
          SELECT INTO Lid nextval(''_lock_seq'');
          INSERT INTO _lock_ids (id, name) VALUES (Lid, seq);
      END IF;

      BEGIN
        PERFORM pg_advisory_lock(1, Lid);
      EXCEPTION
        WHEN undefined_function THEN
          RAISE DEBUG ''No function pg_advisory_lock'';
      END;
      PERFORM nextval(seq) FROM generate_series(1, increment);
      SELECT currval(seq) INTO nv;
      BEGIN
        PERFORM pg_advisory_unlock(1, Lid);
      EXCEPTION
        WHEN undefined_function THEN
          RAISE DEBUG ''No function pg_advisory_unlock'';
      END;

      RETURN nv;

END;' LANGUAGE plpgsql;
