def calculate(angle1, angle2):
    if angle1 < -45:
            print("1또는 8")
            if angle2 < 0:
                predict_direction = 1
            else:
                predict_direction = 8
    elif  angle1 > 45:
        print("4또는 5")
        if angle2 < 0:
            predict_direction = 4
        else:
            predict_direction = 5
    elif angle1 < 0:
        print("2또는 7")
        if angle2 <= 0:
            predict_direction = 2
        else:
            predict_direction = 7
    elif angle1 >= 0:
        print("3또는 6")
        if angle2 < 0:
            predict_direction = 3
        else:
            predict_direction = 6

    return predict_direction

