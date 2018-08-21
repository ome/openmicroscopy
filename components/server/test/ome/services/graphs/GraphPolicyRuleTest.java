/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.graphs;

import java.util.List;
import java.util.Set;

import ome.model.IAnnotated;
import ome.model.IAnnotationLink;
import ome.model.IEnum;
import ome.model.IGlobal;
import ome.model.ILink;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Detector;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filament;
import ome.model.acquisition.Filter;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.FilterSetEmissionFilterLink;
import ome.model.acquisition.FilterSetExcitationFilterLink;
import ome.model.acquisition.GenericExcitationSource;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightEmittingDiode;
import ome.model.acquisition.LightPath;
import ome.model.acquisition.LightPathEmissionFilterLink;
import ome.model.acquisition.LightPathExcitationFilterLink;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.Microscope;
import ome.model.acquisition.OTF;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.acquisition.StageLabel;
import ome.model.acquisition.TransmittanceRange;
import ome.model.annotations.Annotation;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.BasicAnnotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.ChannelAnnotationLink;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.DetectorAnnotationLink;
import ome.model.annotations.DichroicAnnotationLink;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.ExperimenterAnnotationLink;
import ome.model.annotations.ExperimenterGroupAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.FilesetAnnotationLink;
import ome.model.annotations.FilterAnnotationLink;
import ome.model.annotations.FolderAnnotationLink;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.InstrumentAnnotationLink;
import ome.model.annotations.LightPathAnnotationLink;
import ome.model.annotations.LightSourceAnnotationLink;
import ome.model.annotations.ListAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.MapAnnotation;
import ome.model.annotations.NamespaceAnnotationLink;
import ome.model.annotations.NodeAnnotationLink;
import ome.model.annotations.NumericAnnotation;
import ome.model.annotations.ObjectiveAnnotationLink;
import ome.model.annotations.OriginalFileAnnotationLink;
import ome.model.annotations.PlaneInfoAnnotationLink;
import ome.model.annotations.PlateAcquisitionAnnotationLink;
import ome.model.annotations.PlateAnnotationLink;
import ome.model.annotations.ProjectAnnotationLink;
import ome.model.annotations.ReagentAnnotationLink;
import ome.model.annotations.RoiAnnotationLink;
import ome.model.annotations.ScreenAnnotationLink;
import ome.model.annotations.SessionAnnotationLink;
import ome.model.annotations.ShapeAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TermAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.TimestampAnnotation;
import ome.model.annotations.TypeAnnotation;
import ome.model.annotations.WellAnnotationLink;
import ome.model.annotations.XmlAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Folder;
import ome.model.containers.FolderImageLink;
import ome.model.containers.FolderRoiLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PixelsOriginalFileMap;
import ome.model.core.PlaneInfo;
import ome.model.display.ChannelBinding;
import ome.model.display.CodomainMapContext;
import ome.model.display.ContrastStretchingContext;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.ProjectionDef;
import ome.model.display.QuantumDef;
import ome.model.display.RenderingDef;
import ome.model.display.ReverseIntensityContext;
import ome.model.display.Thumbnail;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.AdminPrivilege;
import ome.model.enums.ArcType;
import ome.model.enums.Binning;
import ome.model.enums.ChecksumAlgorithm;
import ome.model.enums.ContrastMethod;
import ome.model.enums.Correction;
import ome.model.enums.DetectorType;
import ome.model.enums.DimensionOrder;
import ome.model.enums.EventType;
import ome.model.enums.ExperimentType;
import ome.model.enums.Family;
import ome.model.enums.FilamentType;
import ome.model.enums.FilterType;
import ome.model.enums.Format;
import ome.model.enums.Illumination;
import ome.model.enums.Immersion;
import ome.model.enums.LaserMedium;
import ome.model.enums.LaserType;
import ome.model.enums.Medium;
import ome.model.enums.MicrobeamManipulationType;
import ome.model.enums.MicroscopeType;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.enums.ProjectionAxis;
import ome.model.enums.ProjectionType;
import ome.model.enums.Pulse;
import ome.model.enums.RenderingModel;
import ome.model.experiment.Experiment;
import ome.model.experiment.MicrobeamManipulation;
import ome.model.fs.Fileset;
import ome.model.fs.FilesetEntry;
import ome.model.fs.FilesetJobLink;
import ome.model.internal.Link;
import ome.model.jobs.ImportJob;
import ome.model.jobs.IndexingJob;
import ome.model.jobs.IntegrityCheckJob;
import ome.model.jobs.Job;
import ome.model.jobs.JobOriginalFileLink;
import ome.model.jobs.JobStatus;
import ome.model.jobs.MetadataImportJob;
import ome.model.jobs.ParseJob;
import ome.model.jobs.PixelDataJob;
import ome.model.jobs.ScriptJob;
import ome.model.jobs.ThumbnailGenerationJob;
import ome.model.jobs.UploadJob;
import ome.model.meta.DBPatch;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.ExternalInfo;
import ome.model.meta.GroupExperimenterMap;
import ome.model.meta.Namespace;
import ome.model.meta.Node;
import ome.model.meta.Session;
import ome.model.meta.Share;
import ome.model.meta.ShareMember;
import ome.model.roi.AffineTransform;
import ome.model.roi.Ellipse;
import ome.model.roi.Label;
import ome.model.roi.Line;
import ome.model.roi.Mask;
import ome.model.roi.Path;
import ome.model.roi.Point;
import ome.model.roi.Polygon;
import ome.model.roi.Polyline;
import ome.model.roi.Rectangle;
import ome.model.roi.Roi;
import ome.model.roi.Shape;
import ome.model.screen.Plate;
import ome.model.screen.PlateAcquisition;
import ome.model.screen.Reagent;
import ome.model.screen.Screen;
import ome.model.screen.ScreenPlateLink;
import ome.model.screen.Well;
import ome.model.screen.WellReagentLink;
import ome.model.screen.WellSample;
import ome.model.stats.StatsInfo;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicy.Action;
import ome.services.graphs.GraphPolicy.Details;
import ome.services.graphs.GraphPolicy.Orphan;
import ome.services.graphs.GraphPolicyRule;
import ome.testing.DataProviderBuilder;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Unit tests for the parsing and matching of graph policy rules from <tt>components/blitz/resources/ome/services/graph-rules/</tt>.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.1
 */
public class GraphPolicyRuleTest {

    private static final ImmutableMap<String, Class<? extends IObject>> modelClassNames;

    private static final GraphPathBean mockGraphPathBean = new GraphPathBean() {
        @Override
        public Class<? extends IObject> getClassForSimpleName(String simpleName) {
            return modelClassNames.get(simpleName);
        }
    };

    static {
        final ImmutableMap.Builder<String, Class<? extends IObject>> builder = ImmutableMap.builder();
        builder.put("AcquisitionMode", AcquisitionMode.class);
        builder.put("AdminPrivilege", AdminPrivilege.class);
        builder.put("AffineTransform", AffineTransform.class);
        builder.put("Annotation", Annotation.class);
        builder.put("AnnotationAnnotationLink", AnnotationAnnotationLink.class);
        builder.put("Arc", Arc.class);
        builder.put("ArcType", ArcType.class);
        builder.put("BasicAnnotation", BasicAnnotation.class);
        builder.put("Binning", Binning.class);
        builder.put("BooleanAnnotation", BooleanAnnotation.class);
        builder.put("ChannelAnnotationLink", ChannelAnnotationLink.class);
        builder.put("ChannelBinding", ChannelBinding.class);
        builder.put("Channel", Channel.class);
        builder.put("ChecksumAlgorithm", ChecksumAlgorithm.class);
        builder.put("CodomainMapContext", CodomainMapContext.class);
        builder.put("CommentAnnotation", CommentAnnotation.class);
        builder.put("ContrastMethod", ContrastMethod.class);
        builder.put("ContrastStretchingContext", ContrastStretchingContext.class);
        builder.put("Correction", Correction.class);
        builder.put("DatasetAnnotationLink", DatasetAnnotationLink.class);
        builder.put("Dataset", Dataset.class);
        builder.put("DatasetImageLink", DatasetImageLink.class);
        builder.put("DBPatch", DBPatch.class);
        builder.put("DetectorAnnotationLink", DetectorAnnotationLink.class);
        builder.put("Detector", Detector.class);
        builder.put("DetectorSettings", DetectorSettings.class);
        builder.put("DetectorType", DetectorType.class);
        builder.put("DichroicAnnotationLink", DichroicAnnotationLink.class);
        builder.put("Dichroic", Dichroic.class);
        builder.put("DimensionOrder", DimensionOrder.class);
        builder.put("DoubleAnnotation", DoubleAnnotation.class);
        builder.put("Ellipse", Ellipse.class);
        builder.put("Event", Event.class);
        builder.put("EventLog", EventLog.class);
        builder.put("EventType", EventType.class);
        builder.put("ExperimenterAnnotationLink", ExperimenterAnnotationLink.class);
        builder.put("Experimenter", Experimenter.class);
        builder.put("ExperimenterGroupAnnotationLink", ExperimenterGroupAnnotationLink.class);
        builder.put("ExperimenterGroup", ExperimenterGroup.class);
        builder.put("Experiment", Experiment.class);
        builder.put("ExperimentType", ExperimentType.class);
        builder.put("ExternalInfo", ExternalInfo.class);
        builder.put("Family", Family.class);
        builder.put("Filament", Filament.class);
        builder.put("FilamentType", FilamentType.class);
        builder.put("FileAnnotation", FileAnnotation.class);
        builder.put("FilesetAnnotationLink", FilesetAnnotationLink.class);
        builder.put("FilesetEntry", FilesetEntry.class);
        builder.put("Fileset", Fileset.class);
        builder.put("FilesetJobLink", FilesetJobLink.class);
        builder.put("FilterAnnotationLink", FilterAnnotationLink.class);
        builder.put("Filter", Filter.class);
        builder.put("FilterSetEmissionFilterLink", FilterSetEmissionFilterLink.class);
        builder.put("FilterSetExcitationFilterLink", FilterSetExcitationFilterLink.class);
        builder.put("FilterSet", FilterSet.class);
        builder.put("FilterType", FilterType.class);
        builder.put("FolderAnnotationLink", FolderAnnotationLink.class);
        builder.put("Folder", Folder.class);
        builder.put("FolderImageLink", FolderImageLink.class);
        builder.put("FolderRoiLink", FolderRoiLink.class);
        builder.put("Format", Format.class);
        builder.put("GenericExcitationSource", GenericExcitationSource.class);
        builder.put("GroupExperimenterMap", GroupExperimenterMap.class);
        builder.put("IAnnotated", IAnnotated.class);
        builder.put("IAnnotationLink", IAnnotationLink.class);
        builder.put("IEnum", IEnum.class);
        builder.put("IGlobal", IGlobal.class);
        builder.put("ILink", ILink.class);
        builder.put("Illumination", Illumination.class);
        builder.put("ImageAnnotationLink", ImageAnnotationLink.class);
        builder.put("Image", Image.class);
        builder.put("ImagingEnvironment", ImagingEnvironment.class);
        builder.put("Immersion", Immersion.class);
        builder.put("ImportJob", ImportJob.class);
        builder.put("IMutable", IMutable.class);
        builder.put("IndexingJob", IndexingJob.class);
        builder.put("InstrumentAnnotationLink", InstrumentAnnotationLink.class);
        builder.put("Instrument", Instrument.class);
        builder.put("IntegrityCheckJob", IntegrityCheckJob.class);
        builder.put("Job", Job.class);
        builder.put("JobOriginalFileLink", JobOriginalFileLink.class);
        builder.put("JobStatus", JobStatus.class);
        builder.put("Label", Label.class);
        builder.put("Laser", Laser.class);
        builder.put("LaserMedium", LaserMedium.class);
        builder.put("LaserType", LaserType.class);
        builder.put("LightEmittingDiode", LightEmittingDiode.class);
        builder.put("LightPathAnnotationLink", LightPathAnnotationLink.class);
        builder.put("LightPathEmissionFilterLink", LightPathEmissionFilterLink.class);
        builder.put("LightPathExcitationFilterLink", LightPathExcitationFilterLink.class);
        builder.put("LightPath", LightPath.class);
        builder.put("LightSettings", LightSettings.class);
        builder.put("LightSourceAnnotationLink", LightSourceAnnotationLink.class);
        builder.put("LightSource", LightSource.class);
        builder.put("Line", Line.class);
        builder.put("Link", Link.class);
        builder.put("ListAnnotation", ListAnnotation.class);
        builder.put("LogicalChannel", LogicalChannel.class);
        builder.put("LongAnnotation", LongAnnotation.class);
        builder.put("MapAnnotation", MapAnnotation.class);
        builder.put("Mask", Mask.class);
        builder.put("Medium", Medium.class);
        builder.put("MetadataImportJob", MetadataImportJob.class);
        builder.put("MicrobeamManipulation", MicrobeamManipulation.class);
        builder.put("MicrobeamManipulationType", MicrobeamManipulationType.class);
        builder.put("Microscope", Microscope.class);
        builder.put("MicroscopeType", MicroscopeType.class);
        builder.put("NamespaceAnnotationLink", NamespaceAnnotationLink.class);
        builder.put("Namespace", Namespace.class);
        builder.put("NodeAnnotationLink", NodeAnnotationLink.class);
        builder.put("Node", Node.class);
        builder.put("NumericAnnotation", NumericAnnotation.class);
        builder.put("ObjectiveAnnotationLink", ObjectiveAnnotationLink.class);
        builder.put("Objective", Objective.class);
        builder.put("ObjectiveSettings", ObjectiveSettings.class);
        builder.put("OriginalFileAnnotationLink", OriginalFileAnnotationLink.class);
        builder.put("OriginalFile", OriginalFile.class);
        builder.put("OTF", OTF.class);
        builder.put("ParseJob", ParseJob.class);
        builder.put("Path", Path.class);
        builder.put("PhotometricInterpretation", PhotometricInterpretation.class);
        builder.put("PixelDataJob", PixelDataJob.class);
        builder.put("PixelsOriginalFileMap", PixelsOriginalFileMap.class);
        builder.put("Pixels", Pixels.class);
        builder.put("PixelsType", PixelsType.class);
        builder.put("PlaneInfoAnnotationLink", PlaneInfoAnnotationLink.class);
        builder.put("PlaneInfo", PlaneInfo.class);
        builder.put("PlaneSlicingContext", PlaneSlicingContext.class);
        builder.put("PlateAcquisitionAnnotationLink", PlateAcquisitionAnnotationLink.class);
        builder.put("PlateAcquisition", PlateAcquisition.class);
        builder.put("PlateAnnotationLink", PlateAnnotationLink.class);
        builder.put("Plate", Plate.class);
        builder.put("Point", Point.class);
        builder.put("Polygon", Polygon.class);
        builder.put("Polyline", Polyline.class);
        builder.put("ProjectAnnotationLink", ProjectAnnotationLink.class);
        builder.put("ProjectDatasetLink", ProjectDatasetLink.class);
        builder.put("ProjectionAxis", ProjectionAxis.class);
        builder.put("ProjectionDef", ProjectionDef.class);
        builder.put("ProjectionType", ProjectionType.class);
        builder.put("Project", Project.class);
        builder.put("Pulse", Pulse.class);
        builder.put("QuantumDef", QuantumDef.class);
        builder.put("ReagentAnnotationLink", ReagentAnnotationLink.class);
        builder.put("Reagent", Reagent.class);
        builder.put("Rectangle", Rectangle.class);
        builder.put("RenderingDef", RenderingDef.class);
        builder.put("RenderingModel", RenderingModel.class);
        builder.put("ReverseIntensityContext", ReverseIntensityContext.class);
        builder.put("RoiAnnotationLink", RoiAnnotationLink.class);
        builder.put("Roi", Roi.class);
        builder.put("ScreenAnnotationLink", ScreenAnnotationLink.class);
        builder.put("ScreenPlateLink", ScreenPlateLink.class);
        builder.put("Screen", Screen.class);
        builder.put("ScriptJob", ScriptJob.class);
        builder.put("SessionAnnotationLink", SessionAnnotationLink.class);
        builder.put("Session", Session.class);
        builder.put("ShapeAnnotationLink", ShapeAnnotationLink.class);
        builder.put("Shape", Shape.class);
        builder.put("ShareMember", ShareMember.class);
        builder.put("Share", Share.class);
        builder.put("StageLabel", StageLabel.class);
        builder.put("StatsInfo", StatsInfo.class);
        builder.put("TagAnnotation", TagAnnotation.class);
        builder.put("TermAnnotation", TermAnnotation.class);
        builder.put("TextAnnotation", TextAnnotation.class);
        builder.put("ThumbnailGenerationJob", ThumbnailGenerationJob.class);
        builder.put("Thumbnail", Thumbnail.class);
        builder.put("TimestampAnnotation", TimestampAnnotation.class);
        builder.put("TransmittanceRange", TransmittanceRange.class);
        builder.put("TypeAnnotation", TypeAnnotation.class);
        builder.put("UploadJob", UploadJob.class);
        builder.put("WellAnnotationLink", WellAnnotationLink.class);
        builder.put("WellReagentLink", WellReagentLink.class);
        builder.put("WellSample", WellSample.class);
        builder.put("Well", Well.class);
        builder.put("XmlAnnotation", XmlAnnotation.class);
        modelClassNames = builder.build();
    }

    /**
     * Convenience class for constructing mock {@link Details} objects.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.4.1
     */
    private static class MockDetails extends Details {
        /**
         * Construct a mock {@link Details} object.
         * @param subject the underlying model object
         * @param action the model object's {@link Action} state
         * @param orphan the model object's {@link Orphan} state
         * @param mayUpdate if the current user may update the model object
         * @param mayDelete if the current user may delete the model object
         * @param mayChmod if the current user may change the model object's permissions
         * @param mayChgrp if the current user may move the model object
         * @param mayChown if the current user may give the model object
         * @param isOwner if the current user owns the model object
         * @param isCheckPermissions if permissions checking is enabled for this model object
         */
        MockDetails(IObject subject, Action action, Orphan orphan,
                boolean mayUpdate, boolean mayDelete, boolean mayChmod, boolean mayChgrp, boolean mayChown,
                boolean isOwner, boolean isCheckPermissions) {
            super(subject, 0L, 0L, action, orphan, mayUpdate, mayDelete, mayChmod, mayChgrp, mayChown,
                    isOwner, isCheckPermissions);
        }

        /**
         * Construct a mock {@link Details} object.
         * @param subject the underlying model object
         * @param ownerId the ID of the model object's owner
         * @param groupId the ID of the model object's group
         * @param action the model object's {@link Action} state
         * @param orphan the model object's {@link Orphan} state
         * @param mayUpdate if the current user may update the model object
         * @param mayDelete if the current user may delete the model object
         * @param mayChmod if the current user may change the model object's permissions
         * @param mayChgrp if the current user may move the model object
         * @param mayChown if the current user may give the model object
         * @param isOwner if the current user owns the model object
         * @param isCheckPermissions if permissions checking is enabled for this model object
         */
        MockDetails(IObject subject, Long ownerId, Long groupId, Action action, Orphan orphan,
                boolean mayUpdate, boolean mayDelete, boolean mayChmod, boolean mayChgrp, boolean mayChown,
                boolean isOwner, boolean isCheckPermissions) {
            super(subject, ownerId, groupId, action, orphan, mayUpdate, mayDelete, mayChmod, mayChgrp, mayChown,
                    isOwner, isCheckPermissions);
        }
    }

    /**
     * Create a new rule that throws an error if it matches.
     * @param matches the string specifying the match condition
     * @return the new rule
     */
    private static GraphPolicyRule newRule(String matches) {
        final GraphPolicyRule rule = new GraphPolicyRule();
        rule.setMatches(matches);
        rule.setError("error");
        return rule;
    }

    /**
     * Create a new rule that can change model object state if it matches.
     * @param matches the string specifying the match condition
     * @param changes the string specifying the conditional state change
     * @return the new rule
     */
    private static GraphPolicyRule newRule(String matches, String changes) {
        final GraphPolicyRule rule = new GraphPolicyRule();
        rule.setMatches(matches);
        rule.setChanges(changes);
        return rule;
    }

    /**
     * Check matches of <tt>I:Image[E]{o}</tt> with negation of <tt>Image</tt>, <tt>E</tt> or <tt>o</tt>
     * against images, datasets and tags of every {@link Action} and {@link Orphan} state.
     * @param object the object to match against
     * @param isObjectNegated if the match should negate the object type
     * @param action the {@link Action} state of the object to match against
     * @param isActionNegated if the match should negate the object's {@link Action} state
     * @param orphan the {@link Orphan} state of the object to match against
     * @param isOrphanNegated if the match should negate the object's {@link Orphan} state
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "simple matches")
    public void testSimpleMatches(IObject object, boolean isObjectNegated, Action action, boolean isActionNegated,
            Orphan orphan, boolean isOrphanNegated) throws GraphException {
        boolean isMatch = (object instanceof Image) != isObjectNegated;
        isMatch = isMatch && ((action == Action.EXCLUDE) != isActionNegated);
        if (action == Action.EXCLUDE) {
            isMatch = isMatch && ((orphan == Orphan.IS_LAST) != isOrphanNegated);
        }
        final Details detailsOld = new MockDetails(object, action, orphan,
                true, true, true, true, true, true, true);
        final StringBuilder matchString = new StringBuilder();
        matchString.append("I:");
        if (isObjectNegated) matchString.append('!');
        matchString.append("Image[");
        if (isActionNegated) matchString.append('!');
        matchString.append("E]");
        if (!isActionNegated) {
            matchString.append("{");
            if (isOrphanNegated) matchString.append('!');
            matchString.append("o}");
        }
        final GraphPolicyRule rule = newRule(matchString.toString(), "I:[I]");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Set<Details> changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(),
                detailsOld,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        Assert.assertEquals(changes.size(), isMatch ? 1 : 0, matchString.toString());
        if (isMatch) {
            final Details detailsNew = changes.iterator().next();
            Assert.assertEquals(detailsNew.subject.getClass(), object.getClass());
            Assert.assertEquals(detailsNew.action, Action.INCLUDE);
        }
    }

    /**
     * Check that bad elements of matches fail parsing.
     * @param isBadName if an object is named the same as a model class
     * @param isBadClass if a nonexistent model class is used
     * @param isBadAction if a nonexistent {@link Action} state is used
     * @param isBadOrphan if a nonexistent {@link Orphan} state is used
     */
    @Test(dataProvider = "nonsense matches")
    public void testNonsenseMatches(Boolean isBadName, Boolean isBadClass, Boolean isBadAction, Boolean isBadOrphan) {
        boolean isExpectException = false;
        final StringBuilder matchString = new StringBuilder();
        if (isBadName != null) {
            matchString.append(isBadName ? "Folder" : "folder");
            matchString.append(':');
            isExpectException |= isBadName;
        }
        if (isBadClass != null) {
            matchString.append(isBadClass ? "Donut" : "Fileset");
            isExpectException |= isBadClass;
        }
        if (isBadAction != null) {
            matchString.append('[');
            matchString.append(isBadAction ? 'X' : 'E');
            matchString.append(']');
            isExpectException |= isBadAction;
        }
        if (isBadOrphan != null) {
            matchString.append('{');
            matchString.append(isBadOrphan ? 'x' : 'i');
            matchString.append('}');
            isExpectException |= isBadOrphan;
        }
        final GraphPolicyRule rule = newRule(matchString.toString());
        try {
            GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
            Assert.assertFalse(isExpectException);
        } catch (GraphException ge) {
            Assert.assertTrue(isExpectException);
        }
    }

    /**
     * Check that error rules trigger exactly when expected.
     * @param isMatch if the rule should match at all
     * @param isTriggerError if the rule should trigger an error condition
     * @param isCheckError if rule review should be done in error-checking mode
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "error rules")
    public void testErrorRules(boolean isMatch, boolean isTriggerError, boolean isCheckError) throws GraphException {
        final Details detailsOld = new MockDetails(new Channel(), isMatch ? Action.EXCLUDE : Action.INCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        GraphPolicyRule rule = null;
        /* collect both cases into same test method for easier contrast */
        if (isTriggerError) {
            final boolean isExpectException = isMatch && isCheckError;
            try {
                rule = newRule("C:Channel[E]");
                final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
                final Set<Details> changes = policy.review(
                        ImmutableMap.<String, Set<Details>>of(),
                        detailsOld,
                        ImmutableMap.<String, Set<Details>>of(),
                        ImmutableSet.<String>of(),
                        isCheckError);
                Assert.assertFalse(isExpectException, rule.toString());
                Assert.assertTrue(changes.isEmpty());
            } catch (GraphException ge) {
                Assert.assertTrue(isExpectException, rule.toString());
                Assert.assertEquals(ge.message, "error");
            }
        } else {
            rule = newRule("C:Channel[E]", "C:[I]");
            final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
            final Set<Details> changes = policy.review(
                    ImmutableMap.<String, Set<Details>>of(),
                    detailsOld,
                    ImmutableMap.<String, Set<Details>>of(),
                    ImmutableSet.<String>of(),
                    isCheckError);
            final boolean isExpectChange = isMatch && !isCheckError;
            Assert.assertEquals(changes.size(), isExpectChange ? 1 : 0);
            if (isExpectChange) {
                final Details detailsNew = changes.iterator().next();
                Assert.assertEquals(detailsNew.subject.getClass(), Channel.class);
                Assert.assertEquals(detailsNew.action, Action.INCLUDE);
            }
        }
    }

    /**
     * Check that multimatch detection triggers with a mutimatch rule.
     * @throws GraphException unexpected
     */
    @Test
    public void testAutomatedMultimatch() throws GraphException {
        final GraphPolicyRule rule = newRule("I:Instrument[E].filter = Filter[I], I.otf = OTF[I]", "I:[I]");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Details detailsInstrument = new MockDetails(new Instrument(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        final Details detailsFilter = new MockDetails(new Filter(), Action.INCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        final Details detailsOTF = new MockDetails(new OTF(), Action.INCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        /* check that reviewing Instrument[E] causes Instrument[I] */
        Set<Details> changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(),
                detailsInstrument,
                ImmutableMap.<String, Set<Details>>of(
                        Instrument.class.getName() + ".filter", ImmutableSet.of(detailsFilter),
                        Instrument.class.getName() + ".otf", ImmutableSet.of(detailsOTF)),
                ImmutableSet.<String>of(),
                false);
        Assert.assertEquals(changes.size(), 1);
        final Details detailsNew = changes.iterator().next();
        Assert.assertEquals(detailsNew.subject.getClass(), Instrument.class);
        Assert.assertEquals(detailsNew.action, Action.INCLUDE);
        /* check that reviewing Filter[I] with Instrument[I] causes nothing */
        changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(Instrument.class.getName() + ".filter", ImmutableSet.of(detailsInstrument)),
                detailsFilter,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        Assert.assertTrue(changes.isEmpty());
        /* check that reviewing Filter[I] with Instrument[E] causes Instrument[E] */
        detailsInstrument.action = Action.EXCLUDE;
        changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(Instrument.class.getName() + ".filter", ImmutableSet.of(detailsInstrument)),
                detailsFilter,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        Assert.assertEquals(changes.size(), 1);
        Assert.assertEquals(detailsNew.subject.getClass(), Instrument.class);
        Assert.assertEquals(detailsNew.action, Action.EXCLUDE);
    }

    /**
     * Check that named condition predicates are honored.
     * @param isConditionNegated if the match should negate the condition
     * @param isConditionSet if the condition should be set for the review
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "named conditions")
    public void testNamedConditions(boolean isConditionNegated, boolean isConditionSet) throws GraphException {
        String ruleCondition = "$ignore_events, E:Event[E]";
        if (isConditionNegated) {
            ruleCondition = "!" + ruleCondition;
        }
        final GraphPolicyRule rule = newRule(ruleCondition, "E:[O]");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        policy.setCondition(isConditionSet ? "ignore_events" : "sunny_day");
        final Details detailsEvent = new MockDetails(new Event(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        final Set<Details> changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(),
                detailsEvent,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        if (isConditionSet == isConditionNegated) {
            Assert.assertTrue(changes.isEmpty());
        } else {
            Assert.assertEquals(changes.size(), 1);
            final Details detailsNew = changes.iterator().next();
            Assert.assertEquals(detailsNew.subject.getClass(), Event.class);
            Assert.assertEquals(detailsNew.action, Action.OUTSIDE);
        }
    }

    /**
     * Check that the action state changes are correctly recognized.
     * @param action the change's action state
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "change action states")
    public void testChangeActionStates(Action action) throws GraphException {
        final char actionLetter;
        switch (action) {
        case EXCLUDE:
            actionLetter = 'E';
            break;
        case DELETE:
            actionLetter = 'D';
            break;
        case INCLUDE:
            actionLetter = 'I';
            break;
        case OUTSIDE:
            actionLetter = 'O';
            break;
        default:
            throw new IllegalArgumentException();
        }
        final GraphPolicyRule rule = newRule("D:Dataset[E]", "D:[" + actionLetter + "]");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Details detailsDataset = new MockDetails(new Dataset(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        final Set<Details> changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(),
                detailsDataset,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        Assert.assertEquals(changes.size(), 1);
        final Details detailsNew = changes.iterator().next();
        Assert.assertEquals(detailsNew.subject.getClass(), Dataset.class);
        Assert.assertEquals(detailsNew.action, action);
    }

    /**
     * Check that the orphan state changes are correctly recognized.
     * @param orphan the change's orphan state
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "change orphan states")
    public void testChangeOrphanStates(Orphan orphan) throws GraphException {
        final char orphanLetter;
        switch (orphan) {
        case IRRELEVANT:
            orphanLetter = 'i';
            break;
        case RELEVANT:
            orphanLetter = 'r';
            break;
        case IS_LAST:
            orphanLetter = 'o';
            break;
        case IS_NOT_LAST:
            orphanLetter = 'a';
            break;
        default:
            throw new IllegalArgumentException();
        }
        final GraphPolicyRule rule = newRule("D:Dataset[E]", "D:{" + orphanLetter + "}");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Details detailsDataset = new MockDetails(new Dataset(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        final Set<Details> changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(),
                detailsDataset,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        Assert.assertEquals(changes.size(), 1);
        final Details detailsNew = changes.iterator().next();
        Assert.assertEquals(detailsNew.subject.getClass(), Dataset.class);
        Assert.assertEquals(detailsNew.orphan, orphan);
    }

    /**
     * Check that the ordering of policy rules is properly respected.
     * @throws GraphException unexpected
     */
    @Test
    public void testRuleOrdering() throws GraphException {
        /* this order is the only of the six possible that results in a final {o} state */
        final List<GraphPolicyRule> rulesOrdered = ImmutableList.of(
                newRule("P:Project{i}", "P:{r}"),
                newRule("P:Project{r}", "P:{o}"),
                newRule("P:Project{o}", "P:{a}"));
        int rightCount = 0;  // checks of correct ordering
        int wrongCount = 0;  // checks of permuted ordering
        for (final List<GraphPolicyRule> rulesShuffle : Collections2.permutations(rulesOrdered)) {
            final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, rulesShuffle);
            final Details detailsProject = new MockDetails(new Project(), Action.EXCLUDE, Orphan.IRRELEVANT,
                    true, true, true, true, true, true, true);
            final Set<Details> changes = policy.review(
                    ImmutableMap.<String, Set<Details>>of(),
                    detailsProject,
                    ImmutableMap.<String, Set<Details>>of(),
                    ImmutableSet.<String>of(),
                    false);
            Assert.assertEquals(changes.size(), 1);
            final Details detailsNew = changes.iterator().next();
            Assert.assertEquals(detailsNew.subject.getClass(), Project.class);
            if (rulesOrdered.equals(rulesShuffle)) {
                Assert.assertEquals(detailsNew.orphan, Orphan.IS_NOT_LAST);
                rightCount++;
            } else {
                Assert.assertNotEquals(detailsNew.orphan, Orphan.IS_NOT_LAST);
                wrongCount++;
            }
        }
        Assert.assertEquals(1, rightCount);
        Assert.assertEquals(5, wrongCount);
    }

    /**
     * Check matching for having {@link GraphPolicy.Ability#UPDATE} over a model object.
     * @param hasAbility if the user has the ability over the model object
     * @param isMatchNegated if the match should negate the requirement for the ability
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "ability matches")
    public void testAbilityMatchesUpdate(boolean hasAbility, Boolean isMatchNegated) throws GraphException {
        final Details detailsPoint = new MockDetails(new Point(), Action.EXCLUDE, Orphan.IRRELEVANT,
                hasAbility, true, true, true, true, true, true);
        testAbilityMatches(hasAbility, isMatchNegated, 'u', detailsPoint);
    }

    /**
     * Check matching for having {@link GraphPolicy.Ability#DELETE} over a model object.
     * @param hasAbility if the user has the ability over the model object
     * @param isMatchNegated if the match should negate the requirement for the ability
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "ability matches")
    public void testAbilityMatchesDelete(boolean hasAbility, Boolean isMatchNegated) throws GraphException {
        final Details detailsPoint = new MockDetails(new Point(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, hasAbility, true, true, true, true, true);
        testAbilityMatches(hasAbility, isMatchNegated, 'd', detailsPoint);
    }

    /**
     * Check matching for having {@link GraphPolicy.Ability#CHGRP} over a model object.
     * @param hasAbility if the user has the ability over the model object
     * @param isMatchNegated if the match should negate the requirement for the ability
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "ability matches")
    public void testAbilityMatchesMove(boolean hasAbility, Boolean isMatchNegated) throws GraphException {
        final Details detailsPoint = new MockDetails(new Point(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, hasAbility, true, true, true);
        testAbilityMatches(hasAbility, isMatchNegated, 'm', detailsPoint);
    }

    /**
     * Check matching for having {@link GraphPolicy.Ability#CHOWN} over a model object.
     * @param hasAbility if the user has the ability over the model object
     * @param isMatchNegated if the match should negate the requirement for the ability
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "ability matches")
    public void testAbilityMatchesGive(boolean hasAbility, Boolean isMatchNegated) throws GraphException {
        final Details detailsPoint = new MockDetails(new Point(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, hasAbility, true, true);
        testAbilityMatches(hasAbility, isMatchNegated, 'g', detailsPoint);
    }

    /**
     * Check matching for having {@link GraphPolicy.Ability#OWN} over a model object.
     * @param hasAbility if the user has the ability over the model object
     * @param isMatchNegated if the match should negate the requirement for the ability
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "ability matches")
    public void testAbilityMatchesOwn(boolean hasAbility, Boolean isMatchNegated) throws GraphException {
        final Details detailsPoint = new MockDetails(new Point(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, hasAbility, true);
        testAbilityMatches(hasAbility, isMatchNegated, 'o', detailsPoint);
    }

    /**
     * Check matching for having a specific ability over a model object.
     * @param hasAbility if the user has the ability over the model object
     * @param isMatchNegated if the match should negate the requirement for the ability
     * @param matchCharacter the character used in the match rule to specify the ability
     * @param detailsPoint details of a {@link Point} that reflect {@code hasAbility}
     * @throws GraphException unexpected
     */
    private static void testAbilityMatches(boolean hasAbility, Boolean isMatchNegated, char matchCharacter, Details detailsPoint)
            throws GraphException {
        final boolean isExpectChange = !Boolean.valueOf(hasAbility).equals(isMatchNegated);
        String match = "S:Shape[E]";
        if (isMatchNegated != null) {
            if (isMatchNegated) {
                match += "/!" + matchCharacter;
            } else {
                match += "/" + matchCharacter;
            }
        }
        final GraphPolicyRule rule = newRule(match, "S:[I]");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Set<Details> changes = policy.review(
                ImmutableMap.<String, Set<Details>>of(),
                detailsPoint,
                ImmutableMap.<String, Set<Details>>of(),
                ImmutableSet.<String>of(),
                false);
        if (isExpectChange) {
            Assert.assertEquals(changes.size(), 1);
            final Details detailsNew = changes.iterator().next();
            Assert.assertEquals(detailsNew.subject.getClass(), Point.class);
            Assert.assertEquals(detailsNew.action, Action.INCLUDE);
        } else {
            Assert.assertTrue(changes.isEmpty());
        }
    }

    /**
     * Check that matching properties tests both property name and direction.
     * @param isMatchingProperty if the property name is correct
     * @param isCorrectDirection if the relationship direction is correct
     * @param isLeftRoot if the left-hand side of the property is the root object for the policy review
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "property matches")
    public void testPropertyMatches(boolean isMatchingProperty, boolean isCorrectDirection, boolean isLeftRoot)
            throws GraphException {
        final GraphPolicyRule rule = newRule("W:Well[E]{r}.plate = [E]", "W:{a}");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Details detailsWell = new MockDetails(new Well(), Action.EXCLUDE, Orphan.RELEVANT,
                true, true, true, true, true, true, true);
        final Details detailsPlate = new MockDetails(new Plate(), Action.EXCLUDE, Orphan.IRRELEVANT,
                true, true, true, true, true, true, true);
        final String propertyName = Well.class.getName() + (isMatchingProperty ? ".plate" : ".name");
        final Set<Details> changes;
        if (isCorrectDirection) {
            if (isLeftRoot) {
                changes = policy.review(
                        ImmutableMap.<String, Set<Details>>of(),
                        detailsWell,
                        ImmutableMap.<String, Set<Details>>of(propertyName, ImmutableSet.of(detailsPlate)),
                        ImmutableSet.<String>of(),
                        false);
            } else {
                changes = policy.review(
                        ImmutableMap.<String, Set<Details>>of(propertyName, ImmutableSet.of(detailsWell)),
                        detailsPlate,
                        ImmutableMap.<String, Set<Details>>of(),
                        ImmutableSet.<String>of(),
                        false);
            }
        } else {
            if (isLeftRoot) {
                changes = policy.review(
                        ImmutableMap.<String, Set<Details>>of(propertyName, ImmutableSet.of(detailsPlate)),
                        detailsWell,
                        ImmutableMap.<String, Set<Details>>of(),
                        ImmutableSet.<String>of(),
                        false);
            } else {
                changes = policy.review(
                        ImmutableMap.<String, Set<Details>>of(),
                        detailsPlate,
                        ImmutableMap.<String, Set<Details>>of(propertyName, ImmutableSet.of(detailsWell)),
                        ImmutableSet.<String>of(),
                        false);
            }
        }
        if (isMatchingProperty && isCorrectDirection) {
            Assert.assertEquals(changes.size(), 1);
            final Details detailsNew = changes.iterator().next();
            Assert.assertEquals(detailsNew.subject.getClass(), Well.class);
            Assert.assertEquals(detailsNew.orphan, Orphan.IS_NOT_LAST);
        } else {
            Assert.assertTrue(changes.isEmpty());
        }
    }

    /**
     * Test the {@code =/o} style of operator in relationship matches.
     * @param isSameOwnership if the two model objects should have the same owner
     * @param isOwnershipNegated if the <q>o</q> of the operator should be negated
     * @throws GraphException unexpected
     */
    @Test(dataProvider = "same ownership matches")
    public void testMatchingOwnership(boolean isSameOwnership, Boolean isOwnershipNegated) throws GraphException {
        final String operator;
        if (isOwnershipNegated == null) {
            operator = "=";
        } else if (isOwnershipNegated) {
            operator = "=/!o";
        } else {
            operator = "=/o";
        }
        final GraphPolicyRule rule = newRule("W:Well[E]{r}.plate " + operator + " [E]", "W:{a}");
        final GraphPolicy policy = GraphPolicyRule.parseRules(mockGraphPathBean, ImmutableSet.<GraphPolicyRule>of(rule));
        final Details detailsWell = new MockDetails(new Well(), 100L, 100L,
                Action.EXCLUDE, Orphan.RELEVANT, true, true, true, true, true, false, true);
        final Details detailsPlate = new MockDetails(new Plate(), isSameOwnership ? 100L : 101L, 100L,
                Action.EXCLUDE, Orphan.IRRELEVANT, true, true, true, true, true, false, true);
        final String propertyName = Well.class.getName() + ".plate";
        final Set<Details> changes = policy.review(
                        ImmutableMap.<String, Set<Details>>of(),
                        detailsWell,
                        ImmutableMap.<String, Set<Details>>of(propertyName, ImmutableSet.of(detailsPlate)),
                        ImmutableSet.<String>of(),
                        false);
        final boolean isExpectChange = !Boolean.valueOf(isSameOwnership).equals(isOwnershipNegated);
        if (isExpectChange) {
            Assert.assertEquals(changes.size(), 1);
            final Details detailsNew = changes.iterator().next();
            Assert.assertEquals(detailsNew.subject.getClass(), Well.class);
            Assert.assertEquals(detailsNew.orphan, Orphan.IS_NOT_LAST);
        } else {
            Assert.assertTrue(changes.isEmpty());
        }
    }

    /**
     * @return test cases for {@link #testSimpleMatches(IObject, boolean, Action, boolean, Orphan, boolean)}
     * @throws ReflectiveOperationException unexpected
     */
    @DataProvider(name = "simple matches")
    public Object[][] provideSimpleMatches() throws ReflectiveOperationException {
        return new DataProviderBuilder()
        .add(ImmutableList.<Class<? extends IObject>>of(Image.class, Dataset.class, TagAnnotation.class)).addBoolean(false)
        .add(Action.class).addBoolean(false).add(Orphan.class).addBoolean(false).build();
    }

    /**
     * @return test cases for {@link #testNonsenseMatches(Boolean, Boolean, Boolean, Boolean)}
     * @throws ReflectiveOperationException unexpected
     */
    @DataProvider(name = "nonsense matches")
    public Object[][] provideNonsenseMatches() throws ReflectiveOperationException {
        return new DataProviderBuilder().addBoolean(true).addBoolean(true).addBoolean(true).addBoolean(true).build();
    }

    /**
     * @return test cases for {@link #testErrorRules(boolean, boolean, boolean)}
     * @throws ReflectiveOperationException unexpected
     */
    @DataProvider(name = "error rules")
    public Object[][] provideErrorRules() throws ReflectiveOperationException {
        return new DataProviderBuilder().addBoolean(false).addBoolean(false).addBoolean(false).build();
    }

    /**
     * @return test cases for {@link #testNamedConditions(boolean, boolean)}
     * @throws ReflectiveOperationException unexpected
     */
    @DataProvider(name = "named conditions")
    public Object[][] provideNamedConditions() throws ReflectiveOperationException {
        return new DataProviderBuilder().addBoolean(false).addBoolean(false).build();
    }

    /**
     * @return test cases for {@link #testChangeActionStates(Action)}
     * @throws ReflectiveOperationException unexpected
     */
    @DataProvider(name = "change action states")
    public Object[][] provideActionStates() throws ReflectiveOperationException {
        return new DataProviderBuilder().add(Action.class).build();
    }

    /**
     * @return test cases for {@link #testChangeOrphanStates(Orphan)}
     * @throws ReflectiveOperationException unexpected
     */
    @DataProvider(name = "change orphan states")
    public Object[][] provideOrphanStates() throws ReflectiveOperationException {
        return new DataProviderBuilder().add(Orphan.class).build();
    }

    /**
     * @return test cases for ability matching tests
     */
    @DataProvider(name = "ability matches")
    public Object[][] provideAbilityMatches() {
        return new DataProviderBuilder().addBoolean(false).addBoolean(true).build();
    }

    /**
     * @return test cases for {@link #testPropertyMatches(boolean, boolean, boolean)}
     */
    @DataProvider(name = "property matches")
    public Object[][] providePropertyMatches() {
        return new DataProviderBuilder().addBoolean(false).addBoolean(false).addBoolean(false).build();
    }

    /**
     * @return test cases for {@link #testMatchingOwnership(boolean, Boolean)}
     */
    @DataProvider(name = "same ownership matches")
    public Object[][] provideMatchingOwnership() {
        return new DataProviderBuilder().addBoolean(false).addBoolean(true).build();
    }
}
