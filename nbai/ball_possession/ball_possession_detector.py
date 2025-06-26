import sys
sys.path.append("../")

from utils import measure_distance, get_center_of_bbox

class BallPossessionDetector:
    
    def __init__(self):
        self.possesion_threshold = 50 
        self.min_frames = 13
        self.overlap_threshold = 0.8

    def get_critical_point(self, player_bbox, ball_center):
        ball_center_x = ball_center[0]
        ball_center_y = ball_center[1]
        x1, y1, x2, y2 = player_bbox
        width = x2 - x1
        height = y2 - y1

        closest = []

        if ball_center_y > y1 and ball_center_y < y2:
            closest.append((x1, ball_center_y))
            closest.append((x2, ball_center_y))
        
        if ball_center_x > x1 and ball_center_x < x2:
            closest.append((ball_center_x, y1))
            closest.append((ball_center_x, y2))
        
        closest += [
            (x1, y1),
            (x1, y2),
            (x2, y1),
            (x2, y2),
            (x1, y1 + height//2),
            (x2, y1 + height//2),
            (x1 + width//2, y1),
            (x1 + width//2, y1)
            
        ]

        return closest

    def get_closest_distance(self, ball_center, player_bbox):
        critical_points = self.get_critical_point(player_bbox, ball_center)

        min = float("inf")

        for critical_point in critical_points:
            distance = measure_distance(ball_center, critical_point)
            if min > distance:
                min = distance
        
        return min
    
    def calculate_overlap(self, player_bbox, ball_bbox):
        px1, py1, px2, py2 = player_bbox
        bx1, by1, bx2, by2 = ball_bbox
        
        ball_area = (bx2-bx1)*(by2-by1)

        intersection_x1 = max(px1, bx1)
        intersection_y1 = max(py1, by1)
        intersection_x2 = min(px2, bx2)
        intersection_y2 = min(py2, by2)

        if intersection_x2 < intersection_x1 or intersection_y2 < intersection_y1:
            return 0

        intersection_area = (intersection_x2 - intersection_x1) * (intersection_y2 - intersection_y1)

        overlap = intersection_area/ball_area
        return overlap
    
    def find_closest_player(self, ball_center, player_tracks_frame, ball_bbox):

        high_overlap_players = []
        player_distances = []

        for player_id, player_info in player_tracks_frame.items():
            player_bbox = player_info.get("bbox", [])
            if not player_bbox:
                continue

            overlap = self.calculate_overlap(player_bbox, ball_bbox)
            distance = self.get_closest_distance(ball_center, player_bbox)

            if overlap > self.overlap_threshold:
                high_overlap_players.append((player_id, overlap))
            else:
                player_distances.append((player_id, distance))

        if high_overlap_players:
            most_overlap = max(high_overlap_players, key=lambda x: x[1])
            return most_overlap[0]

        if player_distances:
            closest_candidate = min(player_distances, key = lambda x: x[1])
            if closest_candidate[1] < self.possesion_threshold:
                return closest_candidate[0]
        
        return -1
    
    def detect_ball_possession(self, player_tracks, ball_tracks):
        frames = len(ball_tracks)
        possessions = [-1] * frames

        possession_frames = {}

        for frame in range(frames):
            ball_info = ball_tracks[frame].get(1, {})
            if not ball_info:
                continue

            ball_bbox = ball_info.get("bbox", [])
            if not ball_bbox:
                continue

            ball_center = get_center_of_bbox(ball_bbox)

            best_player_id = self.find_closest_player(ball_center, player_tracks[frame], ball_bbox)
            if best_player_id != -1:
                consecutive_frames = possession_frames.get(best_player_id, 0) + 1
                possession_frames = {best_player_id: consecutive_frames}

                if consecutive_frames >= self.min_frames:
                    possessions[frame] = best_player_id
            else:
                possession_frames = {}

        return possessions
            
                








        
