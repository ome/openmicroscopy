package ome.formats.model;

import java.util.List;

import Ice.Current;
import omero.RBool;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.model.Details;
import omero.model.IObject;
import omero.model.LogicalChannel;
import omero.model.Roi;
import omero.model.Shape;


/**
 * Since the Shape class is abstract, MetaShape is used as a place holder for
 * any metadata from bio-formats before we know specifically what kind of shape
 * we are dealing with (Text, Rect, Mask, Ellipse, Point, Path, Polygon or
 * Line). Once any of the concrete methods are called (for example, 
 * setCircleCX()), any values stored in this temporary object will be copied
 * into the new concrete object.
 * 
 * @author Chris Allan <callan at blackcat.ca>
 *
 */
public class MetaShape extends Shape
{
	public void copyData(Shape shape)
	{
		shape.setFillColor(fillColor);
		shape.setFillOpacity(fillOpacity);
		shape.setFillRule(fillRule);
		shape.setG(g);
		shape.setLocked(locked);
		shape.setStrokeColor(strokeColor);
		shape.setStrokeDashArray(strokeDashArray);
		shape.setStrokeDashOffset(strokeDashOffset);
		shape.setStrokeLineCap(strokeLineCap);
		shape.setStrokeLineJoin(strokeLineJoin);
		shape.setStrokeMiterLimit(strokeMiterLimit);
		shape.setStrokeOpacity(strokeOpacity);
		shape.setStrokeWidth(strokeWidth);
		shape.setTheT(theT);
		shape.setTheZ(theZ);
		shape.setTransform(transform);
		shape.setVectorEffect(vectorEffect);
		shape.setVisibility(visibility);
	}

	public void addAllLogicalChannelSet(List<LogicalChannel> arg0, Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void addLogicalChannel(LogicalChannel arg0, Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void clearChannels(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public List<LogicalChannel> copyChannels(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public RString getFillColor(Current arg0) {
		return fillColor;
	}

	public RFloat getFillOpacity(Current arg0) {
		return fillOpacity;
	}

	public RString getFillRule(Current arg0) {
		return fillRule;
	}

	public RString getG(Current arg0) {
		return g;
	}

	public RBool getLocked(Current arg0) {
		return locked;
	}

	public Roi getRoi(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public RString getStrokeColor(Current arg0) {
		return strokeColor;
	}

	public RString getStrokeDashArray(Current arg0) {
		return strokeDashArray;
	}

	public RInt getStrokeDashOffset(Current arg0) {
		return strokeDashOffset;
	}

	public RString getStrokeLineCap(Current arg0) {
		return strokeLineCap;
	}

	public RString getStrokeLineJoin(Current arg0) {
		return strokeLineJoin;
	}

	public RInt getStrokeMiterLimit(Current arg0) {
		return strokeMiterLimit;
	}

	public RFloat getStrokeOpacity(Current arg0) {
		return strokeOpacity;
	}

	public RInt getStrokeWidth(Current arg0) {
		return strokeWidth;
	}

	public RInt getTheT(Current arg0) {
		return theT;
	}

	public RInt getTheZ(Current arg0) {
		return theZ;
	}

	public RString getTransform(Current arg0) {
		return transform;
	}

	public RString getVectorEffect(Current arg0) {
		return vectorEffect;
	}

	public RInt getVersion(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public RBool getVisibility(Current arg0) {
		return visibility;
	}

	public void reloadChannels(Shape arg0, Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void removeAllLogicalChannelSet(List<LogicalChannel> arg0,
			Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void removeLogicalChannel(LogicalChannel arg0, Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void setFillColor(RString arg0, Current arg1) {
		fillColor = arg0;
	}

	public void setFillOpacity(RFloat arg0, Current arg1) {
		fillOpacity = arg0;
	}

	public void setFillRule(RString arg0, Current arg1) {
		fillRule = arg0;
	}

	public void setG(RString arg0, Current arg1) {
		g = arg0;
	}

	public void setLocked(RBool arg0, Current arg1) {
		locked = arg0;
	}

	public void setRoi(Roi arg0, Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void setStrokeColor(RString arg0, Current arg1) {
		strokeColor = arg0;
	}

	public void setStrokeDashArray(RString arg0, Current arg1) {
		strokeDashArray = arg0;
	}

	public void setStrokeDashOffset(RInt arg0, Current arg1) {
		strokeDashOffset = arg0;
	}

	public void setStrokeLineCap(RString arg0, Current arg1) {
		strokeLineCap = arg0;
	}

	public void setStrokeLineJoin(RString arg0, Current arg1) {
		strokeLineJoin = arg0;
	}

	public void setStrokeMiterLimit(RInt arg0, Current arg1) {
		strokeMiterLimit = arg0;
	}

	public void setStrokeOpacity(RFloat arg0, Current arg1) {
		strokeOpacity = arg0;
	}

	public void setStrokeWidth(RInt arg0, Current arg1) {
		strokeWidth = arg0;
	}

	public void setTheT(RInt arg0, Current arg1) {
		theT = arg0;
	}

	public void setTheZ(RInt arg0, Current arg1) {
		theZ = arg0;
	}

	public void setTransform(RString arg0, Current arg1) {
		transform = arg0;
	}

	public void setVectorEffect(RString arg0, Current arg1) {
		vectorEffect = arg0;
	}

	public void setVersion(RInt arg0, Current arg1) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void setVisibility(RBool arg0, Current arg1) {
		visibility = arg0;
	}

	public int sizeOfChannels(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void unloadChannels(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public Details getDetails(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public RLong getId(Current arg0) {
		return id;
	}

	public boolean isAnnotated(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public boolean isGlobal(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public boolean isLink(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public boolean isLoaded(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public boolean isMutable(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public IObject proxy(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void setId(RLong arg0, Current arg1) {
		id = arg0;
	}

	public IObject shallowCopy(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void unload(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void unloadCollections(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}

	public void unloadDetails(Current arg0) {
		throw new RuntimeException("Not implemented yet.");
	}
 
}
