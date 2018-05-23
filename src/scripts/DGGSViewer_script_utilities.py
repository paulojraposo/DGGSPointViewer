# -*- coding: utf-8 -*-

import os
import ogr

# Acceptable i/o file types by file extension and their driver names for OGR.
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

# WGS84, in WKT
wktWGS84 = """GEOGCS["WGS 84",DATUM["WGS_1984",SPHEROID["WGS 84",6378137,298.257223563,AUTHORITY["EPSG","7030"]],AUTHORITY["EPSG","6326"]],PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],AUTHORITY["EPSG","4326"]]"""

def appendSuffixToFileName(filepath, suffix):
    """Simply appends a suffix to a filepath, before the file extension, and returns the new filepath."""
    containingDir, fileBasename = os.path.split(filepath)
    fileBasenameNoExt, ext = os.path.splitext(fileBasename)
    return os.path.join(containingDir, fileBasenameNoExt + suffix + ext)

def getDriverByFilepath(filepath):
    path, ext = os.path.splitext(filepath)
    ogrDriverTextCode = typesAndDrivers[ext.lower()]
    return ogr.GetDriverByName(ogrDriverTextCode)
