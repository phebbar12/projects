
class BallPosessionDetector:
    
    def __init__(self):
        self.possesion_threshold = 50 
        self.min_frames = 11
        self.containment_threshold = 0.8

    def get_closest_bbox_point(self, player_bbox, ball_center):
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
            # Finish other key points
        ]