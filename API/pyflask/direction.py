import tensorflow as tf
import numpy as np
from pydub import AudioSegment
import librosa
import scipy.signal as signal
import os
import wave

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

def check_audio_type(filepathT):
    file_path = convert_to_wav(filepathT)
    with wave.open(file_path, 'rb') as wave_file:
        num_channels = wave_file.getnchannels()
    if num_channels == 1:
        return 1
    elif num_channels == 2:
        return 2
    else:
        raise ValueError("Unsupported number of channels")

def main(file_pathT):
    file_path = convert_to_wav(file_pathT)
    # 오디오 파일 Load
    audio_file = AudioSegment.from_file(file_path)

    # 두개의 모노 오디오 파일로 분리
    left_channel = audio_file.split_to_mono()[0]
    right_channel = audio_file.split_to_mono()[1]

    # Save the mono audio files
    left_channel.export(os.path.join("pyflask/static/recoding", str(os.path.basename(file_path)) + "left.wav"), format="wav")
    right_channel.export(os.path.join("pyflask/static/recoding", str(os.path.basename(file_path)) + "right.wav"), format="wav")

    signal1, sr1 = librosa.load(os.path.join("pyflask/static/recoding", str(os.path.basename(file_path)) + "left.wav"), sr=None)

    signal2, sr2 = librosa.load(os.path.join("pyflask/static/recoding", str(os.path.basename(file_path)) + "right.wav"), sr=None)

    corr = signal.correlate(signal1, signal2, mode='full')

    max_index = np.argmax(corr)
    time_delay = (max_index - len(signal2) + 1) / sr1

    distance = 0.15  # 마이크 사이 거리 (갤럭시 S21 기준)
    speed_of_sound = 343  # 소리 속도 m/s
    angle = np.arcsin(np.clip(time_delay * speed_of_sound / distance, -1, 1)) * 180 / np.pi

    print("Direction of sound:", angle, "degrees")
    
    return angle