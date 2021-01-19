"""
Image Stitching Problem
(Due date: Oct. 23, 3 P.M., 2019)
The goal of this task is to stitch two images of overlap into one image.
To this end, you need to find feature points of interest in one image, and then find
the corresponding ones in another image. After this, you can simply stitch the two images
by aligning the matched feature points.
For simplicity, the input two images are only clipped along the horizontal direction, which
means you only need to find the corresponding features in the same rows to achieve image stiching.

Do NOT modify the code provided to you.
You are allowed use APIs provided by numpy and opencv, except “cv2.findHomography()” and
APIs that have “stitch”, “Stitch”, “match” or “Match” in their names, e.g., “cv2.BFMatcher()” and
“cv2.Stitcher.create()”.
"""
import cv2
import numpy as np
import random
import matplotlib.pyplot as plt

def solution(left_img, right_img):
    """
    :param left_img:
    :param right_img:
    :return: you need to return the result image which is stitched by left_img and right_img
    """

    orb = cv2.ORB_create()


    # x_offset = 50
    # y_offset = 50
    # offset_matrix = np.matrix([[1,0,x_offset],[0,1,y_offset],[0,0,1]])
    # translation_matrix = np.float32([ [1,0,x_offset],[0,1,y_offset] ])
    # height, width = left_img.shape[:2]

    # left_img = cv2.warpAffine(left_img,translation_matrix,(width,height))
    # #H_matrix = np.dot(offset_matrix,H_matrix)


    # right_img = cv2.warpAffine(right_img,translation_matrix,(width,height))
    # left_img = cv2.warpAffine(left_img,translation_matrix,(width,height))

    left_kp, left_dscp = orb.detectAndCompute(left_img,None)
    right_kp, right_dscp = orb.detectAndCompute(right_img,None)

    matches = cv2.BFMatcher().match(left_dscp,right_dscp,)
    matches = sorted(matches,key = lambda match:match.distance)
    #matches = matches[:10]


    source = np.float32([left_kp[m.queryIdx].pt for m in matches]).reshape(-1,1,2)
    dest = np.float32([right_kp[m.trainIdx].pt for m in matches]).reshape(-1,1,2)

    # Homography Matrix
    H_matrix, mask = cv2.findHomography(dest, source, cv2.RANSAC,5)


    #
    # x_offset = 0
    # y_offset = 70
    # offset_matrix = np.matrix([[1,0,x_offset],[0,1,y_offset],[0,0,1]])
    # translation_matrix = np.float32([ [1,0,x_offset],[0,1,y_offset] ])
    # height, width = left_img.shape[:2]
    # left_img = cv2.warpAffine(left_img,translation_matrix,(width,height))
    # H_matrix = np.dot(offset_matrix,H_matrix)



    # left_img = np.dot(left_img,offset_matrix)
    # right_img = np.dot(right_img,offset_matrix)






    # cv2.imshow("left",left_img)
    #
    # cv2.imshow("new_left",t)
    # cv2.waitKey()

    #citations - Used opencv tutorials to do this project


    dim = (right_img.shape[1] + left_img.shape[1], right_img.shape[0])
    result = cv2.warpPerspective(right_img, H_matrix, dim)



    result[0:left_img.shape[0], 0:left_img.shape[1]] = left_img

    return result


if __name__ == "__main__":
    left_img = cv2.imread('left.png')
    right_img = cv2.imread('right.png')
    result_image = solution(left_img, right_img)
    cv2.imwrite('results/task2_result.jpg',result_image)
