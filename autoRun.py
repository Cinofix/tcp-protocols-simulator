import os
import sys
import time
import csv

# Read file
path = "simulations.csv"
csv.field_size_limit(sys.maxsize)

with open(path, 'rb') as f:
    reader = csv.reader(f)
    simulations = list(reader)

n = len(simulations)
row = 1
while row < n:
	for i in range(row,min(row+5,n)):
		parameters = simulations[i][0].split(";")
		print parameters
		protocol = parameters[0]
		host = parameters[1]
		buff = parameters[2]
		p = parameters[3]
		g = parameters[4]
		thread = parameters[5]
		lwelch = parameters[6]
		t = parameters[7]
		os.system("java -jar Simtipee.jar " +protocol+" "+host+" "+buff+" "+p+" "+g+" "+thread+" "+lwelch+" "+t+" &")
		row = row+1
	#time.sleep(60*5) # sleep if necessary
