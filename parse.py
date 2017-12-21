import csv
import os
#files_to_read = ["1.txt","2.txt","3.txt","4.txt","5.txt","1.txt","1.txt","1.txt","1.txt","1.txt","1.txt","1.txt","1.txt",]
#values = []
#for file in files_to_read:
#    # process each file here
main_path = "/Users/abhinavadarsh/Desktop/Mac/Stony/Wireless/Project/DataCollection/Data/logsGPS2to23"

fp = open('/Users/abhinavadarsh/Desktop/parsedFileforServer.csv','w')
writer = csv.writer(fp)

for i in range(1,20):
    file_name = str(i) +".txt"
    print "Filename : ",file_name
    file_name = os.path.join(main_path,file_name)

    with open(file_name) as f:
        data = f.read()
        data = data.split("\n")

        for line in data:
            #print (line.split('@')[-1])
            line = line.split('@')
            if len(line) >=3 and '0.0' not in line:
                values = [line[-1],[line[0],line[1]]]
                writer.writerow(values)

fp.close()
#
# with open('','wb') as fp:
#     writer = csv.writer(fp)
#     writer.writerow([item])
#
# writer= csv.writer(open(/Users/abhinavadarsh/Desktop/parsedFile,'wb'))
# header=['type','id','numberOfUpdates','isPingEnabled','lastUpdated']
# length_list=len(header)
# i=0
#
# while i!=length_list :
#     data=header[i]
#     print data
#     i=i+1
#     writer.writerow([data])
#
# for word in header:
#     writer.writerow([word])