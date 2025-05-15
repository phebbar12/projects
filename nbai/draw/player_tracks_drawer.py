from .utils import draw_ellipse

class PlayerTrackDrawer:

    def __init__(self):
        pass

    def draw(self, frames, tracks):

        output_frames = []

        for frame_num, frame in enumerate(frames):
            frame = frame.copy()

            player_dict = tracks[frame_num]

            for track_id, player_bb in player_dict.items():

                frame = draw_ellipse(frame, player_bb['bbox'], (0, 0, 255), track_id)

            output_frames.append(frame)

        return output_frames

