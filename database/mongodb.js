const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const DataModel = require('./flowdb.js');


const app = express();
const port = 3000;

// Connect to MongoDB
mongoose.connect('mongodb://10.102.211.11:27017/flows', { useNewUrlParser: true, useUnifiedTopology: true });

// Middleware for parsing JSON data
app.use(bodyParser.json());


const cors = require('cors');
app.use(cors());


// Endpoint to get all flows
app.get('/flows', async (req, res) => {
  try {
    const allFlows = await DataModel.find();
    res.json(allFlows);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint to get a specific flow by ID
app.get('/flows/:id', async (req, res) => {
  try {
    const flow = await DataModel.findById(req.params.id);
    if (!flow) {
      return res.status(404).json({ message: 'Flow not found' });
    }
    res.json(flow);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint to add a new flow
app.post('/flows', async (req, res) => {
  try {
    const flowData = req.body;

    // Check for duplicates before adding
    const duplicateCheck = await DataModel.findOne({
      srcIP: flowData.srcIP,
      dstIP: flowData.dstIP,
      protocol: flowData.protocol,
    });

    if (duplicateCheck) {
      return res.status(400).json({ error: 'Duplicate flow detected' });
    }

    const newFlow = new DataModel(flowData);
    await newFlow.save();

    res.status(201).json({ message: 'Flow added successfully', flow: newFlow });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Endpoint to update a flow by ID
app.put('/flows/:id', async (req, res) => {
  try {
    const flowId = req.params.id;
    const updateData = req.body;

    // Restricting updates to specific fields
    const allowedFields = [
      'forwardDuration', 'forwardPacketCount', 'forwardByteCount',
      'backwardDuration', 'backwardPacketCount', 'backwardByteCount',
      'flowBytesPerSecond', 'backwardPacketsPerSecond',
      'lrPrediction', 'rfPrediction', 'nbPrediction', 'knnPrediction'
    ];

    // Filtering the fields that can be modified
    const filteredUpdateData = Object.keys(updateData)
      .filter(key => allowedFields.includes(key))
      .reduce((obj, key) => {
        obj[key] = updateData[key];
        return obj;
      }, {});

    const updatedFlow = await DataModel.findByIdAndUpdate(flowId, filteredUpdateData, { new: true });

    if (!updatedFlow) {
      return res.status(404).json({ message: 'Flow not found' });
    }

    res.json({ message: 'Flow updated successfully', flow: updatedFlow });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Endpoint to delete a flow by ID
app.delete('/flows/:id', async (req, res) => {
  try {
    const flowId = req.params.id;
    const deletedFlow = await DataModel.findByIdAndDelete(flowId);

    if (!deletedFlow) {
      return res.status(404).json({ message: 'Flow not found' });
    }

    res.json({ message: 'Flow deleted successfully', flow: deletedFlow });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});


// Endpoint to delete all flows
app.delete('/flows', async (req, res) => {
  try {
    const deletedFlows = await DataModel.deleteMany({});
    
    if (deletedFlows.deletedCount === 0) {
      return res.status(404).json({ message: 'No flows found' });
    }

    res.json({ message: 'All flows deleted successfully', deletedFlows });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});



app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
