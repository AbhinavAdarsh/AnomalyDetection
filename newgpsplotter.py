
import matplotlib.pyplot as plt
import numpy as np
import os

from newlib import LDCOF
def main():
        legend1 = {}
        legend2 = {}
        fp = open('scores','w')
        data = []
        obj = LDCOF(n_clusters=3)
        point_map = dict()
        point_map["green"] = []
        point_map["yellow"] = []
        point_map["red"] = []

        filePath = os.path.join(os.sep, "Users", "abhinavadarsh", "Desktop", "Plots", "parsedFileNew.csv")
        img = os.path.join(os.sep, "Users", "abhinavadarsh", "Desktop", "plotNew.png")
        with open(filePath) as f:
                values = f.readlines()
                for line in values:
                        line = line.split(",")
                        if len(line) > 2:
                                        if len(line[0]) > 0 and len(line[1]) > 0 and '0.0' not in line:
                                                data.append([float(line[0]), float(line[1])])

        data = np.array(data, dtype = float)

        np.random.shuffle(data)
        #training, test = data[:80,:], x[80:,:]


        # total_rows = len(data)
        # train_rows = 4 * (total_rows/5)
        # train_data = data[:train_rows]
        # test_data = data[train_rows:]
        obj.fit(data)
        t = obj.transform(data)

        for count,t in enumerate(t):
            #print(count, t)
            if t < 18:
                point_map["green"].append(data[count])
            elif t > 18 and  t < 20:
                point_map["yellow"].append(data[count])
            elif  t > 21:
                point_map["red"].append(data[count])


        x = [t[0] for t in data]
        y = [t[1] for t in data]
        x = np.array(x, dtype = float)
        y = np.array(y, dtype = float)
        x_min = min(x)
        x_max = max(x)
        y_min = min(y)
        y_max = max(y)

        points = point_map["green"]
        points = np.array(points, dtype=float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        plt.scatter(x,y,color = "green", s =15, label = "Safe points")

        points = point_map["yellow"]
        points = np.array(points, dtype = float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        plt.scatter(x, y, color="cyan", s = 25, label = "Between safe and outlier", marker ="+")

        points = point_map["red"]
        points = np.array(points, dtype = float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        print(len(x))
        plt.scatter(x,y,color = "red", s = 40, label = "Outliers", marker="*")

        points = obj.cluster_centers
        points = np.array(points, dtype=float)
        x = [t[0] for t in points]
        y = [t[1] for t in points]
        plt.scatter(x, y, color="blue", s=60, label = "Centroids", marker="^")
        plt.xlim(x_min, x_max)
        plt.ylim(y_min, y_max)
        legend = plt.legend(loc='center left', shadow=True)
        plt.xlabel("Latitude", fontsize=20)
        plt.ylabel("Longitude", fontsize=20)
        plt.title("Outlier Detection Scatter Plot", fontsize=25)
        #plt.annotate()
        # #z = x# * np.exp(-x ** 2 - y ** 2)
        # z = []
        # #assert len(z) == (len(x) * len(y))
        # z = np.array(z)
        # #z = z.reshape((len(x), len(y)))
        # x = np.arange(5)
        # y = np.arange(5)
        # z = np.arange(25).reshape(5, 5)
        # x1, y1 = np.meshgrid(x, y)
        # plt.contour(x1, y1, z)
        # plt.colorbar()
        #plt.savefig(img)
        plt.show()
if __name__ == '__main__':
        main()