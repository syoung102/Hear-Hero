import tensorflow as tf
import numpy as np
from pydub import AudioSegment
import librosa
import scipy.signal as signal
import os

def main(file_path, selected_sound):
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