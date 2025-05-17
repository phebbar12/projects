from PIL import Image
from transformers import CLIPProcessor, CLIPModel
import cv2

class TeamClassifier():

    def __init__(self, 
                 team_1_class_name="light jersey",
                 team_2_class_name="dark jersey"
                 ):

        self.team_1_class_name = team_1_class_name
        self.team_2_class_name = team_2_class_name

        self.player_classifications = {}

    def init_model(self):
        self.model = CLIPModel.from_pretrained("patrickjohncyh/fashion-clip")
        self.processor = CLIPProcessor.from_pretrained("patrickjohncyh/fashion-clip")
    
    def classify_player(self, frame, bbox):
        image = frame[int(bbox[1]):int(bbox[3]), int(bbox[0]):int(bbox[2])]

        rgb_image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        pil_image = Image.fromarray(rgb_image)

        classes = [self.team_1_class_name, self.team_2_class_name]
        inputs = self.processor(text=classes, images=pil_image, return_tensors="pt", padding=True)

        outputs = self.model(**inputs)
        logits_per_image = outputs.logits_per_image
        probs = logits_per_image.softmax(dim=1)

        class_name = classes[probs.argmax(dim=1)[0]]
        return class_name


    def get_team(self, frame, player_bbox, player_id): 

        if player_id in self.player_team_dict:
            return self.player_classifications[player_id]


        player_color = self.classify_player(frame, player_bbox)

        team_id=2

        if player_color == self.team_1_class_name:
            team_id=1
        
        self.player_classifications[player_id]=team_id
        return team_id
    
    def classify_players_across_frames(self, frames, tracks, read_from_stub=False, stub_path=None):

        self.init_model()

        for frame_num, track in enumerate(tracks):

            for track_id, detection in track.items():
                team = self.get_team(frames[frame_num], )

    
