#Backend

## Game life cycle

A game is seperated in repeating rounds.
Each round has multiple phase denoted by the `gamestate.roundroundPhase` enum.

- `NO_ACTIVE_GAME_PHASE`:  The game has not started. No one has pressed "Start game". Technical not part of a round.
- `TEAM_A_THROWING_PHASE`: Team A (left) is allowed to trow.
- `TEAM_B_THROWING_PHASE`: Team B (right) is allowed to throw.
- `RESTING_PHASE`: One of the teams has thrown and an active throw is in progress (videos are played) followed by the resting period. The next throwing phase will start after a delay.
- `TEAM_A_WON_PHASE`: Team A has won. Technical not part of a round. 
- `TEAM_B_WON_PHASE`: Team B has won. Technical not part of a round.