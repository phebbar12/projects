from ultralytics import YOLO
import sys
sys.path.append("../")
from utils import read_stub, write_stub

class CourtKeyPointDetector:

    def __init__(self, model_path):
        self.model = YOLO(model_path)

    def get_court_key_points(self, frames, read_from_stub=False, stub_path=None):
        
        court_key_points = read_stub(read_from_stub, stub_path)

        if court_key_points and len(court_key_points) == len(frames):
            return court_key_points

        batch_size = 20
        court_key_points = []

        for i in range(0, len(frames), batch_size):

            detection_batch = self.model.predict(frames[i: i + batch_size],conf=0.5)
            for detection in detection_batch:
                court_key_points.append(detection.keypoints)

        write_stub(stub_path, court_key_points)

        return court_key_points
    

