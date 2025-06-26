from copy import deepcopy
from .homography import Homography
import cv2
import numpy as np
import sys
sys.path.append("../")
from utils import measure_distance, get_bottom_of_bbox

class BirdsEyeMapper:

    def __init__(self, court_image_path):
        self.court_image_path = court_image_path
        self.width = 300
        self.height = 161

        self.court_width_meters = 28
        self.court_height_meters = 15

        self.key_points = [
            # left edge
            (0,0),
            (0,int((0.91/self.court_height_meters)*self.height)),
            (0,int((5.18/self.court_height_meters)*self.height)),
            (0,int((10/self.court_height_meters)*self.height)),
            (0,int((14.1/self.court_height_meters)*self.height)),
            (0,int(self.height)),

            # Middle line
            (int(self.width/2),self.height),
            (int(self.width/2),0),
            
            # Left Free throw line
            (int((5.79/self.court_width_meters)*self.width),int((5.18/self.court_height_meters)*self.height)),
            (int((5.79/self.court_width_meters)*self.width),int((10/self.court_height_meters)*self.height)),

            # right edge
            (self.width,int(self.height)),
            (self.width,int((14.1/self.court_height_meters)*self.height)),
            (self.width,int((10/self.court_height_meters)*self.height)),
            (self.width,int((5.18/self.court_height_meters)*self.height)),
            (self.width,int((0.91/self.court_height_meters)*self.height)),
            (self.width,0),

            # Right Free throw line
            (int(((self.court_width_meters-5.79)/self.court_width_meters)*self.width),int((5.18/self.court_height_meters)*self.height)),
            (int(((self.court_width_meters-5.79)/self.court_width_meters)*self.width),int((10/self.court_height_meters)*self.height)),
        ]

    def validate(self, key_points):

        key_points = deepcopy(key_points)
        
        for frame_num, frame_keypoints in enumerate(key_points):
            frame_keypoints = frame_keypoints.xy.tolist()[0]
            
            detected_keypoints_indices = [i for i, kp in enumerate(frame_keypoints) if kp[0] > 0 and kp[1] > 0]
            
            if len(detected_keypoints_indices) < 3:
                continue

            invalid_keypoints = []

            for i in detected_keypoints_indices:
                if frame_keypoints[i][0] == 0 and frame_keypoints[i][1] == 0:
                    continue
                
                other_indices = [idx for idx in detected_keypoints_indices if idx != i and idx not in invalid_keypoints]

                if len(other_indices) < 2:
                    continue
                    
                j,k = other_indices[0], other_indices[1]

                d_ij = measure_distance(frame_keypoints[i], frame_keypoints[j])
                d_ik = measure_distance(frame_keypoints[i], frame_keypoints[k])

                t_ij = measure_distance(self.key_points[i], self.key_points[j])
                t_ik = measure_distance(self.key_points[i], self.key_points[k])
                
                if t_ij > 0 and t_ik > 0:
                    prop_detected = d_ij/d_ik if d_ik > 0 else float('inf')
                    prop_tactical = t_ij/t_ik if t_ik > 0 else float('inf')

                    error = (prop_detected - prop_tactical) / prop_tactical
                    error = abs(error)
                    if error > 0.8:
                        key_points[frame_num].xy[0][i] *= 0
                        key_points[frame_num].xyn[0][i] *= 0
                        invalid_keypoints.append(i)

        return key_points
    
    def transform_players(self, key_points, player_tracks):
        tactical_player_positions = []

        for frame_idx, (frame_keypoints, frame_tracks) in enumerate(zip(key_points, player_tracks)):
            
            tactical_positions = {}

            frame_keypoints = frame_keypoints.xy.tolist()[0]

            if frame_keypoints is None or len(frame_keypoints) == 0:
                tactical_player_positions.append(tactical_positions)
                continue

            detected_key_points = frame_keypoints

            valid_idx = [i for i, keypoint in enumerate(detected_key_points) if keypoint[0] > 0 and keypoint[1] > 0]

            if len(valid_idx) < 4:
                tactical_player_positions.append(tactical_positions)
                continue

            source_pts = np.array([detected_key_points[i] for i in valid_idx], dtype=np.float32)
            target_pts = np.array([self.key_points[i] for i in valid_idx], dtype=np.float32)

            try:
                homography = Homography(source_pts, target_pts)

                for player_id, player in frame_tracks.items():
                    bbox  = player["bbox"]
                    player_position = np.array([get_bottom_of_bbox(bbox)])
                    
                    tactical_position = homography.transform_points(player_position)
                    tactical_positions[player_id] = tactical_position[0].tolist()

            except (ValueError, cv2.error) as e:
                pass
                
            tactical_player_positions.append(tactical_positions)
        return tactical_player_positions

    

