"""
Denoise Problem
(Due date: Nov. 25, 11:59 P.M., 2019)
The goal of this task is to denoise image using median filter.

Do NOT modify the code provided to you.
Do NOT import ANY library or API besides what has been listed.
Hint:
Please complete all the functions that are labeled with '#to do'.
You are suggested to use utils.zero_pad.
"""


import utils
import numpy as np
import json

def median_filter(img):
    """
    Implement median filter on the given image.
    Steps:
    (1) Pad the image with zero to ensure that the output is of the same size as the input image.
    (2) Calculate the filtered image.
    Arg: Input image.
    Return: Filtered image.
    """
    img = utils.zero_pad(img,1,1)
    median_list = list()
    temp_img = np.zeros((512,512))
    for i in range(0,512):
        for j in range(0,512):
            temp_list = list()
            part = img[i:i+3,j:j+3]
            #print ("current iteration",i,j)
            for x in range(0,3):
                for y in range(0,3):
                    temp_list.append(part[x,y])
            temp_list.sort()
            median = temp_list[4]
            temp_img[i,j] = median
            median_list.append(median)

    # print (temp_img.dtype)
    temp_img = temp_img.astype('uint8')
    # print (temp_img.dtype)
    return temp_img




    # for p in range(i,i+3):
    #     for q in range(j,j+3):
    #         img[p,q] = median


    # TODO: implement this function.

def mse(img1, img2):
    """
    Calculate mean square error of two images.
    Arg: Two images to be compared.
    Return: Mean square error.
    """
    loss = 0
    for i in range(0,512):
        for j in range(0,512):
            loss = loss + (img1[i,j] - img2[i,j]) ** 2

    loss = loss / 512
    # print (loss)

    return loss





if __name__ == "__main__":
    img = utils.read_image('lenna-noise.png')
    gt = utils.read_image('lenna-denoise.png')

    result = median_filter(img)
    error = mse(gt, result)

    with open('results/task2.json', "w") as file:
        json.dump(error, file)
    utils.write_image(result,'results/task2_result.jpg')
