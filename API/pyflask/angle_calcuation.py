def calculate(angle1, angle2):
    if (angle1>=0 and angle1<45):
        print("2 or 7")
        if (angle2>=0) :
            predict_direction =2
        else :
            predict_direction = 7
    elif(angle1>=45):   
        print("1 or 8")
        if (angle2>=0) :
            predict_direction =1
        else :
            predict_direction = 8
    elif(angle1<=0 and angle1>-45):  
        print("3 or 6")
        if (angle2>=0) :
            predict_direction =3
        else :
            predict_direction = 6
    elif(angle1<=-45):  
        print("4 or 5")
        if (angle2>=0) :
            predict_direction = 4
        else :
            predict_direction = 5
    return predict_direction

