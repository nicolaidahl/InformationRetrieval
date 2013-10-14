#/usr/bin/python

import sys

d401 = dict()
d402 = dict()
d403 = dict()
d405 = dict()
d408 = dict()

qrels = open('qrels', 'r')

for line in qrels:
    a = line.split()
    if (a[0] == '401'):
        d401[a[2]] = a[3]
    elif (a[0] == '402'):
        d402[a[2]] = a[3]
    elif (a[0] == '403'):
        d403[a[2]] = a[3]
    elif (a[0] == '405'):
        d405[a[2]] = a[3]
    elif (a[0] == '408'):
        d408[a[2]] = a[3]

qrels.close()

results = open(sys.argv[1], 'r')

numrel = 0
for line in results:
    a = line.split()

    if (len(line) > 1 and line[0] != 'R' and int(a[2]) <= 10):
        if (a[0] == '401'):
            numrel += int(d401.get(a[1],0))
        elif (a[0] == '402'):
            numrel += int(d402.get(a[1],0))
        elif (a[0] == '403'):
            numrel += int(d403.get(a[1],0))
        elif (a[0] == '405'):
            numrel += int(d405.get(a[1],0))
        elif (a[0] == '408'):
            numrel += int(d408.get(a[1],0))

results.close()
print numrel
