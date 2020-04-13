/**
 * @fileoverview gRPC-Web generated client stub for endpoints.flunky.simulator
 * @enhanceable
 * @public
 */

// GENERATED CODE -- DO NOT EDIT!


goog.provide('proto.endpoints.flunky.simulator.SimulatorClient');
goog.provide('proto.endpoints.flunky.simulator.SimulatorPromiseClient');

goog.require('grpc.web.GrpcWebClientBase');
goog.require('grpc.web.AbstractClientBase');
goog.require('grpc.web.ClientReadableStream');
goog.require('grpc.web.Error');
goog.require('grpc.web.MethodDescriptor');
goog.require('grpc.web.MethodType');
goog.require('proto.endpoints.flunky.simulator.AbgegebenReq');
goog.require('proto.endpoints.flunky.simulator.AbgegebenResp');
goog.require('proto.endpoints.flunky.simulator.KickPlayerReq');
goog.require('proto.endpoints.flunky.simulator.KickPlayerResp');
goog.require('proto.endpoints.flunky.simulator.LogReq');
goog.require('proto.endpoints.flunky.simulator.LogResp');
goog.require('proto.endpoints.flunky.simulator.ModifyStrafbierCountReq');
goog.require('proto.endpoints.flunky.simulator.ModifyStrafbierCountResp');
goog.require('proto.endpoints.flunky.simulator.RegisterPlayerReq');
goog.require('proto.endpoints.flunky.simulator.RegisterPlayerResp');
goog.require('proto.endpoints.flunky.simulator.ResetGameReq');
goog.require('proto.endpoints.flunky.simulator.ResetGameResp');
goog.require('proto.endpoints.flunky.simulator.SelectThrowingPlayerReq');
goog.require('proto.endpoints.flunky.simulator.SelectThrowingPlayerResp');
goog.require('proto.endpoints.flunky.simulator.SendMessageReq');
goog.require('proto.endpoints.flunky.simulator.SendMessageResp');
goog.require('proto.endpoints.flunky.simulator.StreamEventsReq');
goog.require('proto.endpoints.flunky.simulator.StreamEventsResp');
goog.require('proto.endpoints.flunky.simulator.StreamStateReq');
goog.require('proto.endpoints.flunky.simulator.StreamStateResp');
goog.require('proto.endpoints.flunky.simulator.SwitchTeamReq');
goog.require('proto.endpoints.flunky.simulator.SwitchTeamResp');
goog.require('proto.endpoints.flunky.simulator.ThrowReq');
goog.require('proto.endpoints.flunky.simulator.ThrowResp');



goog.scope(function() {

/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.endpoints.flunky.simulator.SimulatorClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

};


/**
 * @param {string} hostname
 * @param {?Object} credentials
 * @param {?Object} options
 * @constructor
 * @struct
 * @final
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient =
    function(hostname, credentials, options) {
  if (!options) options = {};
  options['format'] = 'text';

  /**
   * @private @const {!grpc.web.GrpcWebClientBase} The client
   */
  this.client_ = new grpc.web.GrpcWebClientBase(options);

  /**
   * @private @const {string} The hostname
   */
  this.hostname_ = hostname;

};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.ThrowReq,
 *   !proto.endpoints.flunky.simulator.ThrowResp>}
 */
const methodDescriptor_Simulator_Throw = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/Throw',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.ThrowReq,
  proto.endpoints.flunky.simulator.ThrowResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.ThrowReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.ThrowResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.ThrowReq,
 *   !proto.endpoints.flunky.simulator.ThrowResp>}
 */
const methodInfo_Simulator_Throw = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.ThrowResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.ThrowReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.ThrowResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.ThrowReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.ThrowResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.ThrowResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.throw =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/Throw',
      request,
      metadata || {},
      methodDescriptor_Simulator_Throw,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.ThrowReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.ThrowResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.throw =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/Throw',
      request,
      metadata || {},
      methodDescriptor_Simulator_Throw);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.AbgegebenReq,
 *   !proto.endpoints.flunky.simulator.AbgegebenResp>}
 */
const methodDescriptor_Simulator_Abgegeben = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/Abgegeben',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.AbgegebenReq,
  proto.endpoints.flunky.simulator.AbgegebenResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.AbgegebenReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.AbgegebenResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.AbgegebenReq,
 *   !proto.endpoints.flunky.simulator.AbgegebenResp>}
 */
const methodInfo_Simulator_Abgegeben = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.AbgegebenResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.AbgegebenReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.AbgegebenResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.AbgegebenReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.AbgegebenResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.AbgegebenResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.abgegeben =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/Abgegeben',
      request,
      metadata || {},
      methodDescriptor_Simulator_Abgegeben,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.AbgegebenReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.AbgegebenResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.abgegeben =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/Abgegeben',
      request,
      metadata || {},
      methodDescriptor_Simulator_Abgegeben);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.RegisterPlayerReq,
 *   !proto.endpoints.flunky.simulator.RegisterPlayerResp>}
 */
const methodDescriptor_Simulator_RegisterPlayer = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/RegisterPlayer',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.RegisterPlayerReq,
  proto.endpoints.flunky.simulator.RegisterPlayerResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.RegisterPlayerReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.RegisterPlayerResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.RegisterPlayerReq,
 *   !proto.endpoints.flunky.simulator.RegisterPlayerResp>}
 */
const methodInfo_Simulator_RegisterPlayer = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.RegisterPlayerResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.RegisterPlayerReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.RegisterPlayerResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.RegisterPlayerReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.RegisterPlayerResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.RegisterPlayerResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.registerPlayer =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/RegisterPlayer',
      request,
      metadata || {},
      methodDescriptor_Simulator_RegisterPlayer,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.RegisterPlayerReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.RegisterPlayerResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.registerPlayer =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/RegisterPlayer',
      request,
      metadata || {},
      methodDescriptor_Simulator_RegisterPlayer);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.KickPlayerReq,
 *   !proto.endpoints.flunky.simulator.KickPlayerResp>}
 */
const methodDescriptor_Simulator_KickPlayer = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/KickPlayer',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.KickPlayerReq,
  proto.endpoints.flunky.simulator.KickPlayerResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.KickPlayerReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.KickPlayerResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.KickPlayerReq,
 *   !proto.endpoints.flunky.simulator.KickPlayerResp>}
 */
const methodInfo_Simulator_KickPlayer = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.KickPlayerResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.KickPlayerReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.KickPlayerResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.KickPlayerReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.KickPlayerResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.KickPlayerResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.kickPlayer =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/KickPlayer',
      request,
      metadata || {},
      methodDescriptor_Simulator_KickPlayer,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.KickPlayerReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.KickPlayerResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.kickPlayer =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/KickPlayer',
      request,
      metadata || {},
      methodDescriptor_Simulator_KickPlayer);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.SwitchTeamReq,
 *   !proto.endpoints.flunky.simulator.SwitchTeamResp>}
 */
const methodDescriptor_Simulator_SwitchTeam = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/SwitchTeam',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.SwitchTeamReq,
  proto.endpoints.flunky.simulator.SwitchTeamResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.SwitchTeamReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.SwitchTeamResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.SwitchTeamReq,
 *   !proto.endpoints.flunky.simulator.SwitchTeamResp>}
 */
const methodInfo_Simulator_SwitchTeam = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.SwitchTeamResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.SwitchTeamReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.SwitchTeamResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.SwitchTeamReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.SwitchTeamResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.SwitchTeamResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.switchTeam =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/SwitchTeam',
      request,
      metadata || {},
      methodDescriptor_Simulator_SwitchTeam,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.SwitchTeamReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.SwitchTeamResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.switchTeam =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/SwitchTeam',
      request,
      metadata || {},
      methodDescriptor_Simulator_SwitchTeam);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.ResetGameReq,
 *   !proto.endpoints.flunky.simulator.ResetGameResp>}
 */
const methodDescriptor_Simulator_ResetGame = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/ResetGame',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.ResetGameReq,
  proto.endpoints.flunky.simulator.ResetGameResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.ResetGameReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.ResetGameResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.ResetGameReq,
 *   !proto.endpoints.flunky.simulator.ResetGameResp>}
 */
const methodInfo_Simulator_ResetGame = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.ResetGameResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.ResetGameReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.ResetGameResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.ResetGameReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.ResetGameResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.ResetGameResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.resetGame =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/ResetGame',
      request,
      metadata || {},
      methodDescriptor_Simulator_ResetGame,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.ResetGameReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.ResetGameResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.resetGame =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/ResetGame',
      request,
      metadata || {},
      methodDescriptor_Simulator_ResetGame);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.SelectThrowingPlayerReq,
 *   !proto.endpoints.flunky.simulator.SelectThrowingPlayerResp>}
 */
const methodDescriptor_Simulator_SelectThrowingPlayer = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/SelectThrowingPlayer',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.SelectThrowingPlayerReq,
  proto.endpoints.flunky.simulator.SelectThrowingPlayerResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.SelectThrowingPlayerReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.SelectThrowingPlayerResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.SelectThrowingPlayerReq,
 *   !proto.endpoints.flunky.simulator.SelectThrowingPlayerResp>}
 */
const methodInfo_Simulator_SelectThrowingPlayer = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.SelectThrowingPlayerResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.SelectThrowingPlayerReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.SelectThrowingPlayerResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.SelectThrowingPlayerReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.SelectThrowingPlayerResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.SelectThrowingPlayerResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.selectThrowingPlayer =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/SelectThrowingPlayer',
      request,
      metadata || {},
      methodDescriptor_Simulator_SelectThrowingPlayer,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.SelectThrowingPlayerReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.SelectThrowingPlayerResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.selectThrowingPlayer =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/SelectThrowingPlayer',
      request,
      metadata || {},
      methodDescriptor_Simulator_SelectThrowingPlayer);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.ModifyStrafbierCountReq,
 *   !proto.endpoints.flunky.simulator.ModifyStrafbierCountResp>}
 */
const methodDescriptor_Simulator_ModifyStrafbierCount = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/ModifyStrafbierCount',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.ModifyStrafbierCountReq,
  proto.endpoints.flunky.simulator.ModifyStrafbierCountResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.ModifyStrafbierCountReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.ModifyStrafbierCountResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.ModifyStrafbierCountReq,
 *   !proto.endpoints.flunky.simulator.ModifyStrafbierCountResp>}
 */
const methodInfo_Simulator_ModifyStrafbierCount = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.ModifyStrafbierCountResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.ModifyStrafbierCountReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.ModifyStrafbierCountResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.ModifyStrafbierCountReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.ModifyStrafbierCountResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.ModifyStrafbierCountResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.modifyStrafbierCount =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/ModifyStrafbierCount',
      request,
      metadata || {},
      methodDescriptor_Simulator_ModifyStrafbierCount,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.ModifyStrafbierCountReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.ModifyStrafbierCountResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.modifyStrafbierCount =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/ModifyStrafbierCount',
      request,
      metadata || {},
      methodDescriptor_Simulator_ModifyStrafbierCount);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.SendMessageReq,
 *   !proto.endpoints.flunky.simulator.SendMessageResp>}
 */
const methodDescriptor_Simulator_SendMessage = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/SendMessage',
  grpc.web.MethodType.UNARY,
  proto.endpoints.flunky.simulator.SendMessageReq,
  proto.endpoints.flunky.simulator.SendMessageResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.SendMessageReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.SendMessageResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.SendMessageReq,
 *   !proto.endpoints.flunky.simulator.SendMessageResp>}
 */
const methodInfo_Simulator_SendMessage = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.SendMessageResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.SendMessageReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.SendMessageResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.SendMessageReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @param {function(?grpc.web.Error, ?proto.endpoints.flunky.simulator.SendMessageResp)}
 *     callback The callback function(error, response)
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.SendMessageResp>|undefined}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.sendMessage =
    function(request, metadata, callback) {
  return this.client_.rpcCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/SendMessage',
      request,
      metadata || {},
      methodDescriptor_Simulator_SendMessage,
      callback);
};


/**
 * @param {!proto.endpoints.flunky.simulator.SendMessageReq} request The
 *     request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!Promise<!proto.endpoints.flunky.simulator.SendMessageResp>}
 *     A native promise that resolves to the response
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.sendMessage =
    function(request, metadata) {
  return this.client_.unaryCall(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/SendMessage',
      request,
      metadata || {},
      methodDescriptor_Simulator_SendMessage);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.StreamStateReq,
 *   !proto.endpoints.flunky.simulator.StreamStateResp>}
 */
const methodDescriptor_Simulator_StreamState = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/StreamState',
  grpc.web.MethodType.SERVER_STREAMING,
  proto.endpoints.flunky.simulator.StreamStateReq,
  proto.endpoints.flunky.simulator.StreamStateResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.StreamStateReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.StreamStateResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.StreamStateReq,
 *   !proto.endpoints.flunky.simulator.StreamStateResp>}
 */
const methodInfo_Simulator_StreamState = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.StreamStateResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.StreamStateReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.StreamStateResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.StreamStateReq} request The request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.StreamStateResp>}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.streamState =
    function(request, metadata) {
  return this.client_.serverStreaming(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/StreamState',
      request,
      metadata || {},
      methodDescriptor_Simulator_StreamState);
};


/**
 * @param {!proto.endpoints.flunky.simulator.StreamStateReq} request The request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.StreamStateResp>}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.streamState =
    function(request, metadata) {
  return this.client_.serverStreaming(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/StreamState',
      request,
      metadata || {},
      methodDescriptor_Simulator_StreamState);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.StreamEventsReq,
 *   !proto.endpoints.flunky.simulator.StreamEventsResp>}
 */
const methodDescriptor_Simulator_StreamEvents = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/StreamEvents',
  grpc.web.MethodType.SERVER_STREAMING,
  proto.endpoints.flunky.simulator.StreamEventsReq,
  proto.endpoints.flunky.simulator.StreamEventsResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.StreamEventsReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.StreamEventsResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.StreamEventsReq,
 *   !proto.endpoints.flunky.simulator.StreamEventsResp>}
 */
const methodInfo_Simulator_StreamEvents = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.StreamEventsResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.StreamEventsReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.StreamEventsResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.StreamEventsReq} request The request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.StreamEventsResp>}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.streamEvents =
    function(request, metadata) {
  return this.client_.serverStreaming(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/StreamEvents',
      request,
      metadata || {},
      methodDescriptor_Simulator_StreamEvents);
};


/**
 * @param {!proto.endpoints.flunky.simulator.StreamEventsReq} request The request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.StreamEventsResp>}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.streamEvents =
    function(request, metadata) {
  return this.client_.serverStreaming(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/StreamEvents',
      request,
      metadata || {},
      methodDescriptor_Simulator_StreamEvents);
};


/**
 * @const
 * @type {!grpc.web.MethodDescriptor<
 *   !proto.endpoints.flunky.simulator.LogReq,
 *   !proto.endpoints.flunky.simulator.LogResp>}
 */
const methodDescriptor_Simulator_StreamLog = new grpc.web.MethodDescriptor(
  '/endpoints.flunky.simulator.Simulator/StreamLog',
  grpc.web.MethodType.SERVER_STREAMING,
  proto.endpoints.flunky.simulator.LogReq,
  proto.endpoints.flunky.simulator.LogResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.LogReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.LogResp.deserializeBinary
);


/**
 * @const
 * @type {!grpc.web.AbstractClientBase.MethodInfo<
 *   !proto.endpoints.flunky.simulator.LogReq,
 *   !proto.endpoints.flunky.simulator.LogResp>}
 */
const methodInfo_Simulator_StreamLog = new grpc.web.AbstractClientBase.MethodInfo(
  proto.endpoints.flunky.simulator.LogResp,
  /**
   * @param {!proto.endpoints.flunky.simulator.LogReq} request
   * @return {!Uint8Array}
   */
  function(request) {
    return request.serializeBinary();
  },
  proto.endpoints.flunky.simulator.LogResp.deserializeBinary
);


/**
 * @param {!proto.endpoints.flunky.simulator.LogReq} request The request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.LogResp>}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorClient.prototype.streamLog =
    function(request, metadata) {
  return this.client_.serverStreaming(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/StreamLog',
      request,
      metadata || {},
      methodDescriptor_Simulator_StreamLog);
};


/**
 * @param {!proto.endpoints.flunky.simulator.LogReq} request The request proto
 * @param {?Object<string, string>} metadata User defined
 *     call metadata
 * @return {!grpc.web.ClientReadableStream<!proto.endpoints.flunky.simulator.LogResp>}
 *     The XHR Node Readable Stream
 */
proto.endpoints.flunky.simulator.SimulatorPromiseClient.prototype.streamLog =
    function(request, metadata) {
  return this.client_.serverStreaming(this.hostname_ +
      '/endpoints.flunky.simulator.Simulator/StreamLog',
      request,
      metadata || {},
      methodDescriptor_Simulator_StreamLog);
};


}); // goog.scope

