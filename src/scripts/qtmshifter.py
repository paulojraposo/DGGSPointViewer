# -*- coding: utf-8 -*-

# Randall Brown
# May 9th, 2018

#   .-.                              _____                                  __
#   /v\    L   I   N   U   X       / ____/__   ___   ___   ____ ___  ____  / /_  __  __
#  // \\  >Phear the Penguin<     / / __/ _ \/ __ \/ __ `/ ___/ __ `/ __ \/ __ \/ / / /
# /(   )\                        / /_/ /  __/ /_/ / /_/ / /  / /_/ / /_/ / / / / /_/ /
#  ^^-^^                         \____/\___/\____/\__, /_/   \__,_/ .___/_/ /_/\__, /
#                                                /____/          /_/          /____/

# Simple script that takes in a QTM file and shifts the longitude of the file by a user defined amount.

# TODO: Find a way to pull the driver directly from the filetype so it works for more than just GeoJSON files.

# Imports ///////////////////////////////////////////////////////////////////////////

import os, argparse
from osgeo import ogr, gdal, osr
from qtmgenerator import constructGeometry
gdal.UseExceptions()

# Script ////////////////////////////////////////////////////////////////////////////////

def main():

    # Parse arguments.
    parser = argparse.ArgumentParser(description='Accepts a QTM GeoJSON file and produces a copy, translated east or west by the given number of geographical degrees.')
    parser.add_argument('QTMLEVELJSON', help='The GeoJSON file for the desired QTM level')
    parser.add_argument('OUTFILEDIR', help='Full path to output directory for the product QTM shapefiles.')
    parser.add_argument('LON_SHIFT', help = 'Number of degrees to shift QTM in longitudinal direction. Positive numbers shift east, negative shift west.')
    args = parser.parse_args()
    geoJ = args.QTMLEVELJSON
    outFileDir = args.OUTFILEDIR
    theta = float(args.LON_SHIFT)

    # Loading input GeoJSON.
    driver = ogr.GetDriverByName("GeoJSON")
    dataSource = driver.Open(geoJ, 0)
    orig_Layer = dataSource.GetLayer()
    featureCount = orig_Layer.GetFeatureCount()

    # Setup for output file name.
    filename, extension = os.path.splitext(geoJ)
    baseFileName = os.path.basename(filename)
    sign = 'p'
    if (theta < 0):
        sign = 'n'

    # Declaration of lists and dictionaries.
    idList = []
    newFacets = []
    iterator = 0

    # Parses through the geometry of the supplied GeoJSON file.
    for feature in orig_Layer:

        NewVerts = []

        # Pulls QTMID
        thisPolyID = feature.GetField("QTMID")

        idList.append(thisPolyID)

        # This will be a top-most POLYGON defn.
        thisGeom = feature.GetGeometryRef() # This will be a top-most POLYGON (( )) defn.

        # Now we go one level deeper to get the ring defining the polygon. We know
        # there is only one geometry (i.e., the ring) within our QTM polygons, so
        # we go straight to index 0 instead of looping:
        subGeom = thisGeom.GetGeometryRef(0)

        # Pulls vertices from input file and shifts longitude by theta degrees.
        verts = subGeom.GetPoints()
        for v in verts:
            vPrime = (v[1], v[0] + theta)
            NewVerts.append(vPrime)

        # This last None item is appended to the NewVerts list because
        # we want to later use qtmgenerator.py's constructGeometry method,
        # which expects a final element indicating facet orientation. It
        # isn't actually used by the method, so it's acceptable here to
        # pass in a dummy variable so the lists are either 5 or 6 elements
        # long, which is what constructGeometry is written to handle.
        NewVerts.append(None)

        newFacets.append(NewVerts)

    # Sets up the output file.
    wktCoordSys = """GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]"""
    outJSONFileName = baseFileName + "_lon" + sign + str(abs(theta)) + extension
    print("File name: {}".format(outJSONFileName))
    sRef = osr.SpatialReference()
    sRef.ImportFromWkt(wktCoordSys)
    driver = ogr.GetDriverByName('GeoJSON')
    outFile = os.path.join(outFileDir, outJSONFileName)
    dst_ds = driver.CreateDataSource(outFile)
    fName = os.path.splitext(os.path.split(outFile)[1])[0]
    dst_layer = dst_ds.CreateLayer(fName, sRef, geom_type=ogr.wkbPolygon)
    levelFieldName = 'QTMID'
    layer_defn = dst_layer.GetLayerDefn()
    new_field = ogr.FieldDefn(levelFieldName, ogr.OFTInteger)
    dst_layer.CreateField(new_field)

    # Create features and write to file.
    for f in newFacets:
        feature = ogr.Feature(layer_defn)
        feature.SetField('QTMID', idList[iterator])
        facetGeometry = constructGeometry(f)
        feature.SetGeometry(facetGeometry)
        dst_layer.CreateFeature(feature)
        iterator = iterator + 1
        feature.Destroy()  # Destroy the feature to free resources.

    dst_ds.Destroy()  # Destroy the data source to free resouces.


if __name__ == '__main__':
    main()

# fin
# exit()
