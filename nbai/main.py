from utils import read_frames, write_frames
from tracking import PlayerTracker

def main():

    frames = read_frames("input_videos/video_1.mp4")

    player_tracker = PlayerTracker("models/player_detector.pt")
    player_tracks = player_tracker.get_object_tracks(frames)
    
    print(player_tracks)

    write_frames(frames, "output_videos/output_video1.avi")


if __name__ == '__main__':
    main()