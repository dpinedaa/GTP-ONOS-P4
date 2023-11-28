import re
import time
import socket
import json

def print_flow_info(src_ip, flows):
    print(f"\n\nSource IP address: {src_ip}\n")

    for flow in flows:
        print("Flows associated with")
        print(f"Tunnel ID: {flow['gtpTunnelId']} | Dst IP Address: {flow['dstInnerIpv4']} | Protocol: {flow['innerIpv4Protocol']} | Src Port: {flow['srcPort']} | Dst Port: {flow['dstPort']} | Packet Count: {flow['packetCount']} | Bytes Count: {flow['bytesCount']} | Duration: {flow['duration']}\n")    
        
def start_server():
    server_address = ('0.0.0.0', 3000)
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    try:
        server_socket.bind(server_address)
        server_socket.listen(1)
        print(f"Server is listening on {server_address[0]}:{server_address[1]}.")

        while True:
            client_socket, client_address = server_socket.accept()
            print("\n\n")
#            print("Received connection from", client_address)

            flow_data = client_socket.recv(4096).decode('utf-8')
#            print("Received flow:", flow_data)

            # Parse the JSON data
            try:
                data = json.loads(flow_data)
                src_ip = data["sourceIp"]
                flows = data["flows"]
                print_flow_info(src_ip, flows)
            except json.JSONDecodeError as e:
                print(f"Error decoding JSON data: {str(e)}")

            # Send "1" to the client to acknowledge receipt
            client_socket.send("1".encode('utf-8'))

            client_socket.close()

    finally:
        # Ensure the server socket is closed even if an exception occurs
        server_socket.close()

if __name__ == '__main__':
    start_server()
