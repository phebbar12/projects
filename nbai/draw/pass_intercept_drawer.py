import cv2
import numpy as np

class PassInterceptDrawer:

    def __init__(self):
        pass

    def get_stats(self, passes, intercepts):

        passes = np.array(passes)
        intercepts = np.array(intercepts)
        team_1_passes = passes[passes == 1].shape[0]
        team_2_passes = passes[passes == 2].shape[0]

        team_1_intercepts= intercepts[intercepts == 1].shape[0]
        team_2_intercepts = intercepts[intercepts == 2].shape[0]

        return team_1_passes, team_1_intercepts, team_2_passes, team_2_intercepts
        




    def draw_frame(self, frame, frame_num, passes, intercepts):

        overlay = frame.copy()
        font_scale = 0.7
        font_thickness = 2

        frame_height, frame_width = overlay.shape[:2]
        rect_x1 = int(frame_width * 0.16)
        rect_x2 = int(frame_width * 0.56)
        rect_y1 = int(frame_height * 0.75)
        rect_y2 = int(frame_height * 0.90)

        text_x = int(frame_width * 0.19)
        text_y1 = int(frame_height * .80)
        text_y2 = int(frame_height * .88)

        cv2.rectangle(overlay, (rect_x1, rect_y1), (rect_x2, rect_y2), (255, 255, 255), -1)
        cv2.addWeighted(overlay, 0.8, frame, 0.2, 0, frame)

        passes_till_frame = passes[:frame_num + 1]
        intercepts_till_frame = intercepts[:frame_num + 1]

        t1_passes, t1_intercepts, t2_passes, t2_intercepts = self.get_stats(passes_till_frame, intercepts_till_frame)
        

        cv2.putText(frame, f"Team 1 -- Passes: {t1_passes} Intercepts: {t1_intercepts}", (text_x, text_y1), cv2.FONT_HERSHEY_SIMPLEX, font_scale, (0, 0, 0), font_thickness)
        cv2.putText(frame, f"Team 2 -- Passes: {t2_passes} Intercepts: {t2_intercepts}", (text_x, text_y2), cv2.FONT_HERSHEY_SIMPLEX, font_scale, (0, 0, 0), font_thickness)

        return frame

    def draw(self, frames, passes, intercepts):
        output_frames = []

        for frame_num, frame in enumerate(frames):

            frame_drawn = self.draw_frame(frame, frame_num, passes, intercepts)
            output_frames.append(frame_drawn)
        
        return output_frames
        