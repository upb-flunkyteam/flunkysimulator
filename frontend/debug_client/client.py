import grpc
import flunkyprotocol_pb2 as proto
import flunkyprotocol_pb2_grpc
import sys
import random
from google.protobuf import empty_pb2 as emptyMessage

#channel = grpc.insecure_channel('flunky.viings.de:11049') # master
channel = grpc.insecure_channel('flunky.viings.de:64809') # develop
#channel = grpc.insecure_channel('localhost:11049')

stub = flunkyprotocol_pb2_grpc.SimulatorStub(channel)

players = 'hans jurgen marie lola jana'.split()

def registerPlayer(name):
  req = proto.RegisterPlayerReq()
  req.playerName = name

  return stub.RegisterPlayer(req)

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

  #start game
  req = proto.ResetGameReq()
  req.playerName = "hans"
  stub.ResetGame(req)
  
def throw(name, strength = 1):
  req = proto.ThrowReq()
  req.playerName = name
  req.strength = strength
  return stub.Throw(req)

def throwNext( strength = 1):
  state = next(stub.StreamState(proto.StreamStateReq()))
  name = state.state.throwingPlayer
  print( name + " is throwing")
  return throw(name,strength)

def setAbgegeben(judge, player, abgegeben = True):
  req = proto.AbgegebenReq()
  req.playerName = judge
  req.targetName = player
  req.setTo = abgegeben
  return stub.Abgegeben(req)

def modifyStrafbier(increment = True,team = 2):
  req = proto.ModifyStrafbierCountReq()
  req.playerName = "Strafbierboy"
  req.targetTeam = team
  req.increment = increment
  stub.ModifyStrafbierCount(req)


# debug rpcs
def setRestingPeriod(milliseconds, active):
  req = proto.RestingPeriodReq()
  req.active = active
  req.milliseconds = milliseconds
  stub.SetRestingPeriod(req)

def hardReset():
  stub.HardReset(emptyMessage.Empty())

def playGame():
  registerPlayersAndStartGame()
  
  state = next(stub.StreamState(proto.StreamStateReq()))
  print (state)

  throw(state.state.throwingPlayer)

  rnd = random.Random()
  for x in range (2):
    throwNext(rnd.randint(1,3))

  p1 = players[rnd.randint(0,len(players)-1)]
  setAbgegeben(p1)
  setAbgegeben(players[rnd.randint(0,len(players)-1)])

  throwNext(rnd.randint(1,3))
  modifyStrafbier()

  modifyStrafbier(False)
  setAbgegeben(p1,False)
  
  throwNext(rnd.randint(1,3))
  
  #kick all
  playersA = []
  playersA += [p for p in state.state.spectators]
  playersA += [p for p in state.state.playerTeamA]
  playersA += [p for p in state.state.playerTeamB]

  pnames= [p.name for p in playersA]
  for pname in pnames:
      req = proto.KickPlayerReq()
      req.targeName = pname
      req.playerName =pname
      print "kicking "+pname
      stub.KickPlayer(req)

  #%%
  state = next(stub.StreamState(proto.StreamStateReq()))
  pnames= [p.name for p in state.state.spectators]


if (len(sys.argv) == 2):
  arg = sys.argv[1]
  if(arg == "listenS"):
    sys.stdout.write('listen to state stream  ') ; sys.stdout.flush()
    req = proto.StreamStateReq()
    for x in stub.StreamState(req):
      sys.stdout.write(str(x)) ; sys.stdout.flush()
      
  if(arg == "listenL"):
    sys.stdout.write('listen to log stream   ') ; sys.stdout.flush()
    req = proto.LogReq()
    for x in stub.StreamLog(req):
      sys.stdout.write(str(x)) ; sys.stdout.flush()
      
  if(arg == "listenV"):
    sys.stdout.write('listen to video event stream   ') ; sys.stdout.flush()
    req = proto.StreamVideoEventsReq()
    for x in stub.StreamVideoEvents(req):
      sys.stdout.write(str(x)) ; sys.stdout.flush()
    



  
