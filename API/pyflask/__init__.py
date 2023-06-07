import os, sys
import json
from flask import Flask, render_template, request
from . import cnn_mfcc
from . import direction
from . import angle_calcuation
from http import HTTPStatus
from flask import Flask, jsonify, redirect, render_template, request, url_for

app = Flask(__name__)
app.debug = True

@app.route('/mfcc', methods=['POST'])
def mfcc_post():
    record = request.files.get("record")
    record.save('./static/recording/' + str(record.filename))
    record_path = './static/recording/' + str(record.filename)
                                             
    s_sound_json = request.form.get("s_sound")
    data = json.loads(s_sound_json)

    s_sound = []
    if data['s_sound']['KNOCKING']:
        s_sound.append(1)
    if data['s_sound']['BARKING']:
        s_sound.append(2)
    if data['s_sound']['CAR HORN']:
        s_sound.append(3)
    if data['s_sound']['SIREN']:
        s_sound.append(4)
    if data['s_sound']['RINGTONE']:
        s_sound.append(5)
    if data['s_sound']['SCREAMING']:
        s_sound.append(6)
    if data['s_sound']['FIRE ALARM']:
        s_sound.append(7)
    
    predict_result = cnn_mfcc.main(record_path, s_sound)

    return jsonify({"r_result": predict_result, "status": HTTPStatus.OK})

@app.route('/direction', methods=['POST'])
def direction_post():
    record1 = request.files.get("record1")
    record1.save('./static/recoding/' + str(record1.filename))
    record1_path = './static/recoding/' + str(record1.filename)

    record2 = request.files.get("record2")
    record2.save('./static/recoding/' + str(record2.filename))
    record2_path = './static/recoding/' + str(record2.filename)
                                             
    s_sound_json = request.form.get("s_sound")
    data = json.loads(s_sound_json)

    s_sound = []
    if data['sound']['KNOCKING']:
        s_sound.append(1)
    if data['sound']['BARKING']:
        s_sound.append(2)
    if data['sound']['CAR HORN']:
        s_sound.append(3)
    if data['sound']['SIREN']:
        s_sound.append(4)
    if data['sound']['RINGTONE']:
        s_sound.append(5)
    if data['sound']['SCREAMING']:
        s_sound.append(6)
    if data['sound']['FIRE ALARM']:
        s_sound.append(7)

    if direction.check_audio_type(record1_path)!=2 or direction.check_audio_type(record2_path)!=2:
        return jsonify({"d_result": -2, "status": HTTPStatus.OK})

    predict_result1 = cnn_mfcc.main(record1_path, s_sound)
    predict_result2 = cnn_mfcc.main(record2_path, s_sound)

    if (predict_result1==predict_result2) & (predict_result1 != -1):
        angle1 = direction.main(record1_path)
        angle2 = direction.main(record2_path)
    else:
        return jsonify({"result": -1, "status": HTTPStatus.OK})
    
    predict_direction = angle_calcuation.calculate(angle1, angle2)
    
    
    return jsonify({"d_result": predict_direction, "status": HTTPStatus.OK})

@app.route('/test', methods=['POST'])
def test_get():           
    data = request.get_json()
    
    return jsonify({"testValue": data, "status": HTTPStatus.OK})
