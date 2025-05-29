from utils import read_frames, write_frames
from tracking import PlayerTracker, BallTracker
from draw import PlayerTrackDrawer, BallTracksDrawer
from team_classifier import TeamClassifier

def main():

    frames = read_frames("input_videos/video_1.mp4")

    player_tracker = PlayerTracker("models/player_detector.pt")
    player_tracks = player_tracker.get_object_tracks(frames, 
                                                     read_from_stub=True,
                                                     stub_path="stubs/player_track_stubs.pkl"
                                                    )
    
    ball_tracker = BallTracker("models/ball_detector_model.pt")
    ball_tracks = ball_tracker.get_object_tracks(frames,
                                                 read_from_stub=True,
                                                 stub_path="stubs/ball_track_stubs.pk1")
    
    ball_tracks = ball_tracker.validate_detection(ball_tracks)
    ball_tracks = ball_tracker.interpolate_ball_position(ball_tracks)

    team_classifier = TeamClassifier()
    team_classifications = team_classifier.get_player_teams_across_frames(frames,
                                                                          player_tracks,
                                                                          read_from_stub=True,
                                                                          stub_path="stubs/player_assignment_stub.pkl"
                                                                        )
    
    player_tracks_drawer = PlayerTrackDrawer()
    ball_tracks_drawer = BallTracksDrawer()

    output_frames = player_tracks_drawer.draw(frames, 
                                              player_tracks,
                                              team_classifications
                                              )
    output_frames = ball_tracks_drawer.draw(output_frames, 
                                            ball_tracks
                                            )

    write_frames(output_frames, "output_videos/output_video1.avi")


if __name__ == '__main__':
    main()