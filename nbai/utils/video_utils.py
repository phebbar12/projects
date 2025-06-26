import cv2
import os

def read_frames(videopath):
    vcap = cv2.VideoCapture(videopath)
    frames = []
    while True:
        ret, frame = vcap.read()
        if not ret:
            break
        frames.append(frame)
    
    return frames

def write_frames(frames, videopath):
    if not os.path.exists(os.path.dirname(videopath)):
        os.mkdir(os.path.dirname(videopath))
    
    vwriter = cv2.VideoWriter_fourcc(*"MPV4")
    out = cv2.VideoWriter(videopath, vwriter, 24.0, (frames[0].shape[1], frames[0].shape[0]))
    for frame in frames:
        out.write(frame)
    out.release()
