# -*- coding: utf-8 -*-

# Randall Brown & Paulo Raposo
# May 2018

#   .-.                              _____                                  __
#   /v\    L   I   N   U   X       / ____/__   ___   ___   ____ ___  ____  / /_  __  __
#  // \\  >Phear the Penguin<     / / __/ _ \/ __ \/ __ `/ ___/ __ `/ __ \/ __ \/ / / /
# /(   )\                        / /_/ /  __/ /_/ / /_/ / /  / /_/ / /_/ / / / / /_/ /
#  ^^-^^                         \____/\___/\____/\__, /_/   \__,_/ .___/_/ /_/\__, /
#                                                /____/          /_/          /____/

# Simple script that takes in a QTM file and shifts the longitude of the file by a user defined amount.

# NB: To avoid a problem in WorldWind (which is where the products of this script are presently meant
# to be used) with having longitudes beyond -180 and 180 degrees, we are clipping the final geometries
# by the 'equirectangular' limits of the lat/lon coordinate system. This means we truncate polygons
# that cross the -180/180 longitude line. Also, we do not currently re-insert the cut-off parts on the
# opposite side of the map.

# TODO: reinsert the cut-off parts of polys at the opposite side of the map?

# Imports ///////////////////////////////////////////////////////////////////////////

from qtmgenerator import constructGeometry
import DGGSViewer_script_utilities as su
import os, argparse
from osgeo import ogr, gdal, osr
gdal.UseExceptions()

# Constants /////////////////////////////////////////////////////////////////////////

# The rectangle that represents valid lat/lon coordinates within bounds.
earthLimitsRing = ogr.Geometry(ogr.wkbLinearRing)
earthLimitsRing.AddPoint(-180.0, -90.0) # sequence: lon, lat (x,y)
earthLimitsRing.AddPoint( 180.0, -90.0) # sequence: lon, lat (x,y)
earthLimitsRing.AddPoint( 180.0,  90.0) # sequence: lon, lat (x,y)
earthLimitsRing.AddPoint(-180.0,  90.0) # sequence: lon, lat (x,y)
earthLimitsRing.AddPoint(-180.0, -90.0) # sequence: lon, lat (x,y)
earthLimitsPolygon = ogr.Geometry(ogr.wkbPolygon)
earthLimitsPolygon.AddGeometry(earthLimitsRing)

idFieldName = "QTMID"

# Script ////////////////////////////////////////////////////////////////////////////////

def main():

    # Parse arguments.
    parser = argparse.ArgumentParser(description='Accepts a QTM file and produces a copy, translated east or west by the given number of geographical degrees.')
    parser.add_argument('QTMFILE', help='The file for the desired QTM level.')
    # parser.add_argument('OUTFILEDIR', help='Full path to output directory for the product QTM shapefiles.')
    parser.add_argument('LONSHIFT', help = 'Number of degrees to shift QTM in longitudinal direction. Positive numbers shift east, negative shift west.')
    args = parser.parse_args()
    qtmFile = args.QTMFILE
    # outFileDir = args.OUTFILEDIR
    theta = float(args.LONSHIFT)

    # Loading input QTM file.
    driver = su.getDriverByFilepath(qtmFile)
    dataSource = driver.Open(qtmFile, 0)
    orig_Layer = dataSource.GetLayer()
    featureCount = orig_Layer.GetFeatureCount()

    # Setup for output file name.
    thetaString = str(theta)
    desiredSuffix = "lonshft" + thetaString
    outFileName = su.appendSuffixToFileName(qtmFile, desiredSuffix)

    # Declaration of lists and dictionaries.
    facetsByQTMID = {}

    # Parses through the geometry of the supplied QTM file.
    for feature in orig_Layer:
        facetIsWithinBounds = True
        aFacet = []
        thisPolyID = feature.GetField("QTMID")
        # This will be a top-most POLYGON defn.
        thisGeom = feature.GetGeometryRef() # This will be a top-most POLYGON (( )) defn.
        # Now we go one level deeper to get the ring defining the polygon. We know
        # there is only one geometry (i.e., the ring) within our QTM polygons, so
        # we go straight to index 0 instead of looping:
        subGeom = thisGeom.GetGeometryRef(0)
        # Pulls vertices from input file and shifts longitude by theta degrees.
        verts = subGeom.GetPoints()
        for v in verts:
            # If any shifted lon point goes beyond 180 E or W, reject this facet.
            proposedNewLon = v[0] + theta
            if proposedNewLon > 180.0 or proposedNewLon < -180.0:
                facetIsWithinBounds = False
            else:
                aFacet.append( (v[1], proposedNewLon) ) # lat then lon, from OGR points which are x then y.

        if facetIsWithinBounds:
            # This last dummy item is appended to the aFacet list because
            # we want to later use qtmgenerator.py's constructGeometry method,
            # which expects a final element indicating facet orientation. It
            # isn't actually used by the method, so it's acceptable here to
            # pass in a dummy variable so the lists are either 5 or 6 elements
            # long, which is what constructGeometry is written to handle.
            # A little kludgy, tho!
            aFacet.append("orientationdummy")

            facetsByQTMID[thisPolyID] = aFacet

    # Sets up the output file.
    sRef = osr.SpatialReference()
    sRef.ImportFromWkt(su.wktWGS84)
    dst_ds = driver.CreateDataSource(outFileName)
    fName = os.path.splitext(os.path.split(outFileName)[1])[0]
    dst_layer = dst_ds.CreateLayer(fName, sRef, geom_type=ogr.wkbPolygon)
    levelFieldName = 'QTMID'
    layer_defn = dst_layer.GetLayerDefn()
    new_field = ogr.FieldDefn(levelFieldName, ogr.OFTInteger) # String?
    dst_layer.CreateField(new_field)

    # Create features and write to file.
    for facetRecordKey in facetsByQTMID.keys():
        facetGeometry = constructGeometry(facetsByQTMID[facetRecordKey])
        # Before creating them, intersect them with earthLimitsPolygon to ensure
        # we don't create features beyond the allowable lat/lon coords. Also
        # ensure there's actually some geometry there; if not, don't write
        # this feature to file.
        # facetGeometryWithinBounds = facetGeometry.Intersection(earthLimitsPolygon)
        # Below, there should be a non-zero count to the intersection geometry.
        # Also, it seems a no-polygon intersection result is of type "GeometryCollection",
        # so could possibly use
        #   facetGeometryWithinBounds.GetGeometryName() != "GeometryCollection"
        #   or
        #   facetGeometryWithinBounds.GetGeometryName() == "Polygon"
        # as a condition too.
        # if facetGeometryWithinBounds.GetGeometryCount():
        if facetGeometry.GetGeometryCount():
            feature = ogr.Feature(layer_defn)
            feature.SetField(levelFieldName, facetRecordKey)
            # feature.SetGeometry(facetGeometryWithinBounds)
            feature.SetGeometry(facetGeometry)
            dst_layer.CreateFeature(feature)
            feature.Destroy()  # Destroy the feature to free resources.

    dst_ds.Destroy()  # Destroy the data source to free resouces.


if __name__ == '__main__':
    main()

# fin
# exit()
