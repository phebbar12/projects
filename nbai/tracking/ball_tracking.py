from ultralytics import YOLO
import supervision as sv
import sys
sys.path.append("../")
from utils import read_stub, write_stub

class BallTracker:
    
    def __init__(self, model_path):
        self.model = YOLO(model_path)

    def detect_frames(self, frames):
        batch_size = 30
        detections = []
        for i in range(0, len(frames), batch_size):
            batch_frames = frames[i:i + batch_size]
            batch_detections = self.model.predict(batch_frames, conf=0.5)
            detections+=batch_detections
        
        return detections

    def get_object_tracks(self, frames, read_from_stub=False, stub_path=None):

        tracks = read_stub(read_from_stub, stub_path)
        if tracks is not None:
            if len(tracks) == len(frames):
                return tracks
        
        detections = self.detect_frames(frames)
        tracks = []

        for frame_num, detection in enumerate(detections):
            cls_names = detection.names
            cls_names_inv = {v:k for k,v in cls_names.items()}

            detection_supervision = sv.Detections.from_ultralytics(detection)
            tracks.append({})
            
            best_bbox = None
            best_confidence = 0

            for frame_detection in detection_supervision:
                bbox = frame_detection[0].tolist()
                cls_id = frame_detection[3]
                confidence = frame_detection[2]

                if cls_id == cls_names_inv["Ball"]:
                    if best_confidence < confidence:
                        best_confidence = confidence
                        best_bbox = bbox

            if best_bbox is not None:
                tracks[frame_num][1] = {"bbox": best_bbox}

        write_stub(stub_path, tracks)

        return tracks