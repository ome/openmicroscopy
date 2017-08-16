--
-- This is a manual patch for ome_nextval($1, $2)
-- to prevent deadlocks if nextval(seq) fails in
-- https://github.com/openmicroscopy/openmicroscopy/blob/v5.3.3/sql/psql/OMERO5.3__0/psql-footer.sql#L795
-- pg_advisory_xact_lock should automatically release the lock at the end of
-- the transaction
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

      PERFORM pg_advisory_lock(1, Lid);

      BEGIN
          PERFORM nextval(seq) FROM generate_series(1, increment);
          SELECT currval(seq) INTO nv;
      EXCEPTION
          WHEN OTHERS THEN
              PERFORM pg_advisory_unlock(1, Lid);
          RAISE;
      END;

      PERFORM pg_advisory_unlock(1, Lid);

      RETURN nv;

END;' LANGUAGE plpgsql;
