import sys, math

if(len(sys.argv) != 2):
    print ("Please specify the stat file to be processed.")
    sys.exit()

historyFileStr = 'History-file: '
numOpsStr = 'Num-operations: '
numConcOpsStr = 'Num-concurrent-ops: '
numConcOpPairsStr = 'Num-concurrent-op-pairs: '
numDistinctSchStr = 'Num-distinct-schedules: '

statsFile = sys.argv[1]
stats = open(statsFile)
counter = 0

avgNumOps = 0
avgNumConcOps = 0
avgNumConcOpPairs = 0
avgNumDistinctSch = 0

maxNumConcOps = 0
maxNumConcOpPairs = 0
maxNumDistinctSch = 0

numOps = []
numConcOps = []
numConcOpPairs = []
numDistinctSch = []

for line in stats:
    if(line.startswith(historyFileStr)):
        counter = counter + 1
    if(line.startswith(numOpsStr)):
        v= int(line[len(numOpsStr):])
        avgNumOps = avgNumOps + v
        numOps.append(v)
    if(line.startswith(numConcOpsStr)):
        v = int(line[len(numConcOpsStr):])
        avgNumConcOps = avgNumConcOps + v
        numConcOps.append(v)
        if(maxNumConcOps < v):
            maxNumConcOps = v
    if(line.startswith(numConcOpPairsStr)):
        v = int(line[len(numConcOpPairsStr):])
        avgNumConcOpPairs = avgNumConcOpPairs + v
        numConcOpPairs.append(v)
        if(maxNumConcOpPairs < v):
            maxNumConcOpPairs = v
    if(line.startswith(numDistinctSchStr)):
        v = int(line[len(numDistinctSchStr):])
        numDistinctSch.append(v)
        avgNumDistinctSch = avgNumDistinctSch + v
        if(maxNumDistinctSch < v):
            maxNumDistinctSch = v

avgNumOps = float(avgNumOps) / counter
avgNumConcOps = float(avgNumConcOps) / counter
avgNumConcOpPairs = float(avgNumConcOpPairs) / counter
avgNumDistinctSch = float(avgNumDistinctSch) / counter

stdNumOps = 0
stdNumConcOps = 0
stdNumConcOpPairs = 0
stdNumDistinctSch = 0

if(counter > 1):
    for i in range(0, counter-1):
        stdNumOps = stdNumOps + pow(numOps[i] - avgNumOps, 2)
        stdNumConcOps = stdNumConcOps + pow(numConcOps[i] - avgNumConcOps, 2)
        stdNumConcOpPairs = stdNumConcOpPairs + pow(numConcOpPairs[i] - avgNumConcOpPairs, 2)
        stdNumDistinctSch = stdNumDistinctSch + pow(numDistinctSch[i] - avgNumDistinctSch, 2)

    stdNumOps = math.sqrt(stdNumOps / (counter - 1))
    stdNumConcOps = math.sqrt(stdNumConcOps / (counter - 1))
    stdNumConcOpPairs = math.sqrt(stdNumConcOpPairs / (counter - 1))
    stdNumDistinctSch = math.sqrt(stdNumDistinctSch / (counter - 1))
else:
    stdNumOps = 0
    stdNumConcOps = 0
    stdNumConcOpPairs = 0
    stdNumDistinctSch = 0

print ("\n\nStats file: " + sys.argv[1])
print ("Num processed files: " + str(counter))
print ("Average num ops: " + str(avgNumOps) + "\n")

print ("Max num conc ops: " +  str(maxNumConcOps))
print ("Average num conc ops: " + str(avgNumConcOps))
print ("Std num conc ops: " +  str(stdNumConcOps) + "\n")

print ("Max num conc op pairs: " +  str(maxNumConcOpPairs))
print ("Average num conc op pairs: " + str(avgNumConcOpPairs))
print ("Std num conc op pairs: " +  str(stdNumConcOpPairs) + "\n")

print ("Max num distinct schedules: " + str(maxNumDistinctSch))
print ("Average num distinct schedules: " + str(avgNumDistinctSch))
print ("Std num distinct schedules: " +  str(stdNumDistinctSch) + "\n")

stats.close()