#!/usr/bin/env python3
# -*- coding: utf-8 -*-

#   .-.                              _____                                  __
#   /v\    L   I   N   U   X       / ____/__   ___   ___   ____ ___  ____  / /_  __  __
#  // \\  >Phear the Penguin<     / / __/ _ \/ __ \/ __ `/ ___/ __ `/ __ \/ __ \/ / / /
# /(   )\                        / /_/ /  __/ /_/ / /_/ / /  / /_/ / /_/ / / / / /_/ /
#  ^^-^^                         \____/\___/\____/\__, /_/   \__,_/ .___/_/ /_/\__, /
#                                                /____/          /_/          /____/


"""
This script...

"""

import sys, os, subprocess, datetime, argparse, logging
import nvector as nv
from osgeo import ogr, osr
import qtmgenerator as qg

# Constants ////////////////////////////////////////////////////////////////////////////

# The acceptable i/o file types by file extension and their driver names for OGR.
# These are a hand-picked subset of these: http://gdal.org/1.11/ogr/ogr_formats.html.
# These are chosen mainly because they're available in OGR by default (i.e., OGR is
# compiled supporting them by default), and they carry attribute fields over well.
typesAndDrivers = {
    ".shp":     "ESRI Shapefile",
    ".geojson": "GeoJSON",
    ".kml":     "KML",
    ".gml":     "GML",
    ".gmt":     "GMT"
}

idFieldName = "QTMID"

# Well-known text definition of World Geodetic System of 1984 (WGS 84). Taken from spatialreference.org.
wgs84WKT = """GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]"""


# Methods //////////////////////////////////////////////////////////////////////////////

def getDriverByFilepath(filePath):
    name, ext = os.path.splitext(filePath)
    try:
        driverName = typesAndDrivers[ext.lower()]
        # print(driverName)
        driver = ogr.GetDriverByName(driverName)
        return driver
    except:
        print("Couldn't determine the OGR driver for file extension {}, aborting.".format(ext))
        exit()

def loadDataSourceFromFile(filePath):
    """Given a file reference, this function loads and returns the file as an OGR
    data source. The relevant OGR driver is interpreted from the file extension."""

    drv = getDriverByFilepath(filePath)
    dataSource = drv.Open(filePath, 0) # 0 means read-only. 1 means writeable.
    if dataSource is None:
        print("Couldn't open {}, aborting.".format(filePath))
        exit()
    else:
        return dataSource

def levelByIDLength(anIDLength):
    # Examples:
    # 4 would be the 4th octant, level 0.
    # 80312 would be level four, a sub-facet in the 8th octant.
    return anIDLength - 1

def determineOrient(anID, vertexCount):
    # Facets at the poles:
    # Polar facets will have 5 points defined, and others 4. North pole facets will have
    # an index starting with 1, 2, 3, or 4, and southern 5, 6, 7, or 8.
    # two vertices at 90.0 latitude, south at -90.0.
    # Also, polar facets will aways be of form x1111..., where x is 1 through 8, and all
    # following digits are consistently 1.
    # All others:
    # The original four octants are up (1-4) or down (5-8) to begin with, and then
    # orientation changes to the opposite each time a zero digit is encountered, reading
    # from left to right.
    northern = ["1","2","3","4"]
    if vertexCount == 5:
        # Polar
        if anID[0] in northern:  #This rectangle chunk definately works
            return "n"
        else:
            return "s"

    elif vertexCount == 4:
        upward = True
        for i in range(len(anID)):
            if i == 0:
                # Octant digit, the first one.
                if anID[i] not in northern:
                    upward = not upward      #Doesn't seem to be an error here either.
            else:
                # Other digits, all others.
                if anID[i] == "0":
                    upward = not upward
        if upward:
            return "u"
        else:
            return "d"


def main():

    parser = argparse.ArgumentParser(description='')
    parser.add_argument('INFACETS', help='Path to an OGR-compatible file containing some set of QTM facets at any single level.')
    parser.add_argument('OUTFACETS', help='Path for an output file of OGR-writable format to contain the level+1 facets created by subdividing those from the input file.')
    args = parser.parse_args()
    inFile = args.INFACETS
    outFile = args.OUTFACETS

    # Load the input facets
    ds = loadDataSourceFromFile(inFile)
    inLayer = ds.GetLayer()
    # Determine the level of the input facets from their ID values. Also ensure consistency of level.
    idLengths = []
    for feature in inLayer:
        idLengths.append(len(str(feature.GetField(idFieldName))))
    inLayer.ResetReading()
    idLengthsSet = set(idLengths)
    if len(idLengthsSet) != 1:
        print("Input facets don't appear to be all of the same level, aborting.")
        exit()
    idLength = list(idLengthsSet)[0] # First one should be the length of all of them by this point.
    inLevel = levelByIDLength(idLength)
    print("QTM level of input is {}.".format(str(inLevel)))

    # Build facets, being Python lists, for these in-read facets, in the same matter as the qtmgenerator script has them.
    # Iterate over these features. If they have 4 vertices, they're triangular. If 5, "square." Importantly,
    # we need to determine wether they're north-facing or not, using determineOrient().
    inFacets = []
    for feature in inLayer:
        thisID = feature.GetField(idFieldName)
        # print(thisID)
        thisGeom = feature.GetGeometryRef() # This will be a top-most POLYGON defn.
        # print(str(thisGeom)) # Casting to string gives the WKT for the polygon.
        #
        # Now we go one level deeper to get the ring defining the polygon exterior.
        # We know there is only one geometry (i.e., the ring) within our QTM polygons,
        # so we go straight to index 0 instead of checking or looping:
        subGeom = thisGeom.GetGeometryRef(0)
        # print(str(subGeom))
        theseVertices = subGeom.GetPoints()
        # print(str(len(theseVertices)) + " verts in this ring.")
        orient = determineOrient(str(thisID), len(theseVertices)) # returns are any of "n", "s", "u" or "d".
        #print(orient) Orient returs right
        thisFacet = []
        for v in theseVertices:
            thisFacet.append( ( v[1], v[0] ) )
            # print("coords are {} {}".format(str(v[1]), str(v[0])))
        if orient == "n":
            thisFacet.append(True)
        elif orient == "s":
            thisFacet.append(False)
        else:
            # "u" or "d"
            thisFacet.append(orient)
        inFacets.append( (thisFacet, thisID) )

    # Prepare output file
    lvl = inLevel + 1
    sRef = osr.SpatialReference()
    sRef.ImportFromWkt(wgs84WKT)
    driver = getDriverByFilepath(outFile)
    dst_ds = driver.CreateDataSource(outFile)
    fName = os.path.splitext(os.path.split(outFile)[1])[0]
    dst_layer = dst_ds.CreateLayer(fName, sRef, geom_type=ogr.wkbPolygon)
    layer_defn = dst_layer.GetLayerDefn()
    new_field = ogr.FieldDefn(idFieldName, ogr.OFTString)
    dst_layer.CreateField(new_field)

    # Divide the in-read facets and write new ones to outfile.
    for iFacet in inFacets:
        # print(iFacet[0])
        theseFacets = qg.divideFacet(iFacet[0])
        # Subdivision facets are returned in 0 - 3 sequence. Subdivision ID strings
        # are the same as the parent layer, plus one more digit, 0 - 3. We use k
        # here to step through 0 - 4 for each subdivision facet as they are returned
        # in sequence.
        k = 0
        for tF in theseFacets:
            feature = ogr.Feature(layer_defn)
            feature.SetField(idFieldName, str(iFacet[1]) + str(k))
            facetGeometry = qg.constructGeometry(tF)
            feature.SetGeometry(facetGeometry)
            dst_layer.CreateFeature(feature)
            feature.Destroy()
            k = k +1

    dst_ds.Destroy()  # Destroy the data source to free resouces



# Main module check /////////////////////////////////////////////////////////////////////

if __name__ == '__main__':
    main()
