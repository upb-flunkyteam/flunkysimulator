import grpc
import flunky_service_pb2 as flunkyService
import flunky_service_pb2_grpc
import player_service_pb2 as playerService
import player_service_pb2_grpc
import client_service_pb2 as clientService
import client_service_pb2_grpc
from google.protobuf import empty_pb2 as emptyMessage

import sys
import random

CLIENT_SECRET_KEY = "client_secret_key"

#channel = grpc.insecure_channel('flunky.viings.de:11049') # master
#channel = grpc.insecure_channel('flunky.viings.de:64809') # develop
channel = grpc.insecure_channel('localhost:11049')

flunkyStub = flunky_service_pb2_grpc.FlunkyServiceStub(channel)
playerStub = player_service_pb2_grpc.PlayerServiceStub(channel)
clientStub = client_service_pb2_grpc.ClientServiceStub(channel)

players = 'hans jurgen marie lola jana'.split()

req = clientService.ClientStreamReq()
clientStream = clientStub.ClientStream(req)
secret = clientStream.next().clientRegisterd.secret
metadataSecret = [('client_secret_key',secret)]

      
def registerPlayer(name):
  req = playerService.RegisterPlayerReq()
  req.playerName = name

  return playerStub.RegisterPlayer(req,metadata=metadataSecret)

def kickPlayer(name):
  req = flunkyService.KickPlayerReq()
  req.playerName = "egal"
  req.targeName = name
  flunkyStub.KickPlayer(req)

def getState():
  req = flunkyService.StreamStateReq()
  resp = flunkyStub.StreamState(req)
  print (next(resp))

def registerPlayersAndStartGame():
  for pname in 'hans jurgen marie lola jana'.split():
    req = flunkyService.RegisterPlayerReq()
    req.playerName = pname
    flunkyStub.RegisterPlayer(req)
    print("Added "+pname)

  #start game
  req = flunkyService.ResetGameReq()
  req.playerName = "hans"
  flunkyStub.ResetGame(req)
  
def throw(name, strength = 1):
  req = flunkyService.ThrowReq()
  req.playerName = name
  req.strength = strength
  return flunkyStub.Throw(req)

def throwNext( strength = 1):
  state = next(flunkyStub.StreamState(flunkyService.StreamStateReq()))
  name = state.state.throwingPlayer
  print( name + " is throwing")
  return throw(name,strength)

def setAbgegeben(judge, player, abgegeben = True):
  req = flunkyService.AbgegebenReq()
  req.playerName = judge
  req.targetName = player
  req.setTo = abgegeben
  return flunkyStub.Abgegeben(req)

def modifyStrafbier(increment = True,team = 2):
  req = flunkyService.ModifyStrafbierCountReq()
  req.playerName = "Strafbierboy"
  req.targetTeam = team
  req.increment = increment
  flunkyStub.ModifyStrafbierCount(req)


# debug rpcs
def setRestingPeriod(milliseconds, active):
  req = flunkyService.RestingPeriodReq()
  req.active = active
  req.milliseconds = milliseconds
  flunkyStub.SetRestingPeriod(req)

def hardReset():
  flunkyStub.HardReset(emptyMessage.Empty())

def playGame():
  registerPlayersAndStartGame()
  
  state = next(flunkyStub.StreamState(flunkyService.StreamStateReq()))
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
      req = flunkyService.KickPlayerReq()
      req.targeName = pname
      req.playerName =pname
      print "kicking "+pname
      flunkyStub.KickPlayer(req)

  #%%
  state = next(flunkyStub.StreamState(flunkyService.StreamStateReq()))
  pnames= [p.name for p in state.state.spectators]


if (len(sys.argv) == 2):
  arg = sys.argv[1]
  if(arg == "listenS"):
    sys.stdout.write('listen to state stream  ') ; sys.stdout.flush()
    req = flunkyService.StreamStateReq()
    for x in flunkyStub.StreamState(req):
      sys.stdout.write(str(x)) ; sys.stdout.flush()
      
  if(arg == "listenL"):
    sys.stdout.write('listen to log stream   ') ; sys.stdout.flush()
    req = flunkyService.LogReq()
    for x in flunkyStub.StreamLog(req):
      sys.stdout.write(str(x)) ; sys.stdout.flush()
      
  if(arg == "listenV"):
    sys.stdout.write('listen to video event stream   ') ; sys.stdout.flush()
    req = flunkyService.StreamVideoEventsReq()
    for x in flunkyStub.StreamVideoEvents(req):
      sys.stdout.write(str(x)) ; sys.stdout.flush()
    



  
