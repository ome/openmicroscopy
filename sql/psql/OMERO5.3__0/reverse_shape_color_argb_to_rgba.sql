CREATE OR REPLACE FUNCTION unfix_color(color INTEGER) RETURNS INTEGER AS $$

DECLARE
    least_3_bytes CONSTANT INTEGER := (1 << 24) - 1;
    least_1_byte CONSTANT INTEGER := (1 << 8) - 1;
    rgb INTEGER;
    alpha INTEGER;

BEGIN
    rgb := color >> 8 & least_3_bytes;
    alpha := color & least_1_byte;
    RETURN alpha << 24 | rgb;

END;$$ LANGUAGE plpgsql;

--
-- Edit TRUE to apply change to a subset of ROIs
--

UPDATE shape
  SET strokecolor = CASE WHEN strokecolor IS NOT NULL THEN unfix_color(strokecolor) END,
      fillcolor   = CASE WHEN fillcolor   IS NOT NULL THEN unfix_color(fillcolor)   END
  WHERE (strokecolor IS NOT NULL OR
         fillcolor   IS NOT NULL) AND
        TRUE;

DROP FUNCTION unfix_color(INTEGER);
