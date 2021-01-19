"""
RANSAC Algorithm Problem
(Due date: Oct. 23, 3 P.M., 2019)
The goal of this task is to fit a line to the given points using RANSAC algorithm, and output
the names of inlier points and outlier points for the line.

Do NOT modify the code provided to you.
Do NOT use ANY API provided by opencv (cv2) and numpy (np) in your code.
Do NOT import ANY library (function, module, etc.).
You can use the library random
Hint: It is recommended to record the two initial points each time, such that you will Not
start from this two points in next iteration.
"""
import random
def solution(input_points, t, d, k):
    """
    :param input_points:
           t: t is the perpendicular distance threshold from a point to a line
           d: d is the number of nearby points required to assert a model fits well, you may not need this parameter
           k: k is the number of iteration times
           Note that, n for line should be 2
           (more information can be found on the page 90 of slides "Image Features and Matching")
    :return: inlier_points_name, outlier_points_name
    inlier_points_name and outlier_points_name is two list, each element of them is str type.
    For example: If 'a','b' is inlier_points and 'c' is outlier_point.
    the output should be two lists of ['a', 'b'], ['c'].
    Note that, these two lists should be non-empty.
    """
    # TODO: implement this function.
    Result_list = list() # Storing temporary results
    final_result = list([100,100,100]) # final Result
    sample_list = list() # Keeping Track of all samples

    for k in range(0,100):
        n = len(input_points)
        r = random.sample(range(1,n),2)
        if r in sample_list:
            continue
        sample_list.append(r)

        p1 = input_points[r[0]-1]
        p2 = input_points[r[1]-1]

        temp1 = p1["value"][1]-p2["value"][1] #y2-y1
        temp2 = p1["value"][0]-p2["value"][0] #x2-x1

        line = [temp1,-temp2,temp2*p1["value"][1]-temp1*p1["value"][0]]

        inl = list()
        out = list()
        Error = 0
        for point in input_points:
            p = point["value"]
            dis = abs(p[0]*line[0]+p[1]*line[1]+line[2])/((line[0]**2 + line[1]**2)**0.5)
            if (dis>0.5):
                out.append(point["name"])
            else:
                inl.append(point["name"])
                Error = Error + dis #Calculating Error for Inliers

        Result_list.append([inl,out,Error,line])

        for result in Result_list:
            if (len(result[0]) >= d+2):
                if final_result[2] > result[2]:
                    final_result = result
                else:
                    continue

    inliers =  final_result[0]
    outliers = final_result[1]

    # print ("Inliers",inliers)
    # print ("Outliers",outliers)
    return inliers, outliers



if __name__ == "__main__":
    input_points = [{'name': 'a', 'value': (0.0, 1.0)}, {'name': 'b', 'value': (2.0, 1.0)},
                    {'name': 'c', 'value': (3.0, 1.0)}, {'name': 'd', 'value': (0.0, 3.0)},
                    {'name': 'e', 'value': (1.0, 2.0)}, {'name': 'f', 'value': (1.5, 1.5)},
                    {'name': 'g', 'value': (1.0, 1.0)}, {'name': 'h', 'value': (1.5, 2.0)}]
    t = 0.5
    d = 3
    k = 100
    inlier_points_name, outlier_points_name = solution(input_points, t, d, k)  # TODO
    assert len(inlier_points_name) + len(outlier_points_name) == 8
    f = open('./results/task1_result.txt', 'w')
    f.write('inlier points: ')
    for inliers in inlier_points_name:
        f.write(inliers + ',')
    f.write('\n')
    f.write('outlier points: ')
    for outliers in outlier_points_name:
        f.write(outliers + ',')
    f.close()
