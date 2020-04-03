import grpc
import flunkyprotocol_pb2
import flunkyprotocol_pb2_grpc

def run():
  channel = grpc.insecure_channel('localhost:11049')
  stub = flunkyprotocol_pb2_grpc.SimulatorStub(channel)
  throwReq=flunkyprotocol_pb2.ThrowReq()
  response = stub.Throw(throwReq)
  print("Greeter client received: " + response.message)


run()
