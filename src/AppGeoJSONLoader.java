/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
//package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.geojson.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.logging.Level;

/**
 * Utility class to load data from a GeoJSON source into a layer.
 *
 * @author dcollins
 * @version $Id: AppGeoJSONLoader.java 2326 2014-09-17 22:35:45Z dcollins $
 */
public class AppGeoJSONLoader
{
    protected static final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

    /** Create a new loader. */
    public AppGeoJSONLoader()
    {
    }

    /**
     * Parse a GeoJSON document and add it to a layer.
     *
     * @param docSource GeoJSON document. May be a file path {@link String}, {@link java.io.File}, {@link java.net.URL},
     *                  or {@link java.net.URI}.
     * @param layer     layer to receive the new Renderable.
     */
    public void addSourceGeometryToLayer(Object docSource, RenderableLayer layer)
    {
        if (WWUtil.isEmpty(docSource))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GeoJSONDoc doc = null;
        try
        {
            doc = new GeoJSONDoc(docSource);
            doc.parse();

            if (doc.getRootObject() instanceof GeoJSONObject)
            {
                this.addGeoJSONGeometryToLayer((GeoJSONObject) doc.getRootObject(), layer);
                System.out.println("was GeoJSONObject");
            }
            else if (doc.getRootObject() instanceof Object[])
            {
                for (Object o : (Object[]) doc.getRootObject())
                {
                    if (o instanceof GeoJSONObject)
                    {
                        this.addGeoJSONGeometryToLayer((GeoJSONObject) o, layer);
                        System.out.println("Wasn't GeoJSONObject");
                    }
                    else
                    {
                        this.handleUnrecognizedObject(o);
                    }
                }
            }
            else
            {
                this.handleUnrecognizedObject(doc.getRootObject());
            }
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadGeoJSON", docSource);
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
        finally
        {
            WWIO.closeStream(doc, docSource.toString());
        }
    }
    /**
     * Create a layer from a GeoJSON document.
     *
     * @param object GeoJSON object to be added to the layer.
     * @param layer layer to receive the new GeoJSON renderable.
     */
    public void addGeoJSONGeometryToLayer(GeoJSONObject object, RenderableLayer layer)
    {
        if (object == null)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (object.isGeometry()) {
            this.addRenderableForGeometry(object.asGeometry(), layer, null);
            System.out.println("geometry");
        }
        else if (object.isFeature()) {
            this.addRenderableForFeature(object.asFeature(), layer);
            System.out.println("feature");
        }
        else if (object.isFeatureCollection()) {
            this.addRenderableForFeatureCollection(object.asFeatureCollection(), layer);
            System.out.println("feturecollection");
        }
        else {
            this.handleUnrecognizedObject(object);
        }
    }

    /**
     * Create a layer from a GeoJSON document.
     *
     * @param docSource GeoJSON document. May be a file path {@link String}, {@link java.io.File}, {@link java.net.URL},
     *                  or {@link java.net.URI}.
     *
     * @return the new layer.
     */
    public Layer createLayerFromSource(Object docSource)
    {
        if (WWUtil.isEmpty(docSource))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RenderableLayer layer = new RenderableLayer();
        addSourceGeometryToLayer(docSource, layer);

        return layer;
    }

    /**
     * Create a layer from a GeoJSON object.
     *
     * @param object GeoJSON object to use to create a Renderable, which will be added to the new layer.
     *
     * @return the new layer.
     */
    public Layer createLayerFromGeoJSON(GeoJSONObject object)
    {
        if (object == null)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RenderableLayer layer = new RenderableLayer();
        addGeoJSONGeometryToLayer(object, layer);

        return layer;
    }

    protected void handleUnrecognizedObject(Object o)
    {
        Logging.logger().warning(Logging.getMessage("generic.UnrecognizedObjectType", o));
    }

    //**************************************************************//
    //********************  Geometry Conversion  *******************//
    //**************************************************************//

    protected void addRenderableForGeometry(GeoJSONGeometry geom, RenderableLayer layer, AVList properties)
    {
        // Getting at the attributes for this feature:
        Set entries =properties.getEntries();
//        System.out.println(String.valueOf(entries));

//        Double val = (Double) properties.getValue(Main.app.attrToBin);
//        System.out.println("value of " + Main.app.attrToBin + " is " + String.valueOf(val));

        if (geom.isPoint()) {
            this.addRenderableForPoint(geom.asPoint(), layer, properties);
        }
        else if (geom.isMultiPoint()) {
            this.addRenderableForMultiPoint(geom.asMultiPoint(), layer, properties);
        }
        else if (geom.isLineString()) {
            this.addRenderableForLineString(geom.asLineString(), layer, properties);
        }
        else if (geom.isMultiLineString()) {
            this.addRenderableForMutiLineString(geom.asMultiLineString(), layer, properties);
        }
        else if (geom.isPolygon()) {
//            System.out.println("POLY");
            this.addRenderableForPolygon(geom.asPolygon(), layer, properties);
        }
        else if (geom.isMultiPolygon()) {
            this.addRenderableForMultiPolygon(geom.asMultiPolygon(), layer, properties);
        }
        else if (geom.isGeometryCollection()) {
            this.addRenderableForGeometryCollection(geom.asGeometryCollection(), layer, properties);
        }
        else {
            this.handleUnrecognizedObject(geom);
        }
    }

    protected void addRenderableForGeometryCollection(GeoJSONGeometryCollection c, RenderableLayer layer,
        AVList properties)
    {
        if (c.getGeometries() == null || c.getGeometries().length == 0)
            return;

        for (GeoJSONGeometry geom : c.getGeometries())
        {
            this.addRenderableForGeometry(geom, layer, properties);
        }
    }

    protected void addRenderableForFeature(GeoJSONFeature feature, RenderableLayer layer)
    {
        if (feature.getGeometry() == null)
        {
            Logging.logger().warning(Logging.getMessage("nullValue.GeometryIsNull"));
            return;
        }

        this.addRenderableForGeometry(feature.getGeometry(), layer, feature.getProperties());
    }

    protected void addRenderableForFeatureCollection(GeoJSONFeatureCollection c, RenderableLayer layer)
    {
        if (c.getFeatures() != null && c.getFeatures().length == 0)
            return;

        for (GeoJSONFeature feat : c.getFeatures())
        {
            // This is getting called for each of our GeoJSON features (i.e., facets)
            // To get at all the values per feature:
            // System.out.println(feat.getProperties().getValues());
//            try{
//                feat.getProperties().getValue(Main.app.attrToBin);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            System.out.println("damn");

            this.addRenderableForFeature(feat, layer);
        }
    }

    protected void addRenderableForPoint(GeoJSONPoint geom, RenderableLayer layer, AVList properties)
    {
        PointPlacemarkAttributes attrs = this.createPointAttributes(geom, layer);

        layer.addRenderable(this.createPoint(geom, geom.getPosition(), attrs, properties));
    }

    protected void addRenderableForMultiPoint(GeoJSONMultiPoint geom, RenderableLayer layer, AVList properties)
    {
        PointPlacemarkAttributes attrs = this.createPointAttributes(geom, layer);

        for (int i = 0; i < geom.getPointCount(); i++)
        {
            layer.addRenderable(this.createPoint(geom, geom.getPosition(i), attrs, properties));
        }
    }

    protected void addRenderableForLineString(GeoJSONLineString geom, RenderableLayer layer, AVList properties)
    {
        ShapeAttributes attrs = this.createPolylineAttributes(geom, layer);

        layer.addRenderable(this.createPolyline(geom, geom.getCoordinates(), attrs, properties));
    }

    protected void addRenderableForMutiLineString(GeoJSONMultiLineString geom, RenderableLayer layer, AVList properties)
    {
        ShapeAttributes attrs = this.createPolylineAttributes(geom, layer);

        for (GeoJSONPositionArray coords : geom.getCoordinates())
        {
            layer.addRenderable(this.createPolyline(geom, coords, attrs, properties));
        }
    }

    protected void addRenderableForPolygon(GeoJSONPolygon geom, RenderableLayer layer, AVList properties)
    {
//        ShapeAttributes attrs = this.createPolygonAttributes(geom, layer);
        ShapeAttributes attrs = this.createBlankPolygonAttributes();

        if (Main.app.hasBinned == true){
            if (Main.app.attrToBin != null){
                Double thisMean = (Double) properties.getValue("Mean");
                System.out.println(String.valueOf(thisMean));
                if (thisMean > 0.0){
                    System.out.println("tripped the if");
                    attrs.setInteriorMaterial(new Material(Color.GREEN) );
                    attrs.setInteriorOpacity(1.0);
                    attrs.setDrawInterior(true);
                    System.out.println(attrs.getInteriorOpacity());
                }
            }
        }

        layer.addRenderable(this.createPolygon(geom, geom.getExteriorRing(), geom.getInteriorRings(), attrs,
            properties));
    }

    protected void addRenderableForMultiPolygon(GeoJSONMultiPolygon geom, RenderableLayer layer, AVList properties)
    {
        ShapeAttributes attrs = this.createPolygonAttributes(geom, layer);

        for (int i = 0; i < geom.getPolygonCount(); i++)
        {
            layer.addRenderable(
                this.createPolygon(geom, geom.getExteriorRing(i), geom.getInteriorRings(i), attrs, properties));
                System.out.println("MULTIPOLYGON");
        }
    }

    //**************************************************************//
    //********************  Primitive Geometry Construction  *******//
    //**************************************************************//

    @SuppressWarnings( {"UnusedDeclaration"})
    protected Renderable createPoint(GeoJSONGeometry owner, Position pos, PointPlacemarkAttributes attrs,
        AVList properties)
    {
        PointPlacemark p = new PointPlacemark(pos);
        p.setAttributes(attrs);
        if (pos.getAltitude() != 0)
        {
            p.setAltitudeMode(WorldWind.ABSOLUTE);
            p.setLineEnabled(true);
        }
        else
        {
            p.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        }

        if (properties != null)
            p.setValue(AVKey.PROPERTIES, properties);

        return p;
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected Renderable createPolyline(GeoJSONGeometry owner, Iterable<? extends Position> positions,
        ShapeAttributes attrs, AVList properties)
    {
        if (positionsHaveNonzeroAltitude(positions))
        {
            Path p = new Path();
            p.setPositions(positions);
            p.setAltitudeMode(WorldWind.ABSOLUTE);
            p.setAttributes(attrs);

            if (properties != null)
                p.setValue(AVKey.PROPERTIES, properties);

            return p;
        }
        else
        {
            SurfacePolyline sp = new SurfacePolyline(attrs, positions);

            if (properties != null)
                sp.setValue(AVKey.PROPERTIES, properties);

            return sp;
        }
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected Renderable createPolygon(GeoJSONGeometry owner, Iterable<? extends Position> outerBoundary,
        Iterable<? extends Position>[] innerBoundaries, ShapeAttributes attrs, AVList properties)
    {
        if (positionsHaveNonzeroAltitude(outerBoundary))
        {
            Polygon poly = new Polygon(outerBoundary);
            poly.setAttributes(attrs);

            if (innerBoundaries != null)
            {
                for (Iterable<? extends Position> iter : innerBoundaries)
                {
                    poly.addInnerBoundary(iter);
                }
            }

            if (properties != null)
                poly.setValue(AVKey.PROPERTIES, properties);

            return poly;
        }
        else
        {
            System.out.println("Making SurfacePolygon");
            SurfacePolygon poly = new SurfacePolygon(attrs, outerBoundary);

            if (innerBoundaries != null)
            {
                for (Iterable<? extends Position> iter : innerBoundaries)
                {
                    poly.addInnerBoundary(iter);
                }
            }

            if (properties != null)
                poly.setValue(AVKey.PROPERTIES, properties);

            return poly;
        }
    }

    protected static boolean positionsHaveNonzeroAltitude(Iterable<? extends Position> positions)
    {
        for (Position pos : positions)
        {
            if (pos.getAltitude() != 0)
                return true;
        }

        return false;
    }

    //**************************************************************//
    //********************  Attribute Construction  ****************//
    //**************************************************************//

    @SuppressWarnings( {"UnusedDeclaration"})
    protected PointPlacemarkAttributes createPointAttributes(GeoJSONGeometry geom, Layer layer)
    {
        if (layer == null)
            return randomAttrs.nextAttributes().asPointAttributes();

        String key = this.getClass().getName() + ".PointAttributes";
        PointPlacemarkAttributes attrs = (PointPlacemarkAttributes) layer.getValue(key);
        if (attrs == null)
        {
            attrs = randomAttrs.nextAttributes().asPointAttributes();
            layer.setValue(key, attrs);
        }

        return attrs;
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected ShapeAttributes createPolylineAttributes(GeoJSONGeometry geom, Layer layer)
    {
        if (layer == null)
            return randomAttrs.nextAttributes().asShapeAttributes();

        String key = this.getClass().getName() + ".PolylineAttributes";
        ShapeAttributes attrs = (ShapeAttributes) layer.getValue(key);
        if (attrs == null)
        {
            attrs = randomAttrs.nextAttributes().asShapeAttributes();
            layer.setValue(key, attrs);
        }

        return attrs;
    }

    private ShapeAttributes createBlankPolygonAttributes(){
        BasicShapeAttributes bA = new BasicShapeAttributes();
        bA.setInteriorOpacity(0.0);
        bA.setOutlineMaterial(new Material(Color.WHITE));
        return bA;
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    protected ShapeAttributes createPolygonAttributes(GeoJSONGeometry geom, Layer layer)
    {
        if (layer == null)
            return randomAttrs.nextAttributes().asShapeAttributes();

        String key = this.getClass().getName() + ".PolygonAttributes";
        ShapeAttributes attrs = (ShapeAttributes) layer.getValue(key);
        if (attrs == null)
        {
            attrs = randomAttrs.nextAttributes().asShapeAttributes();
            layer.setValue(key, attrs);
        }

//        attrs.setOutlineMaterial(new Material(Color.WHITE));
//        attrs.setInteriorOpacity(0.0);

        return attrs;
    }
}
