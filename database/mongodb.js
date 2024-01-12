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

// Endpoint to get all flows
app.get('/flows', async (req, res) => {
  try {
    const allFlows = await DataModel.find();
    res.json(allFlows);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint to add a new flow
app.post('/flows', async (req, res) => {
  try {
    const { flowId, ...rest } = req.body;

    // Ensure that flowId is set as the _id
    const newData = new DataModel({
      ...rest,
      _id: new mongoose.Types.ObjectId(flowId),
      flowId: flowId,
    });

    const savedData = await newData.save();
    res.status(201).json(savedData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint to update a flow by ID
app.put('/flows/update/:id', async (req, res) => {
  try {
    const updatedData = await DataModel.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true }
    );

    if (!updatedData) {
      return res.status(404).json({ error: 'Flow not found' });
    }

    res.json(updatedData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Endpoint to delete a flow by ID
app.delete('/flows/:id', async (req, res) => {
  try {
    const deletedData = await DataModel.findByIdAndDelete(req.params.id);
    res.json(deletedData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
