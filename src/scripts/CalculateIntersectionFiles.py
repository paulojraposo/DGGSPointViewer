#!/usr/bin/env python3
# -*- coding: utf-8 -*-

#   .-.                              _____                                  __
#   /v\     L   I   N   U   X      / ____/__   ___   ___   ____ ___  ____  / /_  __  __
#  // \\  >Respect the Penguin<   / / __/ _ \/ __ \/ __ `/ ___/ __ `/ __ \/ __ \/ / / /
# /(   )\                        / /_/ /  __/ /_/ / /_/ / /  / /_/ / /_/ / / / / /_/ /
#  ^^-^^                         \____/\___/\____/\__, /_/   \__,_/ .___/_/ /_/\__, /
#                                                /____/          /_/          /____/


try:

    # Not all these are used by this script, but are used by the scripts it
    # calls in subprocesses, so checking for them all now.
    import os, sys, argparse, csv, math, datetime, subprocess, time
    import numpy as np
    import nvector as nv
    from scipy import stats
    from osgeo import ogr, osr

    import DGGSViewer_script_utilities
    import qtmgenerator
    import qtmpointintersection
    import qtmshifter
    import makesingleqtmsublevel

except:

    print("Some dependency is missing, or your Python environment isn't set right. See the README file for how to fix this.")




def printHeaderMsg(msg):
    print("\n/*")
    print(msg)
    print(" */\n")



def formatTimeInterval(interval):
    # From Padraic Cunningham, https://stackoverflow.com/questions/27779677/how-to-format-elapsed-time-from-seconds-to-hours-minutes-seconds-and-milliseco
   hours, rem = divmod(interval, 3600)
   minutes, seconds = divmod(rem, 60)
   return "{:0>2}:{:0>2}:{:05.2f}".format(int(hours), int(minutes), seconds)



def main():

    scriptStart = time.time()

    desc = "A script that prepares data in a CSV file for visualization in the DGGS Viewer app. Please see the README file for details."

    # Parse arguments from the command line.
    parser = argparse.ArgumentParser(description=desc)
    parser.add_argument('USERCSV', help='A user-supplied CSV file of points. See the README file for the necessary formatting of this file. Must contain fields for lat and lon coordinates named exactly "latitude" and "longitude", case-specific.')
    parser.add_argument('CSVFIELD', help='Name of the field within the input CSV for which statistics will be calculated. Must be a ratio numerical value in all cells.')
    parser.add_argument('USERDIR', help='A directory into which to write the intersected QTM facets.')
    parser.add_argument('--MAXQTMLEVEL', default=10, help='An integer number of how many QTM levels to generate.')
    parser.add_argument('--KEEPINTERIM', default=False, action="store_true", help='Whether or not to keep all non-data geojson files in the output folder. False by default; such files will be deleted.')
    args = parser.parse_args()

    # Absolute file paths for the files we'll use.
    userCSV = os.path.abspath(args.USERCSV)
    qtmgeneratorModulePath = os.path.abspath(qtmgenerator.__file__)
    qtmpointintersectionModulePath = os.path.abspath(qtmpointintersection.__file__)
    qtmshifterModulePath = os.path.abspath(qtmshifter.__file__)
    makesingleqtmsublevelModulePath = os.path.abspath(makesingleqtmsublevel.__file__)

    appLonShifts = [-5.0, -4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0]
    firstProcessedLevel = 3 # We skip levels 0, 1, and 2.

    baseQTMFileFormat   = "qtmlvl{}.geojson" # lvl only
    blankQTMFileFormat  = "qtmlvl{}lonshft{}.geojson" # fields for lvl and lon shift, respectively.
    outputsNameFormat   = "qtmlvl{}lonshft{}_agg.geojson" # fields for lvl and lon shift, respectively.



    # First, make the first 3 QTM levels in the user folder. Then produce all the lon shifted
    # versions for the firstProcessedLevel.
    printHeaderMsg("Making bare first 4 levels of QTM (indexes 0 through 3) in the given directory.")
    cmd = "python {} {} {}".format(qtmgeneratorModulePath, args.USERDIR, firstProcessedLevel + 1)
    subprocess.call(cmd, shell=True)

    for lonShift in appLonShifts:
        baseQTMFile = os.path.join(args.USERDIR, baseQTMFileFormat.format(str(firstProcessedLevel)))
        cmd = "python {} {} {}".format(qtmshifterModulePath, baseQTMFile, str(lonShift))
        subprocess.call(cmd, shell=True)



    # Next, we call couplets of qtmpointintersection.py and makesingleqtmsublevel.py for each QTM level and lon shift.
    printHeaderMsg("Starting intersection calculations.")

    aCounter = 1
    permutations = (int(args.MAXQTMLEVEL) - firstProcessedLevel + 1) * 11 # levels by lon shifts.

    # For giving the user an estimate of time left before completion from this point on.
    permutationTimes = []
    runningPermTimeAverage = None

    for qlvl in range(int(args.MAXQTMLEVEL) + 1):

        if qlvl >= firstProcessedLevel:

            for lonShift in appLonShifts:

                # Giving the user a notion of how much time is left for the whole batch, based on
                # running average of permutation run time.
                estTimeLeft = "< calculating, ready in next permutation... >"
                if runningPermTimeAverage:
                    estTimeLeft = formatTimeInterval( runningPermTimeAverage * (permutations - aCounter) )

                progressPrecent = str( round( 100.0 * float(aCounter) / float(permutations), 1 ) )

                printHeaderMsg("Working on {} of {} permutations ({}%). Estimated completion in {}".format(aCounter, permutations, progressPrecent, estTimeLeft))

                permStart = time.time()

                # Calculate this intersection with a call to qtmpointintersection.py
                cmdFormat = "python {} {} {} {} {} --oi" # script, inQTM, outQTM, CSV, field. Always making only those facets with intersections.
                script = qtmpointintersectionModulePath
                qtmFile = os.path.join(args.USERDIR, blankQTMFileFormat.format(str(qlvl), str(lonShift)))
                intersectedQTM = os.path.join(args.USERDIR, outputsNameFormat.format(str(qlvl), str(lonShift)))
                csvFile = userCSV
                fieldName = args.CSVFIELD
                cmd = cmdFormat.format(script, qtmFile, intersectedQTM, csvFile, fieldName)
                subprocess.call(cmd, shell=True)

                # Make the next levels subfacets with their names as per blankQTMFileFormat.
                cmdFormat = "python {} {} {}" # script, inFacets, outFacets
                script = makesingleqtmsublevelModulePath
                inFacets = intersectedQTM
                outFacets = os.path.join(args.USERDIR, blankQTMFileFormat.format(str(qlvl+1), str(lonShift)))
                cmd = cmdFormat.format(makesingleqtmsublevelModulePath, inFacets, outFacets)
                subprocess.call(cmd, shell=True)

                permEnd = time.time()

                permTime = permEnd - permStart
                permutationTimes.append(permTime)
                runningPermTimeAverage = float(sum(permutationTimes)) / len(permutationTimes)

                aCounter += 1



    if not args.KEEPINTERIM:
        printHeaderMsg("Deleting interim files from given folder...")
        interimFiles = [ os.path.join(args.USERDIR, f) for f in os.listdir(args.USERDIR) if os.path.isfile(os.path.join(args.USERDIR, f)) ]
        for iF in interimFiles:
            fName = os.path.split(iF)[1]
            fExt  = os.path.splitext(fName)[1]
            # Data files all end in "_agg.geojson". Also, in case the user csv is in this folder, don't delete it.
            if "_agg.geojson" not in fName and iF != userCSV and fExt.lower() != ".csv":
                os.remove(iF)




    printHeaderMsg("Finished, total elapsed time {}".format(formatTimeInterval(time.time() - scriptStart)))





if __name__ == "__main__":
    main()
