import socket
import json
import pandas as pd
import numpy as np
import joblib
import random
import subprocess
import warnings

# Load the pre-trained models and other necessary components
# (Assume that the necessary models and components are already loaded)
# Suppress warnings
warnings.filterwarnings('ignore')

def print_model_prediction(logistic_regression_prediction, knearest_neighbours_prediction, naive_bayes_prediction, random_forest_prediction):
    print("\nLogistic Regression Prediction:", logistic_regression_prediction)
    print("K-Nearest Neighbours Prediction:", knearest_neighbours_prediction)
    print("Naive Bayes Prediction:", naive_bayes_prediction)
    print("Random Forest Prediction:", random_forest_prediction)

# Set up the server socket
server_ip = "0.0.0.0"
server_port = 4500

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((server_ip, server_port))
server_socket.listen(1)

print(f"Server listening on {server_ip}:{server_port}")


# Load the pre-trained models
logistic_regression_model = joblib.load('logistic_regression_model.joblib')
knearest_neighbours_model = joblib.load('knearest_neighbours_model.joblib')
naive_bayes_model = joblib.load('naive_bayes_model.joblib')
random_forest_model = joblib.load('random_forest_model.joblib')
scaler_train = joblib.load('scaler.joblib')


while True:
    # Wait for a connection from the client
    client_socket, client_address = server_socket.accept()
    print(f"Accepted connection from {client_address}")

    try:
        # Receive data from the client
        data = client_socket.recv(1024)
        if not data:
            break

        # Decode the received data (assuming it's JSON)
        json_data = data.decode('utf-8')
        payload = json.loads(json_data)

        # Extract the flowString from the payload
        flowString = payload.get('flowString')

        # Split the flowString into individual values
        flow_values = flowString.split()

        # Convert the flow values to NumPy array
        input_values = np.array([float(value) for value in flow_values])

        # Use the loaded scaler to transform the input data
        scaled_input_data = scaler_train.transform(input_values.reshape(1, -1))

        # Make predictions using each model
        logistic_regression_prediction = logistic_regression_model.predict(scaled_input_data)
        knearest_neighbours_prediction = knearest_neighbours_model.predict(scaled_input_data)
        naive_bayes_prediction = naive_bayes_model.predict(scaled_input_data)
        random_forest_prediction = random_forest_model.predict(scaled_input_data)

        # Print predictions for each model
        print_model_prediction(logistic_regression_prediction, knearest_neighbours_prediction, naive_bayes_prediction, random_forest_prediction)

        # Send a response back to the client
        response = {"status": "success", "message": "Prediction printed successfully"}
        client_socket.send(json.dumps(response).encode('utf-8'))

    except Exception as e:
        print(f"Error processing data: {e}")
        response = {"status": "error", "message": str(e)}
        client_socket.send(json.dumps(response).encode('utf-8'))

    finally:
        # Close the client socket
        client_socket.close()

# Close the server socket
server_socket.close()
