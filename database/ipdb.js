const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const IPModel = require('./ipschema.js');

const app = express();
const port = 3001;

// Connect to MongoDB
mongoose.connect('mongodb://10.102.211.11:27017/blockedIPs', { useNewUrlParser: true, useUnifiedTopology: true });

// Middleware for parsing JSON data
app.use(bodyParser.json());

// Middleware for handling CORS
const cors = require('cors');
app.use(cors());

//Add a new IP
app.post('/ip', async (req, res) => {
    try {
        console.log('Request Body:', req.body); // Log the request body
        const newIP = await IPModel.create(req.body);
        res.status(201).json(newIP);
    } catch (error) {
        console.error('Error:', error); // Log the error
        res.status(500).json({ error: 'Internal Server Error' });
    }
});


// Route to get all IPs
app.get('/ips', async (req, res) => {
    try {
        const allIPs = await IPModel.find();
        res.status(200).json(allIPs);
    } catch (error) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

// Route to delete one IP
app.delete('/ip/:id', async (req, res) => {
    try {
        const deletedIP = await IPModel.findByIdAndDelete(req.params.id);
        if (!deletedIP) {
            return res.status(404).json({ error: 'IP not found' });
        }
        res.status(200).json(deletedIP);
    } catch (error) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

// Route to delete all IPs
app.delete('/ips', async (req, res) => {
    try {
        const deletedIPs = await IPModel.deleteMany({});
        res.status(200).json(deletedIPs);
    } catch (error) {
        res.status(500).json({ error: 'Internal Server Error' });
    }
});

// Start the server
app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
