import csv
import socket
import json
import random
import time

# Set up the server details
server_ip = "127.0.0.1"  # Replace with the actual server IP
server_port = 4500

# Use the absolute path to the CSV file
file_path = 'test_data.csv'  # Replace with the actual path
column_index = 2  # Replace with the index of the column you want to check (0-based index)

try:
    with open(file_path, 'r', newline='') as csvfile:
        csv_reader = csv.reader(csvfile)
        header = next(csv_reader)  # Read the header

        benig_rows = []
        attack_rows = []
        for row in csv_reader:
            last_element = row[-1].strip()  # Get the last element of the row
            if last_element.endswith('0'):
                benig_rows.append(row)
            elif last_element.endswith('1'):
                attack_rows.append(row)


        # Randomly select 30 rows
        benign_random_rows = random.sample(benig_rows, k=30)
        attack_random_rows = random.sample(attack_rows, k=30)

        combined_random_rows = benign_random_rows + attack_random_rows

        total_correct_logistic_regression = 0
        total_correct_knearest_neighbours = 0
        total_correct_naive_bayes = 0
        total_correct_random_forest = 0

        #for row in benign_random_rows:

        for row in combined_random_rows:
            # Remove the last element from the row
            row_without_last_element = row[:-1]

            formatted_row = ' '.join(map(str, row_without_last_element))  # Join elements with space
            print(formatted_row)

            # Set up a new client socket for each sample
            client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            client_socket.connect((server_ip, server_port))

            try:
                # Create a dictionary payload
                payload = {"flowString": formatted_row}

                # Convert the payload to JSON
                json_payload = json.dumps(payload)

                # Send the JSON data to the server
                client_socket.send(json_payload.encode('utf-8'))

                # Receive the response from the server
                response_data = client_socket.recv(1024)
                response = json.loads(response_data.decode('utf-8'))

                # Extract predictions and probabilities for each model
                logistic_regression_prediction = response['logistic_regression']['prediction']
                knearest_neighbours_prediction = response['knearest_neighbours']['prediction']
                naive_bayes_prediction = response['naive_bayes']['prediction']
                random_forest_prediction = response['random_forest']['prediction']

                # Assuming the ground truth label is stored in the last column of the row
                ground_truth = int(row[-1].strip())

                # Check accuracy for each model
                if logistic_regression_prediction == ground_truth:
                    total_correct_logistic_regression += 1

                if knearest_neighbours_prediction == ground_truth:
                    total_correct_knearest_neighbours += 1

                if naive_bayes_prediction == ground_truth:
                    total_correct_naive_bayes += 1

                if random_forest_prediction == ground_truth:
                    total_correct_random_forest += 1

                # Print the server's response
                print(f"Server response: {response}")

            except Exception as e:
                print(f"Error: {e}")

            finally:
                # Close the client socket
                client_socket.close()

            # Introduce a delay of 2 seconds before sending the next flow
            time.sleep(1)

        # Calculate accuracy for each model
        accuracy_logistic_regression = total_correct_logistic_regression / len(combined_random_rows)
        accuracy_knearest_neighbours = total_correct_knearest_neighbours / len(combined_random_rows)
        accuracy_naive_bayes = total_correct_naive_bayes / len(combined_random_rows)
        accuracy_random_forest = total_correct_random_forest / len(combined_random_rows)

        print(f"\nAccuracy - Logistic Regression: {accuracy_logistic_regression}")
        print(f"Accuracy - K-Nearest Neighbours: {accuracy_knearest_neighbours}")
        print(f"Accuracy - Naive Bayes: {accuracy_naive_bayes}")
        print(f"Accuracy - Random Forest: {accuracy_random_forest}")

except Exception as e:
    print(f"Error: {e}")
