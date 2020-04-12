import grpc
import flunkyprotocol_pb2 as proto
import flunkyprotocol_pb2_grpc
import sys

#channel = grpc.insecure_channel('viings.de:11049')
channel = grpc.insecure_channel('localhost:11049')
stub = flunkyprotocol_pb2_grpc.SimulatorStub(channel)

def registerPlayer(name):
  req = proto.RegisterPlayerReq()
  req.playerName = name

  stub.RegisterPlayer(req)

def kickPlayer(name):
  req = proto.KickPlayerReq()
  req.playerName = "egal"
  req.targeName = name
  stub.KickPlayer(req)

def getState():
  req = proto.StreamStateReq()
  resp = stub.StreamState(req)
  print (next(resp))

def registerPlayersAndStartGame():
  for pname in 'hans jurgen marie lola jana'.split():
    req = proto.RegisterPlayerReq()
    req.playerName = pname
    stub.RegisterPlayer(req)
    print("Added "+pname)

  # go into teams

  for pname in 'hans jurgen marie lola jana'.split():
    req = proto.SwitchTeamReq()
    req.playerName = pname
    req.targetName = pname
    req.targetTeam = 2
    print pname +"goes in a team"
    stub.SwitchTeam(req)

  #start game
  req = proto.ResetGameReq()
  req.playerName = "hans"
  stub.ResetGame(req)
  
def throw(name, strength = 1):
  req = proto.ThrowReq()
  req.playerName = name
  req.strength = strength
  stub.Throw(req)

def throwNext( strength = 1):
  state = next(stub.StreamState(proto.StreamStateReq()))
  name = state.state.throwingPlayer
  print( name + " is throwing")
  throw(name,strength)

def setAbgegeben(player, abgegeben = true):
  req = proto.AbgegebenReq()
  req.playerName = "jemand"
  req.targeName = player
  req.setTo = abgegeben

def playGame():
  registerPlayersAndStartGame()
  
  state = next(stub.StreamState(proto.StreamStateReq()))
  print (state)

  throw(state.state.throwingPlayer)
  
  #kick all
  players = []
  players += [p for p in state.state.spectators]
  players += [p for p in state.state.playerTeamA]
  players += [p for p in state.state.playerTeamB]

  pnames= [p.name for p in players]
  for pname in pnames:
      req = proto.KickPlayerReq()
      req.targeName = pname
      req.playerName =pname
      print "kicking "+pname
      stub.KickPlayer(req)

  #%%
  state = next(stub.StreamState(proto.StreamStateReq()))
  pnames= [p.name for p in state.state.spectators]


if (len(sys.argv) == 2 and sys.argv[1] == "listen"):
  sys.stdout.write('llisten to stream') ; sys.stdout.flush()
  req = proto.StreamStateReq()
  for x in stub.StreamState(req):
    sys.stdout.write(str(x)) ; sys.stdout.flush()
