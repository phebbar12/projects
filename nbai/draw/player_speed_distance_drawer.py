import cv2
import sys
import sys
sys.path.append("../")
from utils import get_bottom_of_bbox

class PlayerSpeedDistanceDrawer:

    def __init__(self):
        pass

    def draw(self, frames, player_tracks, player_distances, player_speeds):

        output_frames = []
        total_distances = {}

        for frame, player_track, player_distance, player_speed in zip(frames, player_tracks, player_distances, player_speeds):
            output_frame = frame.copy()

            for player_id, distance in player_distance.items():
                if player_id not in total_distances:
                    total_distances[player_id] = 0
                
                total_distances[player_id] += distance

            for player_id, bbox in player_track.items():
                x1,y1,x2,y2 = bbox['bbox']
                position = [int((x1+x2)/2),int(y2)]
                position[1]+=40

                distance = total_distances.get(player_id, None)
                speed = player_speed.get(player_id, None)

                if speed is not None:
                    cv2.putText(output_frame, f"{speed:.2f} km/h", position, cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)

                if distance is not None:
                    cv2.putText(output_frame, f"{distance:.2f} m", (position[0], position[1]+20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)

            output_frames.append(output_frame)
        
        return output_frames
