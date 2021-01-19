"""
K-Means Segmentation Problem
(Due date: Nov. 25, 11:59 P.M., 2019)
The goal of this task is to segment image using k-means clustering.

Do NOT modify the code provided to you.
Do NOT import ANY library or API besides what has been listed.
Hint:
Please complete all the functions that are labeled with '#to do'.
You are allowed to add your own functions if needed.
You should design you algorithm as fast as possible. To avoid repetitve calculation, you are suggested to depict clustering based on statistic histogram [0,255].
You will be graded based on the total distortion, e.g., sum of distances, the less the better your clustering is.
"""


import utils
import numpy as np
import json
import time


def kmeans(img,k):
    """
    Implement kmeans clustering on the given image.
    Steps:
    (1) Random initialize the centers.
    (2) Calculate distances and update centers, stop when centers do not change.
    (3) Iterate all initializations and return the best result.
    Arg: Input image;
         Number of K.
    Return: Clustering center values;
            Clustering labels of all pixels;
            Minimum summation of distance between each pixel and its center.
    """

    histogram = set(img.reshape(512*512))
    histogram = list(histogram)
    best_score = 1000000000
    recorded = []
    counter = 0
    minus = np.full((512,512),-1)
    # img = np.array(img, dtype = "int32")


    for i in range(0,163):
        for j in range(i+1,163):
            counter += 1
            # print ("Iteration : ",counter)

            c1 = histogram[i]
            c2 = histogram[j]

            while(True):
                if (c1,c2) in recorded:
                    break
                elif (c2,c1) in recorded:
                    break

                else:
                    recorded.append((c1,c2))
                # cluster1 = []
                # cluster2 = []
                error = 0
                # for row in range(0,512):
                #     for col in range(0,512):
                #         dis_c1 = abs(int(img[row,col]) - c1)
                #         dis_c2 = abs(int(img[row,col]) - c2)
                #
                #         if (dis_c1 <= dis_c2):
                #             cluster1.append(img[row,col])
                #             #error = error + dis_c1
                #
                #         elif (dis_c1 > dis_c2):
                #             cluster2.append(img[row,col])
                #             #error = error + dis_c2

                img1 = img.copy()
                img2 = img.copy()
                # img1.reshape(262144,1)
                # img2.reshape(262144,1)

                img1 = abs(img1-np.full((512,512),c1))
                img2 = abs(img2-np.full((512,512),c2))

                cluster1 = np.where(img1<=img2,img,-1)
                cluster2 = np.where(img1<=img2,-1,img)


                #print (cluster1.shape)
                count_1 = np.count_nonzero(cluster1 == -1)
                count_2 = np.count_nonzero(cluster2 == -1)


                new_c1 = (np.sum(cluster1)) + count_1
                new_c2 = (np.sum(cluster2)) + count_2

                # print (len(cluster1),len(cluster2))
                # new_c1 = int(sum(cluster1))
                # new_c2 = int(sum(cluster2))
                # print (new_c1,new_c2)

                if (new_c1 == 0):
                    new_c1 = 0
                    new_c2 = int (new_c2 / (262144-count_2))

                elif (new_c2 == 0):
                    new_c1 = int(new_c1 / (262144-count_1))
                    new_c2 = 0

                else:
                    new_c1 = int(new_c1 / (262144-count_1))
                    new_c2 = int(new_c2 / (262144-count_2))


                if (new_c1 == c1 and new_c2 == c2):
                    final_c1 = c1
                    final_c2 = c2
                    distance = np.sum(np.where(img1<=img2,img1,img2))
                    labels = np.where(img1<=img2,0,1)


                    # print ("Starting points ",c1,c2,"Error ",distance,"Final_centers",final_c1,final_c2,"New centers",new_c1,new_c2)
                c1 = new_c1
                c2 = new_c2


    return (int(final_c1),int(final_c2)),labels, int(distance)


def visualize(centers,labels):
    """
    Convert the image to segmentation map replacing each pixel value with its center.
    Arg: Clustering center values;
         Clustering labels of all pixels.
    Return: Segmentation map.
    """
    c1 = centers[0]
    c2 = centers[1]
    vis_img = np.where(labels==0,c1,c2)
    vis_img = np.array(vis_img,dtype="uint8")

    return vis_img


if __name__ == "__main__":
    img = utils.read_image('lenna.png')
    k = 2
    #test(79,158)

    start_time = time.time()
    centers, labels, sumdistance = kmeans(img,k)
    result = visualize(centers, labels)
    end_time = time.time()

    running_time = end_time - start_time
    print(running_time)

    centers = list(centers)
    with open('results/task1.json', "w") as jsonFile:
        jsonFile.write(json.dumps({"centers":centers, "distance":sumdistance, "time":running_time}))
    utils.write_image(result, 'results/task1_result.jpg')
