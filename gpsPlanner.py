import numpy as np
import matplotlib.pyplot as plt

# import os
# def main():
#         data = []
#         filePath = os.path.join(os.sep,"Users","abhinavadarsh","Desktop","parsedFileNew.csv")
#         with open(filePath) as f:
#                 values = f.readlines()
#                 for line in values:
#                         line = line.split(",")
#                         if len(line) > 2:
#                                         if len(line[0]) > 0 and len(line[1]) > 0:
#                                                 data.append([float(line[0]), float(line[1])])
#
#         x = [t[0] for t in data if int(t[0]) !=0]
#         y = [t[1] for t in data if int(t[1]) !=0]
#         x = np.array(x, dtype = float)
#         y = np.array(y, dtype = float)
#         plt.xlim(min(x), max(x))
#         plt.ylim(min(y), max(y))
#         plt.scatter(x,y)
#         path = os.path.join(os.sep,"Users","abhinavadarsh","Desktop","plotDatawithoutOutlier.png")
#         plt.savefig(path)
#
# if __name__ == '__main__':
#         main()


import numpy as np
import os
#import matplotlib.pyplot as plt
#from PyNomaly import loop
from newlib import LDCOF
def main():
        fp = open('scores','w')
        #x=t.load_boston()['data']
        #print(len(x))
        #print(len(x[0]))
        data = []
        obj = LDCOF()
        point_map = dict()
        point_map["green"] = []
        point_map["yellow"] = []
        point_map["red"] = []

        filePath = os.path.join(os.sep, "Users", "abhinavadarsh", "Desktop", "parsedFileNew.csv")
        with open(filePath) as f:
                values = f.readlines()
                for line in values:
                        line = line.split(",")
                        if len(line) > 2:
                                        if len(line[0]) > 0 and len(line[1]) > 0 and '0.0' not in line:
                                                data.append([float(line[0]), float(line[1])])

        data = np.array(data, dtype = float)
        #obj.fit(data)
        total_rows = len(data)
        train_rows = 4 * (total_rows/5)
        train_data = data[:train_rows]
        test_data = data[train_rows:]
        #print(dir(obj))
        #print obj.__dict__
        #for t in obj.data_clusters: print t
        #print obj.distances
        obj.fit(train_data)
        t = obj.transform(test_data)
        print t
        #print t.__dict__
        #print obj.__dict__
        exit()
        ############
        data = np.random.rand(100, 5)
        np.random.shuffle(data)
        training, test = data[:80, :], data[80:, :]
        ############
        scores = loop.LocalOutlierProbability(data, extent=0.90, n_neighbors=10).fit()
        #s = [ t for t in scores if float(t)> 0.5]
        #print(len(s))
        for count,t in enumerate(scores):
            print(count, t)
            if t < 0.8:
                point_map["green"].append(data[count])
            elif t > 0.8 and  t < 0.98:
                point_map["yellow"].append(data[count])
            elif  t > 0.98:
                point_map["red"].append(data[count])

        print (len(point_map["green"]))
        print(len(point_map["yellow"]))
        print(len(point_map["red"]))
        print("------------------")
        #exit(0)
        for t in scores:
                fp.write(str(t)+"\n")
        fp.flush()
        x = [t[0] for t in data]
        y = [t[1] for t in data]
        x = np.array(x, dtype = float)
        y = np.array(y, dtype = float)
        x_min = min(x)
        x_max = max(x)
        y_min = min(y)
        y_max = max(y)
        print(x_min, x_max)
        print(y_min, y_max)

        points = point_map["green"]
        points = np.array(points, dtype = float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        print(len(x))
        plt.xlim(min(x), max(x))
        plt.ylim(min(y), max(y))
        plt.scatter(x,y,color = "green", s =1)

        points = point_map["yellow"]
        points = np.array(points, dtype = float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        print(len(x))
        plt.scatter(x, y, color="yellow", s = 1)

        points = point_map["red"]
        points = np.array(points, dtype = float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        print(len(x))
        plt.scatter(x,y,color = "red", s = 1)

        plt.xlim(x_min, x_max)
        plt.ylim(y_min, y_max)
        plt.show()
if __name__ == '__main__':
        main()