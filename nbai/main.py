import argparse
import os
from utils import read_frames, write_frames
from tracking import PlayerTracker, BallTracker
from draw import PlayerTrackDrawer, BallTracksDrawer, TeamBallControlDrawer, PassInterceptDrawer, CourtKeyPointDrawer, BirdsEyeViewDrawer, PlayerSpeedDistanceDrawer
from team_classifier import TeamClassifier
from ball_possession import BallPossessionDetector
from pass_intercept_detector import PassInterceptDetector
from court_key_point_detector import CourtKeyPointDetector
from birds_eye_mapping import BirdsEyeMapper
from player_velocity import PlayerVelocity
from config import (
    STUBS_DEFAULT_PATH, 
    PLAYER_DETECTOR_MODEL_PATH, 
    BALL_DETECTOR_MODEL_PATH, 
    KEYPOINT_DETECTOR_MODEL_PATH, 
    OUTPUT_VIDEO_PATH
)



def parse_args():
    parser = argparse.ArgumentParser(description='Basketball Video Analysis')
    parser.add_argument('input_video', type=str, help='Path to input video file')
    parser.add_argument('team_1_color', type=str, help='Path to input video file')
    parser.add_argument('team_2_color', type=str, help='Path to input video file')
    parser.add_argument('--output_video_path', type=str, default=OUTPUT_VIDEO_PATH, 
                        help='Path to output video file')
    parser.add_argument('--stub_path', type=str, default=STUBS_DEFAULT_PATH,
                        help='Path to stub directory')
    return parser.parse_args()

def main():
    args = parse_args()
    frames = read_frames(args.input_video)

    player_tracker = PlayerTracker(PLAYER_DETECTOR_MODEL_PATH)
    player_tracks = player_tracker.get_object_tracks(frames, 
                                                     read_from_stub=True,
                                                     stub_path=os.path.join(args.stub_path, "player_track_stubs.pkl")
                                                    )
    
    ball_tracker = BallTracker(BALL_DETECTOR_MODEL_PATH)
    ball_tracks = ball_tracker.get_object_tracks(frames,
                                                 read_from_stub=True,
                                                 stub_path=os.path.join(args.stub_path, "ball_track_stubs.pkl")
                                                )
    
    court_key_point_detector = CourtKeyPointDetector(KEYPOINT_DETECTOR_MODEL_PATH)
    court_key_points = court_key_point_detector.get_court_key_points(frames, 
                                                                     read_from_stub=True, 
                                                                     stub_path=os.path.join(args.stub_path, "court_key_points_stubs.pkl")
                                                                    )
    
    ball_tracks = ball_tracker.validate_detection(ball_tracks)
    ball_tracks = ball_tracker.interpolate_ball_position(ball_tracks)

    team_classifier = TeamClassifier(args.team_1_color, args.team_2_color)
    team_classifications = team_classifier.get_player_teams_across_frames(frames,
                                                                          player_tracks,
                                                                          read_from_stub=True,
                                                                          stub_path=os.path.join(args.stub_path, "player_assignment_stub.pkl")
                                                                        )
    
    ball_possession_detector = BallPossessionDetector()
    ball_possessions = ball_possession_detector.detect_ball_possession(player_tracks, ball_tracks)

    pass_intercept_detector = PassInterceptDetector()
    passes = pass_intercept_detector.detect_passes(team_classifications, ball_possessions)
    interceptions = pass_intercept_detector.detect_interecpts(team_classifications, ball_possessions)

    birds_eye_view = BirdsEyeMapper(court_image_path="./images/basketball_court.png")
    court_key_points = birds_eye_view.validate(court_key_points)

    tactical_player_positions = birds_eye_view.transform_players(court_key_points, player_tracks)

    speed_distance_calculator = PlayerVelocity(
        birds_eye_view.width,
        birds_eye_view.height,
        birds_eye_view.court_width_meters,
        birds_eye_view.court_height_meters
    )

    player_distances = speed_distance_calculator.calculate_distance(tactical_player_positions)
    player_speeds = speed_distance_calculator.calculate_speed(player_distances)



    player_tracks_drawer = PlayerTrackDrawer()
    ball_tracks_drawer = BallTracksDrawer()
    team_ball_control_drawer = TeamBallControlDrawer()
    pass_intercept_drawer = PassInterceptDrawer()
    court_key_point_drawer = CourtKeyPointDrawer()
    birds_eye_view_drawer = BirdsEyeViewDrawer()
    player_speed_distance_drawer = PlayerSpeedDistanceDrawer()

    output_frames = player_tracks_drawer.draw(frames, 
                                              player_tracks,
                                              team_classifications,
                                              ball_possessions
                                              )
    output_frames = ball_tracks_drawer.draw(output_frames, 
                                            ball_tracks
                                            )
    
    output_frames = team_ball_control_drawer.draw(output_frames, 
                                                  team_classifications, 
                                                  ball_possessions
                                                  )
    
    output_frames= pass_intercept_drawer.draw(output_frames, 
                                              passes, 
                                              interceptions
                                             )

    output_frames = court_key_point_drawer.draw(output_frames,
                                                court_key_points
                                                )
    
    output_frames = birds_eye_view_drawer.draw(output_frames, 
                                               birds_eye_view.court_image_path,
                                               birds_eye_view.width,
                                               birds_eye_view.height,
                                               birds_eye_view.key_points,
                                               tactical_player_positions, 
                                               team_classifications,
                                               ball_possessions
                                               )

    output_frames = player_speed_distance_drawer.draw(output_frames, 
                                                      player_tracks, 
                                                      player_distances, 
                                                      player_speeds
                                                      )

    write_frames(output_frames, args.output_video_path)


if __name__ == '__main__':
    main()