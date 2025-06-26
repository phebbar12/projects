import sys
sys.path.append("../")
from utils.bbox_utils import measure_distance

class PlayerVelocity:

    def __init__(self, width, height, width_meters, height_meters):
        self.width = width
        self.height = height
        self.width_meters = width_meters
        self.height_meters = height_meters

    def calculate_meter_distance(self, previous_pixel_positions, current_pixel_positions):
        previous_pixel_x, previous_pixel_y = previous_pixel_positions
        current_pixel_x, current_pixel_y = current_pixel_positions

        previous_meter_x = previous_pixel_x * self.width_meters / self.width
        previous_meter_y = previous_pixel_y * self.height_meters / self.height

        current_meter_x = current_pixel_x * self.width_meters / self.width
        current_meter_y = current_pixel_y * self.height_meters / self.height

        meter_distance = measure_distance((previous_meter_x, previous_meter_y), (current_meter_x, current_meter_y))
        meter_distance = meter_distance*0.4
        return meter_distance


    def calculate_distance(self, tactical_player_positions):
        output_distances = []
        previous_positions = {}

        for frame_num, tactical_player_position in enumerate(tactical_player_positions):

            output_distances.append({})

            for player_id, current_player_position in tactical_player_position.items():
                if player_id in previous_positions:
                    previous_player_position = previous_positions[player_id]
                    distance = self.calculate_meter_distance(current_player_position, previous_player_position)
                    output_distances[frame_num][player_id] = distance
                
                previous_positions[player_id] = current_player_position

        return output_distances
        
    def calculate_speed(self, distances, frame_rate=30):
        speeds = []
        window_size = 5

        for frame_idx in range(len(distances)):
            speeds.append({})

            for player_id in distances[frame_idx].keys():
                start_idx = max(0, frame_idx -(window_size * 3) + 1)
                total_distance = 0
                frames_present = 0

                last_frame_present = None

                for i in range(start_idx, frame_idx):
                    if player_id in distances[i]:
                        if last_frame_present is not None:
                            total_distance += distances[i][player_id]
                            frames_present += 1
                        last_frame_present = i

                if frames_present >= window_size:
                    time_seconds = frames_present / frame_rate
                    time_hours = time_seconds / 3600
                    if time_hours >= 0:
                        speed_km = (total_distance/ 1000) / time_hours
                        speeds[frame_idx][player_id] = speed_km
                    else:
                        speeds[frame_idx][player_id] = 0
                else:
                    speeds[frame_idx][player_id] = 0
        
        return speeds

                