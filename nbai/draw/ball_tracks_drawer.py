from .utils import draw_pointer

class BallTracksDrawer:

    def __init__(self):
        self.pointer_color = (0, 255, 0)

    def draw(self, frames, tracks):
        output_frames = []
        for frame_num, frame in enumerate(frames):
            output_frame = frame.copy()
            ball_dict = tracks[frame_num]
            for _, track in ball_dict.items():
                bbox = track["bbox"]

                if bbox is None:
                    continue

                output_frame = draw_pointer(frame, bbox, self.pointer_color)
            output_frames.append(output_frame)
        return output_frames