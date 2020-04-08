import grpc
import flunkyprotocol_pb2
import flunkyprotocol_pb2_grpc

#channel = grpc.insecure_channel('viings.de:11049')
channel = grpc.insecure_channel('localhost:11049')
stub = flunkyprotocol_pb2_grpc.SimulatorStub(channel)

def throw():
  throwReq=flunkyprotocol_pb2.ThrowReq()
  response = stub.Throw(throwReq)
  print("Greeter client received: " + response.message)



def registerPlayer(name):
  req = flunkyprotocol_pb2.RegisterPlayerReq()
  req.playerName = name

  stub.RegisterPlayer(req)

def blockingGetStates():
  req = flunkyprotocol_pb2.StreamStateReq()
  resp = stub.StreamState(req)
  for x in resp:
    print (x)
