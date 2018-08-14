from subprocess import call
import os, sys
from os import path

if(len(sys.argv) != 3):
  print ("Please specify the directory of test files and a directory name for the output files")
  sys.exit()

testsPath = sys.argv[1]
outPath = path.join("out", sys.argv[2])
pattern = "*.java"

print ("Running tests..")

counter = 1
if not path.exists(outPath):
  os.makedirs(outPath)

count = 0

dirs = os.listdir(testsPath)
for entry in dirs: #todo check if dir
  if(path.isdir(path.join(testsPath, entry))):
    outFile = open(path.join("out", path.join(sys.argv[2], str(entry)+(".txt"))), "w")
    files = os.listdir(path.join(testsPath,entry))
    for f in sorted(files):
      if f.endswith(".class"):
        class_file, _ = path.splitext(path.join(path.join(testsPath, entry), f))
        command = "java {0}".format(class_file.replace('/', '.'))
        #print(command)
        call(command, shell=True, stdout=outFile)
