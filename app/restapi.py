import requests
import json

# Replace these with your ONOS credentials and the actual ONOS REST API URL
username = "onos"
password = "rocks"
api_url = "http://10.102.211.38:8181/onos/v1/flows/device:s1"

# Create a session with the credentials
session = requests.Session()
session.auth = (username, password)

# Send a GET request to the ONOS REST API
response = session.get(api_url)

# Check if the request was successful
if response.status_code == 200:
    # Parse the JSON response
    data = response.json()

    # Extract the flows from the response
    flows = data.get("flows", [])

    # Print the extracted flows
    for flow in flows:
        if flow["tableName"] == "IngressPipeImpl.gtp_tunnel":
            print("Flow ID:", flow["id"])
            print("App ID:", flow["appId"])
            print("Device ID:", flow["deviceId"])
            print("Packet count:", flow["packets"])
            print("Table Name:", flow["tableName"])
            print("Priority:", flow["priority"])
            print("Selector Criteria:", flow["selector"]["criteria"])
            print("Treatment Instructions:", flow["treatment"]["instructions"])
            print("\n")
        
else:
    print("Failed to fetch data from the ONOS REST API. Status Code:", response.status_code)






