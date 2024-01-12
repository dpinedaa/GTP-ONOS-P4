const mongoose = require('mongoose');

const flowSchema = new mongoose.Schema({
  _id: { type: String, alias: 'flowId', required: true }, // Explicitly set _id to flowId
  srcIP: { type: String, required: true },
  dstIP: { type: String, required: true },
  protocol: { type: Number, required: true },
  forwardDuration: { type: Number, required: true },
  forwardPacketCount: { type: Number, required: true },
  forwardByteCount: { type: Number, required: true },
  backwardDuration: { type: Number, required: true },
  backwardPacketCount: { type: Number, required: true },
  backwardByteCount: { type: Number, required: true },
  flowBytesPerSecond: { type: Number, required: true },
  backwardPacketsPerSecond: { type: Number, required: true },
  lrPrediction: { type: Number, required: true },
  rfPrediction: { type: Number, required: true },
  nbPrediction: { type: Number, required: true },
  knnPrediction: { type: Number, required: true },
});

const Flow = mongoose.model('Flow', flowSchema);

module.exports = Flow;
