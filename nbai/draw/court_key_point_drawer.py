import supervision as sv
import numpy as np

class CourtKeyPointDrawer:

    def __init__(self):
        self.keypoint_color = "#ff2c2c"

    def draw(self, frames, court_key_points):
        vertex_annotator = sv.VertexAnnotator(
            color=sv.Color.from_hex(self.keypoint_color),
            radius=8
        )
        vertex_label_annotator = sv.VertexLabelAnnotator(
            color=sv.Color.from_hex(self.keypoint_color),
            text_color=sv.Color.WHITE,
            text_scale=0.5,
            text_thickness=1
        )

        output_frames = []
        for index, frame in enumerate(frames):
            annotate_frame = frame.copy()

            keypoints = court_key_points[index]
            keypoints_np = keypoints.cpu().numpy()
            annotate_frame = vertex_annotator.annotate(scene=annotate_frame, key_points=keypoints_np)
            annotate_frame = vertex_label_annotator.annotate(scene=annotate_frame, key_points=keypoints_np)
            output_frames.append(annotate_frame)
        
        return output_frames