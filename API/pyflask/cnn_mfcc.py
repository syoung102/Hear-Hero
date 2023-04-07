from keras.models import load_model
import librosa
import numpy as np
from sklearn.preprocessing import LabelEncoder
import os
from pydub import AudioSegment

max_pad_len = 1287
model = load_model('pyflask\static\h5\mfcc_CNN.h5')
le = LabelEncoder()

def convert_to_wav(file_path):
    _, ext = os.path.splitext(file_path)
    ext = ext.lower()
    
    if ext == '.wav':
        return file_path
    try:
        # AudioSegment를 이용하여 파일 로드
        audio = AudioSegment.from_file(file_path, format=ext[1:])
        
        # 원본 파일과 동일한 이름으로 wav 파일로 저장
        output_file = os.path.splitext(file_path)[0] + '.wav'
        audio.export(output_file, format='wav')
        
        # 원본 파일 삭제
        os.remove(file_path)
        
        print(f"{file_path} 파일을 wav로 변환하여 {output_file} 파일로 저장했습니다.")

        return output_file
    except Exception as e:
        print(f"{file_path} 파일 변환 실패: {str(e)}")

def extract_mfcc(file_name):
    try:
        audio, sr = librosa.load(file_name) 
        mfccs = librosa.feature.mfcc(y=audio, sr=sr, n_mfcc=40)
        # 패딩
        pad_width = max_pad_len - mfccs.shape[1]
        mfccs = np.pad(mfccs, pad_width = ((0, 0), (0, pad_width)), mode = 'constant')
    except Exception as e:
        print("Error encountered while parsing file: ", file_name)
        return None 
    return mfccs

def print_prediction(file_name):
    prediction_feature = extract_mfcc(file_name)
    prediction_feature = np.reshape(prediction_feature, (-1, 40, 1287, 1))
    predict_x = model.predict(prediction_feature)
    predicted_vector = np.argmax(predict_x,axis=1)
    predicted_class = le.inverse_transform(predicted_vector) 
    
    find  = 0
    predicted_proba = predict_x[0]
    for i in range(len(predicted_proba)): 
        category = le.inverse_transform(np.array([i]))
        print(category[0], "\t\t : ", format(predicted_proba[i], '.32f'))
        if (predicted_proba[i]) > 0.8:
          find = 1

    if find == 1:
      print("The predicted class is:", predicted_class[0], '\n')
      return predicted_class[0] 
    else:
      print("Nothing\n")
      return -1
    

def main(file_name, selected_sound):
    output_file = convert_to_wav(file_name)
    result = print_prediction(output_file)
    if result == -1:
        return -1
    for i in selected_sound:
       if result == i:
          return result
    return -1
