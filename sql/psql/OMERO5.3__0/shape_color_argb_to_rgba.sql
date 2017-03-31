CREATE OR REPLACE FUNCTION fix_color(color INTEGER) RETURNS INTEGER AS $$

DECLARE
    least_3_bytes CONSTANT INTEGER := (1 << 24) - 1;
    least_1_byte CONSTANT INTEGER := (1 << 8) - 1;
    rgb INTEGER;
    alpha INTEGER;

BEGIN
    rgb := color & least_3_bytes;
    alpha := color >> 24 & least_1_byte;
    IF alpha = 0 THEN
        alpha := least_1_byte;
    END IF;
    RETURN rgb << 8 | alpha;

END;$$ LANGUAGE plpgsql;

--
-- Edit TRUE to apply change to a subset of ROIs
--

UPDATE shape
  SET strokecolor = CASE WHEN strokecolor IS NOT NULL THEN fix_color(strokecolor) END,
      fillcolor   = CASE WHEN fillcolor   IS NOT NULL THEN fix_color(fillcolor)   END
  WHERE (strokecolor IS NOT NULL OR
         fillcolor   IS NOT NULL) AND
        TRUE;

DROP FUNCTION fix_color(INTEGER);
