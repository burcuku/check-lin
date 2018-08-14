from subprocess import call
import os, sys
from os import path

if len(sys.argv) != 3:
    print ("Please specify the directory of test files and a directory name for the output files")
    sys.exit()

testsPath = sys.argv[1]
outPath = path.join("out", sys.argv[2])
pattern = "*.java"


print ("Compiling tests..")

for x in next(os.walk(testsPath))[1]:
    for y in next(os.walk(os.path.join(testsPath, str(x))))[2]:
        if str(y).endswith(".java"):
            command = "javac {0}".format(os.path.join(os.path.join(sys.argv[1], str(x)), str(y)))
            # print command
            call(command, shell=True)
