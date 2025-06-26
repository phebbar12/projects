class PassInterceptDetector:

    def __init__(self):
        pass

    def detect_passes(self, team_assignments, ball_possessions):
        passes = [-1] * len(ball_possessions)
        prev_holder = -1
        prev_frame = -1

        for frame in range(1, len(ball_possessions)):

            if ball_possessions[frame - 1] != -1:
                prev_holder = ball_possessions[frame - 1]
                prev_frame = frame - 1

            current_holder = ball_possessions[frame]

            if prev_holder != -1 and current_holder != -1 and prev_holder != current_holder:
                prev_team = team_assignments[prev_frame].get(prev_holder, -1)
                current_team = team_assignments[frame].get(current_holder, -1)

                if prev_team == current_team and prev_team != -1:
                    passes[frame] = prev_team
        
        return passes

    
    def detect_interecpts(self, team_assignments, ball_possessions):
        interceptions = [-1] * len(ball_possessions)
        prev_holder = -1
        prev_frame = -1

        for frame in range(1, len(ball_possessions)):

            if ball_possessions[frame - 1] != -1:
                prev_holder = ball_possessions[frame - 1]
                prev_frame = frame - 1

            current_holder = ball_possessions[frame]

            if prev_holder != -1 and current_holder != -1 and prev_holder != current_holder:
                prev_team = team_assignments[prev_frame].get(prev_holder, -1)
                current_team = team_assignments[frame].get(current_holder, -1)

                if prev_team != current_team and prev_team != -1 and current_team != -1:
                    interceptions[frame] = current_team

        return interceptions