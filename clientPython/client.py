import grpc
import flunkyprotocol_pb2 as proto
import flunkyprotocol_pb2_grpc

#channel = grpc.insecure_channel('viings.de:11049')
channel = grpc.insecure_channel('localhost:11049')
stub = flunkyprotocol_pb2_grpc.SimulatorStub(channel)

def throw():
  throwReq=proto.ThrowReq()
  response = stub.Throw(throwReq)



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

def playGame():
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

  state = next(stub.StreamState(proto.StreamStateReq()))
  print state

  #kick all
  pnames= [p.name for p in state.state.spectators]

  for pname in pnames:
      req = proto.KickPlayerReq()
      req.targeName = pname
      req.playerName =pname
      print "kicking "+pname
      stub.KickPlayer(req)

  #%%
  state = next(stub.StreamState(proto.StreamStateReq()))
  pnames= [p.name for p in state.state.spectators]
