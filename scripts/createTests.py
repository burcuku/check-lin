from subprocess import call
import sys

if(len(sys.argv) != 6):
  print ("Please specify the directory of input files, name of test files, "
		 "stat file, and the min and max depth for the hitting families.")
  sys.exit()

inputFile = sys.argv[1]
testsFile = sys.argv[2]
statFile = sys.argv[3]
minD = sys.argv[4]
maxD = sys.argv[5]

print ("Creating tests..")

for x in range(int(minD), int(maxD)):
  command = "sbt -J-Xmx4G -J-Xms4G \"run {0} {1} {2} {3}\"".format(inputFile, testsFile + str(x), statFile + str(x), x)
  call(command, shell=True)
  print ("Created Tests for d="  + str(x) + " for file: " + inputFile + "\n")

print ("Completed tests..")