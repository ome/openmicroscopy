package pojos;

import java.util.Iterator;
import java.util.List;

import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.NamedValue;

@SuppressWarnings("unchecked")
public class MapAnnotationData extends AnnotationData {

	/**
	 * The name space used to identify MapAnnotations created be the user
	 */
	public static final String NS_CLIENT_CREATED = omero.constants.metadata.NSCLIENTMAPANNOTATION.value;

	public MapAnnotationData(MapAnnotation value) {
		super(value);
	}

	public MapAnnotationData() {
		super(MapAnnotationI.class);
	}

	@Override
	public void setContent(Object content) {
		MapAnnotation anno = (MapAnnotation) asAnnotation();
		anno.setMapValue((List<NamedValue>) content);
		setDirty(true);
	}

	@Override
	public Object getContent() {
		MapAnnotation anno = (MapAnnotation) asAnnotation();
		return anno.getMapValue();
	}

	@Override
	public String getContentAsString() {
		List<NamedValue> data = (List<NamedValue>) getContent();
		StringBuilder sb = new StringBuilder();
		Iterator<NamedValue> it = data.iterator();
		while (it.hasNext()) {
			NamedValue next = it.next();
			sb.append(next.name + "=" + next.value);
			if (it.hasNext())
				sb.append(';');
		}
		return sb.toString();
	}

}
