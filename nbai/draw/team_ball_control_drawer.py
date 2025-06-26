import numpy as np
import cv2

class TeamBallControlDrawer():
    def __init__(self):
        pass
    
    def get_team_ball_control(self, player_assignment, ball_possessions):
        team_ball_control = []
        for player_assignment_frame, ball_possession_frame in zip(player_assignment, ball_possessions):
            if ball_possession_frame == -1:
                team_ball_control.append(-1)
                continue

            if ball_possession_frame not in player_assignment_frame:
                team_ball_control.append(-1)
                continue

            if player_assignment_frame[ball_possession_frame] == 1:
                team_ball_control.append(1)
            else:
                team_ball_control.append(2)
            
        team_ball_control = np.array(team_ball_control)
        return team_ball_control

    def draw_frame(self, frame, frame_num, team_ball_control):

        overlay = frame.copy()
        font_scale = 0.7
        font_thickness = 2

        frame_height, frame_width = overlay.shape[:2]
        rect_x1 = int(frame_width * 0.6)
        rect_x2 = int(frame_width * 0.99)
        rect_y1 = int(frame_height * 0.75)
        rect_y2 = int(frame_height * 0.90)

        text_x = int(frame_width * 0.63)
        text_y1 = int(frame_height * .80)
        text_y2 = int(frame_height * .88)

        cv2.rectangle(overlay, (rect_x1, rect_y1), (rect_x2, rect_y2), (255, 255, 255), -1)
        cv2.addWeighted(overlay, 0.8, frame, 0.2, 0, frame)
        
        team_ball_control_till_frame = team_ball_control[:frame_num + 1]
        team_1_possessions = team_ball_control_till_frame[team_ball_control_till_frame==1].shape[0]
        team_2_possessions = team_ball_control_till_frame[team_ball_control_till_frame==2].shape[0]

        team_1_possession_percentage = team_1_possessions / (team_ball_control_till_frame.shape[0])
        team_2_possession_percentage = team_2_possessions / (team_ball_control_till_frame.shape[0])

        cv2.putText(frame, f"Team 1 Ball Control: {team_1_possession_percentage * 100:.2f}%", (text_x, text_y1), cv2.FONT_HERSHEY_SIMPLEX, font_scale, (0, 0, 0), font_thickness)
        cv2.putText(frame, f"Team 2 Ball Control: {team_2_possession_percentage * 100:.2f}%", (text_x, text_y2), cv2.FONT_HERSHEY_SIMPLEX, font_scale, (0, 0, 0), font_thickness)

        return frame


    def draw(self, frames, player_assignment, ball_possessions):
        
        team_ball_control = self.get_team_ball_control(player_assignment, ball_possessions)
        output_frames = []
        for frame_num, frame in enumerate(frames):
            frame_drawn = self.draw_frame(frame, frame_num, team_ball_control)
            output_frames.append(frame_drawn)

        return output_frames

    


        