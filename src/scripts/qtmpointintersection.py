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

import DGGSViewer_script_utilities as su
import os, sys, argparse, csv, math, datetime
import numpy as np
import nvector as nv
from scipy import stats
from osgeo import ogr, osr
from makesingleqtmsublevel import determineOrient


# Constants /////////////////////////////////////////////////////////////////////////

desc = """Given one set of QTM facets in a GeoJSON file, and one
CSV file of lat/lon locations with ratio numerical data to be summarized,
this script produces an indentical QTM facets file, except with
statistical summaries for point intersections and descriptive statistics
added, having used the facets to bin the points. Intersection is done
geodesically, on a spherical model of the Earth."""


# Script ////////////////////////////////////////////////////////////////////////////

def intersectsPlateCarreeBoundingBox(aWKTPoint, aWKTPolygon):

    """Given a WKT point and a WKT polygon, this method assumes they're in the
    same 2D plate carree coordinate system, and tests whether the point is
    within the bounding box (aka envelope) of the polygon, returning a Boolean
    indicating the result."""

    point = ogr.CreateGeometryFromWkt(aWKTPoint)
    ptLat = point.GetY()
    ptLon = point.GetX()

    polygon  = ogr.CreateGeometryFromWkt(aWKTPolygon)
    subGeom  = polygon.GetGeometryRef(0) # Assumes only one outside linear ring!
    subGeomVertices = subGeom.GetPoints()
    latVals = [v[1] for v in subGeomVertices]
    lonVals = [v[0] for v in subGeomVertices]

    withinLatBounds = min(latVals) <= ptLat and max(latVals) >= ptLat
    withinLonBounds = min(lonVals) <= ptLon and max(lonVals) >= ptLon

    intersection = False
    if withinLatBounds and withinLonBounds:
        intersection = True

    return intersection

# Polar ray casting method for intersect. Doesn't work at the moment, needs
# more debugging.
def intersectionTest_PolarRayCastGeodesicPolygon(aWKTPoint, aWKTPolygon):

    intersectNum = 0

    # nv frame and flattening set up
    nvFrame   = nv.FrameE();
    nvFrame.f = 0.0

    point = ogr.CreateGeometryFromWkt(aWKTPoint)

    polygon  = ogr.CreateGeometryFromWkt(aWKTPolygon)
    subGeom  = polygon.GetGeometryRef(0) # Assumes only one outside linear ring!
    subGeomVertices = subGeom.GetPoints()

    # We start asuming we'll cast to the North Pole, change if our
    # point is actually in the northern hemisphere.
    rayCastingToNorthPole = True
    pole = (90.0, 0.0) # lat then lon
    if float(point.GetY()) >= 0.0: # if point is in northern hemisphere
        rayCastingToNorthPole = False
        pole = (-90.0, 0.0) # lat then lon

    nvPoint = nvFrame.GeoPoint(float(point.GetY()), float(point.GetX()), degrees=True) # lat, then lon
    nvPole = nvFrame.GeoPoint(float(pole[0]), float(pole[1]), degrees=True) # lat, then lon
    polePath = nv.GeoPath(nvPoint, nvPole) # This is the path the ray traces to the given pole

    # Iterate through the facet sides
    for i in range(len(subGeomVertices) - 1):
        v1 = subGeomVertices[i]
        v2 = subGeomVertices[i + 1]

        # Skip the polar singularities
        if (v1[1] == v2[1] == 90.0) or (v1[1] == v2[1] == -90.0):
            print("polar singularity skipped")
            continue

        # Test to see whether this is a latitude parallel or not. We treat these
        # two cases differently, since parallel checks are easier in our case.
        if v1[1] == v2[1]:
            # print("working on a parallel")
            # A parallel
            lon1, lon2 = sorted([v1[0], v2[0]]) # sorted longitudes so lon1 < lon2
            parallelLat = v1[1]
            if rayCastingToNorthPole:
                # Point is in southern hemisphere
                if float(point.GetY()) < parallelLat and float(point.GetX()) >= lon1 and float(point.GetX()) <= lon2:
                    intersectNum = intersectNum + 1
                    print("parallel intersection")
                else:
                    print("no parallel intersection")
            else:
                # Point is in northern hemisphere
                if float(point.GetY()) > parallelLat and float(point.GetX()) >= lon1 and float(point.GetX()) <= lon2:
                    intersectNum = intersectNum + 1
                    print("parallel intersection")
                else:
                    print("no parallel intersection")
        else:
            # A non-parallel arc of a great circle
            # To start need to make a path out of the geometry vertices for this arc
            vertStart = nvFrame.GeoPoint(float(v1[1]), float(v1[0]), degrees=True)
            vertEnd = nvFrame.GeoPoint(float(v2[1]), float(v2[0]), degrees=True)
            geomPath = nv.GeoPath(vertStart, vertEnd)

            intersect = polePath.intersect(geomPath)
            if geomPath.on_path(intersect)[0]:
                print("gc arc intersection!")
                intersectNum = intersectNum + 1
            else:
                print("no gc arc intersection")

    print("intersectNum: {}".format(str(intersectNum)))
    if intersectNum == 1:
        return True
    else:
        return False

def intersectionTest_ConvexSurroundingGeodesicPolygon(aWKTPoint, aWKTPolygon, facetID):

    """Checks for point-in-QTM facet intersection using a spherical
    model of the Earth by testing that the point is always to the
    left of each polygon arc (each being an arc of a Great Circle
    on the spherical Earth), as arcs proceed counter-clockwise."""

    # See comments in nvector source: https://github.com/pbrod/Nvector/blob/master/nvector/objects.py
    nvFrame   = nv.FrameE(); # Defaults to WGS84, with flattening.
    nvFrame.f = 0.0 # Zero flattening, makes perfect sphere. Rigorous calculation on ellipsoids are much more involved.

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

        if start[1] == end[1]:
            # A latitude parallel. 'Lies left' is a matter of being higher or lower in
            # latitude, depending on whether the facet orientation is up or down,
            # respectively.
            orient = determineOrient(str(facetID), len(subGeomVertices))

            if orient == "u":
                if point.GetY() >= start[1]:
                    liesLeftList.append(True)
                else:
                    liesLeftList.append(False)

            if orient == "d":
                if point.GetY() <= start[1]:
                    liesLeftList.append(True)
                else:
                    liesLeftList.append(False)
        else:
            # For facet sides that are 'diagonal' great circle arcs.
            #
            # Below, nvector wants lat then lon. That's the reverse of what OGR stores, x then y.
            pathStartPoint = nvFrame.GeoPoint(float(start[1]), float(start[0]), degrees=True) # lat then lon
            pathEndPoint   = nvFrame.GeoPoint(float(end[1]  ), float(end[0]  ), degrees=True) # lat then lon
            gcArc = nv.GeoPath(pathStartPoint, pathEndPoint)
            crossTrackDist = gcArc.cross_track_distance(nvPoint, method='greatcircle').ravel()
            if crossTrackDist <= 0.0:
                liesLeftList.append(True)
            else:
                liesLeftList.append(False)

    intersects = all(liesLeftList) # True if everything in liesLeftList evaluates to True, else False.

    return intersects


def main():

    startTime = datetime.datetime.now()

    # Parse arguments. Take in 4 required parameters and 1 optional parameter.
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('QTMFILE', help='File of QTM facets to calculate intersection against points for.')
    parser.add_argument('POINTSCSV', help='CSV file with points to calculate intersection against QTM facets for. Must contain fields for lat and lon coordinates named exactly "latitude" and "longitude", case-specific.')
    # parser.add_argument('OUTFACETS', help='Full path for the product QTM GeoJSON file with spatially-binned statistics.')
    parser.add_argument('FIELD', help='Name of the field within the input CSV that statistics will be calculated on. Must be a ratio numerical value in all cells.')
    parser.add_argument('--oi', default=False, action="store_true", help='Write only those facets that have 1 or more point intersect them to the output.')
    args = parser.parse_args()
    inFacetsFilePath = args.QTMFILE
    points  = args.POINTSCSV
    # outFile = args.OUTFACETS
    outFile = su.appendSuffixToFileName(inFacetsFilePath, "_agg")
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
    print("Reading QTM facets from input...")
    driver     = su.getDriverByFilepath(inFacetsFilePath)
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

                # The intersection checks. First check the bounding box (cheap and fast rejections),
                # and then test geodetically if there is bounding box intersection.
                if intersectsPlateCarreeBoundingBox(pointGeomWKT, facetGeomWKT):
                    pointIsWithinFacet = intersectionTest_ConvexSurroundingGeodesicPolygon(pointGeomWKT, facetGeomWKT, facetID)

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
    print("Preparing output file...")
    wktCoordSys = su.wktWGS84
    sRef = osr.SpatialReference()
    sRef.ImportFromWkt(wktCoordSys)
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
    print("Calculating statistics in facets...")
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
    print("Had {} input points, {} intersections found.".format(str(totalPointCount), str(synopticIntersectionTotal)))
    print("Had {} input facets, {} facets with point intersection.".format(str(facetCount), str(intersectedFacetsCount)))
    print("Output file contains {} facets.".format(str(outFacetCount)))

    endTime = datetime.datetime.now()
    elapsed = endTime - startTime
    print("Total time taken: " + str(elapsed))

if __name__ == '__main__':
    main()

# fin
# exit()
