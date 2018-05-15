#!/usr/bin/env python3
# -*- coding: utf-8 -*-

#   .-.                              _____                                  __
#   /v\    L   I   N   U   X       / ____/__   ___   ___   ____ ___  ____  / /_  __  __
#  // \\  >Phear the Penguin<     / / __/ _ \/ __ \/ __ `/ ___/ __ `/ __ \/ __ \/ / / /
# /(   )\                        / /_/ /  __/ /_/ / /_/ / /  / /_/ / /_/ / / / / /_/ /
#  ^^-^^                         \____/\___/\____/\__, /_/   \__,_/ .___/_/ /_/\__, /
#                                                /____/          /_/          /____/


# Written by Paulo Raposo (pauloj.raposo [at] outlook.com) and
# Randall Brown (ranbrown8448 [at] gmail.com) at the Department
# of Geography, University of Tennessee, Knoxville, Spring &
# Summer 2018.


# Imports ///////////////////////////////////////////////////////////////////////////

import os, sys, argparse, csv, math, datetime
import numpy as np
import nvector as nv
from scipy import stats
from osgeo import ogr, osr


# Constants /////////////////////////////////////////////////////////////////////////

desc = """Given one set of QTM facets in a GeoJSON file, and one
CSV file of lat/lon locations with ratio numerical data to be summarized,
this script produces an indentical QTM facets file, except with
statistical summaries for point intersections and descriptive statistics
added, having used the facets to bin the points. Intersection is done
geodesically, on a spherical model of the Earth."""


# Script ////////////////////////////////////////////////////////////////////////////

def checkSpheroidal(aWKTPoint, aWKTPolygon):

    """Checks for point-in-triangle intersection using a spherical
    model of the Earth."""

    # See comments in nvector source: https://github.com/pbrod/Nvector/blob/master/nvector/objects.py
    nvFrame   = nv.FrameE(); # Defaults to WGS84, with flattening.
    nvFrame.f = 0.0 # Zero flattening, makes perfect sphere.

    point   = ogr.CreateGeometryFromWkt(aWKTPoint)
    nvPoint = nvFrame.GeoPoint(float(point.GetY()), float(point.GetX()), degrees=True) # lat then lon

    polygon  = ogr.CreateGeometryFromWkt(aWKTPolygon)
    subGeom  = polygon.GetGeometryRef(0) # Assumes only one outside linear ring!
    subGeomVertices = subGeom.GetPoints()

    # Iterate over polygon vertices to build Great Circle arcs
    # between successive vertices. Keep track of whether the points
    # lie left of the arc path (arcs go CCW as seen from directly above).
    liesLeftList = []
    #
    for vI in range(len(subGeomVertices) - 1): # Don't run on last vertex since there's none that follows it.
        start = subGeomVertices[vI]
        end = subGeomVertices[vI + 1]
        # Below, nvector wants lat then lon. That's the reverse of what OGR stores, x then y.
        pathStartPoint = nvFrame.GeoPoint(float(start[1]), float(start[0]), degrees=True) # lat then lon
        pathEndPoint = nvFrame.GeoPoint(float(end[1]  ), float(end[0]  ), degrees=True) # lat then lon
        gcArc = nv.GeoPath(pathStartPoint, pathEndPoint)
        crossTrackDist = gcArc.cross_track_distance(nvPoint, method='greatcircle').ravel()
        if crossTrackDist <= 0.0:
            liesLeftList.append(True)
        else:
            liesLeftList.append(False)
        iPoint = gcArc.closest_point_on_great_circle(nvPoint)

    intersects = all(liesLeftList) # True if everything in liesLeftList evaluates to True, else False.

    return intersects

def main():

    startTime = datetime.datetime.now()

    # Parse arguments. Take in 4 required parameters and 1 optional parameter.
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('INFACETSGEOJSON', help='GeoJSON file of QTM facets to calculate intersection against points for.')
    parser.add_argument('POINTSCSV', help='CSV file with points to calculate intersection against QTM facets for. Must contain fields for lat and lon coordinates named exactly "latitude" and "longitude", case-specific.')
    parser.add_argument('OUTFACETSGEOJSON', help='Full path for the product QTM GeoJSON file with spatially-binned statistics.')
    parser.add_argument('FIELD', help='Name of the field within the input CSV that statistics will be calculated on. Must be a ratio numerical value in all cells.')
    parser.add_argument('--oi', default=False, action="store_true", help='Write only those facets that have 1 or more point intersect them to the output.')
    args = parser.parse_args()
    inFacetsFilePath = args.INFACETSGEOJSON
    points  = args.POINTSCSV
    outFile = args.OUTFACETSGEOJSON
    inField = args.FIELD
    onlyIntersections = False
    if args.oi:
        onlyIntersections = args.oi

    # Number of intersections over all points and QTM facets.
    synopticIntersectionTotal = 0

    # Dictionary of lists of point tuples, to be indexed by facet ID strings.
    # Entries to this dictionary are only to be made if a polygon is intersected
    # by at least one point.
    pointsContainedByPolygon = {}

    # List to contain 3-tuples, one for each CSV point, of form (lat, lon, attrVal).
    worldStatPoints = []

    # Read QTM facets.
    print("Reading QTM facets from GeoJSON...")
    driver     = ogr.GetDriverByName("GeoJSON")
    dataSource = driver.Open(inFacetsFilePath, 0)
    orig_Layer = dataSource.GetLayer()
    facetCount = orig_Layer.GetFeatureCount()

    # Read CSV file.
    print("Reading points from CSV...")
    with open(points) as csvfile:
        csvreader = csv.reader(csvfile, delimiter=',', quotechar='"') #, dialect=csv.excel)
        headers   = next(csvreader)
        # Find field indexes from CSV file headers.
        latIndex       = headers.index('latitude')
        lonIndex       = headers.index('longitude')
        statFieldIndex = headers.index(inField) # This is the field that is designated from the shell, for statistical calculations.

        # Populate worldStatPoints.
        for row in csvreader:
            lat        = row[latIndex]
            lon        = row[lonIndex]
            statVal    = row[statFieldIndex]
            pointTuple = (float(lat), float(lon), float(statVal)) # NB: lat then lon
            worldStatPoints.append(pointTuple)

    totalPointCount = len(worldStatPoints)

    # Loop through each facet, and then each point, in nested loops, and check intersection.
    print("Beginning intersection tests...")
    facetShadowIndex = 0
    # NB: I had tried to iterate over orig_Layer using this:
    # for fI in range(len(orig_Layer))
    # But that was causing a runtime segmentation fault.
    # Perhaps something in OGR was unhappy about that!
    for facet in orig_Layer:

        facetGeomWKT = facet.GetGeometryRef().ExportToWkt()
        facetID      = facet.GetField("QTMID")

        for pI in range(len(worldStatPoints)):

            # We only proceed to intersection checking if it isn't None.
            if worldStatPoints[pI]:

                thisLat = worldStatPoints[pI][0]
                thisLon = worldStatPoints[pI][1]
                point   = ogr.Geometry(ogr.wkbPoint)
                point.AddPoint(thisLon, thisLat) # lon then lat
                pointGeomWKT = str(point)

                # The intersection check, returns boolean.
                pointIsWithinFacet = checkSpheroidal(pointGeomWKT, facetGeomWKT)

                if pointIsWithinFacet:

                    # Add it to those points in pointsContainedByPolygon keyed by this
                    # facetID value, or create that list in the dictionary under this
                    # facetID value.
                    if facetID in pointsContainedByPolygon.keys():
                        pointsContainedByPolygon[facetID].append(worldStatPoints[pI])
                    else:
                        pointsContainedByPolygon[facetID] = [ worldStatPoints[pI] ]

                    synopticIntersectionTotal += 1

                    # Set this point to None so we don't check it again, since it
                    # won't intersect another facet at this level. Speeds script up.
                    worldStatPoints[pI] = None

        sys.stdout.write("\r")
        prcnt    = ((facetShadowIndex + 1) * 100) / len(orig_Layer)
        prcntRnd = round(prcnt, 1)
        sys.stdout.write("Progress: " + str(facetShadowIndex + 1) + " of " + str(len(orig_Layer)) + " facets, {}%".format(str(prcntRnd)))
        facetShadowIndex += 1

    # Prepare output file.
    wktCoordSys = """GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]"""
    sRef = osr.SpatialReference()
    sRef.ImportFromWkt(wktCoordSys)
    driver = ogr.GetDriverByName('GeoJSON')
    dst_ds = driver.CreateDataSource(outFile)
    fName  = os.path.splitext(os.path.split(outFile)[1])[0]
    dst_layer  = dst_ds.CreateLayer(fName, sRef, geom_type=ogr.wkbPolygon)
    layer_defn = dst_layer.GetLayerDefn()
    #
    idFieldName       = 'QTMID'
    countFieldName    = 'PointCount'
    sumFieldName      = 'Sum'
    modeFieldName     = 'Mode'
    medianFieldName   = 'Median'
    meanFieldName     = 'Mean'
    stDevFieldName    = 'StDev'
    skewFieldName     = 'Skew'
    kurtosisFieldName = 'Kurtosis'
    #
    idField       = ogr.FieldDefn(idFieldName, ogr.OFTString)
    countField    = ogr.FieldDefn(countFieldName, ogr.OFTInteger)
    sumField      = ogr.FieldDefn(sumFieldName, ogr.OFTReal)
    modeField     = ogr.FieldDefn(modeFieldName, ogr.OFTReal)
    medianField   = ogr.FieldDefn(medianFieldName, ogr.OFTReal)
    meanField     = ogr.FieldDefn(meanFieldName, ogr.OFTReal)
    stDevField    = ogr.FieldDefn(stDevFieldName, ogr.OFTReal)
    skewField     = ogr.FieldDefn(skewFieldName, ogr.OFTReal)
    kurtosisField = ogr.FieldDefn(kurtosisFieldName, ogr.OFTReal)
    #
    dst_layer.CreateField(idField)
    dst_layer.CreateField(countField)
    dst_layer.CreateField(sumField)
    dst_layer.CreateField(modeField)
    dst_layer.CreateField(medianField)
    dst_layer.CreateField(meanField)
    dst_layer.CreateField(stDevField)
    dst_layer.CreateField(skewField)
    dst_layer.CreateField(kurtosisField)

    # Reset reading to loop through the input facets again.
    orig_Layer.ResetReading()

    outFacetCount = 0

    # Write features and attribute summaries to output file.
    for feat in orig_Layer:

        thisID = feat.GetField('QTMID')

        # To use the --oi shell argument (see above), we determine whether
        # this facet had any points intersect with it by testing whether
        # an entry to pointsContainedByPolygon was made for it.
        createOutputFacet = True # Default
        if thisID not in pointsContainedByPolygon.keys() and onlyIntersections:
            createOutputFacet = False

        if createOutputFacet:

            feature     = ogr.Feature(layer_defn)
            thisGeom    = feat.GetGeometryRef()
            thisGeomWKT = thisGeom.ExportToWkt()

            # Stats calculations
            #
            # pointCount is zero unless there were intersecting points,
            # in which case it's the length of the list of intersecting
            # points. Similarly, each of the other stats default to
            # zero unless there were points intersecting here.
            #
            pointCount  = 0
            statSum     = 0.0
            statMode    = 0.0
            statMedian  = 0.0
            statMean    = 0.0
            statStDev   = 0.0
            skew        = 0.0
            kurtosis    = 0.0

            if thisID in pointsContainedByPolygon.keys():

                thesePoints = pointsContainedByPolygon[thisID]
                pointCount  = len(thesePoints)
                statValues  = [pointTuple[2] for pointTuple in thesePoints ] # Index 2 is stat value in point tuple above.
                statSum     = sum(statValues)
                statMode    = stats.mode(statValues, axis=None).mode[0] # Stats.mode assumes multiple input arrays, and returns multiple modes; we assume only one. See scipy documentation.
                statMedian  = np.median(statValues)
                statMean    = statSum / len(statValues)
                d           = [ (i - statMean) ** 2 for i in statValues ]
                statStDev   = math.sqrt( sum(d) / len(d) )
                skew        = stats.skew(statValues)
                kurtosis    = stats.kurtosis(statValues)

            feature.SetField(idFieldName, str(thisID))
            feature.SetField(countFieldName, pointCount)
            feature.SetField(sumFieldName, statSum)
            feature.SetField(modeFieldName, statMode)
            feature.SetField(medianFieldName, statMedian)
            feature.SetField(meanFieldName, statMean)
            feature.SetField(stDevFieldName, statStDev)
            feature.SetField(skewFieldName, skew)
            feature.SetField(kurtosisFieldName, kurtosis)

            feature.SetGeometry(ogr.CreateGeometryFromWkt(thisGeomWKT))

            dst_layer.CreateFeature(feature)

            feature.Destroy()  # Destroy the feature to free resources.

            outFacetCount += 1

    dst_ds.Destroy()  # Destroy the data source to free resouces.

    intersectedFacetsCount = len(pointsContainedByPolygon.keys())
    print()
    print("There were {} input points and {} intersections found overall.".format(str(totalPointCount), str(synopticIntersectionTotal)))
    print("There were {} input facets, and {} facets were found to have intersections with points.".format(str(facetCount), str(intersectedFacetsCount)))
    print("The output file contains {} facets.".format(str(outFacetCount)))

    endTime = datetime.datetime.now()
    elapsed = endTime - startTime
    print("Total time taken: " + str(elapsed))

if __name__ == '__main__':
    main()

# fin
# exit()
