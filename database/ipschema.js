const mongoose = require('mongoose');

const ipSchema = new mongoose.Schema({
    _id: { type: String, alias: 'IP', required: true }, // Explicitly set _id to flowId
    srcIP: { type: String, required: true }
});

const IPModel = mongoose.model('IP', ipSchema);

module.exports = IPModel; // Export the model, not just the schema
