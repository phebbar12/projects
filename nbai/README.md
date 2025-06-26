# Basketball Clip Analyzer

The **Basketball Clip Analyzer** is a computer vision tool that uses YOLO-based object detection models to analyze basketball gameplay. It detects players, the ball, and court keypoints, performs perspective transformations, and estimates player movement metrics such as speed and distance traveled.

##  Features

- **YOLO Object Detection**: Detects players, basketball, and court lines/keypoints.
- **Zero-Shot Classification**: Automatically assigns players to teams using jersey color descriptors.
- **Homography & Court Perspective Transformation**: Transforms detected positions into real-world court coordinates.
- **Metric Estimation**: Roughly estimates player speed and distance traveled based on NBA court dimensions.

##  Usage

```bash
python main.py "input_video_filepath" "team_1_jersey_color" "team_2_jersey_color"
```

### Example

```bash
python main.py input_videos/video_1.mp4 "white shirt" "dark blue shirt"
```