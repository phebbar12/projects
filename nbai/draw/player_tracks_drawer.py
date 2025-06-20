from .utils import draw_ellipse, draw_pointer

class PlayerTrackDrawer:

    def __init__(self, team_1_color=[255, 245, 238], team_2_color=[128, 0, 0]):
        self.default_team = 1
        self.team_1_color = team_1_color
        self.team_2_color = team_2_color

    def draw(self, frames, tracks, player_assignments, ball_possessions):

        output_frames = []

        for frame_num, frame in enumerate(frames):
            frame = frame.copy()

            player_dict = tracks[frame_num]

            player_assignment = player_assignments[frame_num]

            possession_player_id = ball_possessions[frame_num]

            for track_id, player_bb in player_dict.items():
                team_id = player_assignment.get(track_id, self.default_team)

                if team_id == 1:
                    color = self.team_1_color
                else:
                    color = self.team_2_color

                if track_id == possession_player_id:
                    frame = draw_pointer(frame, player_bb['bbox'], [0, 0, 255])
                    
                frame = draw_ellipse(frame, player_bb['bbox'], color, track_id)

            output_frames.append(frame)

        return output_frames

