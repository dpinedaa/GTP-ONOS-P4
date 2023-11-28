import re
import time
import socket

def extract_flow_info(flow_data):
    # Compile the regular expression pattern outside the loop
    pattern = re.compile(r'Flow ID: (.*?), Selector: DefaultTrafficSelector{criteria=\[(.*?)\]}, Packet Count: (\d+), Byte Count: (\d+), Duration: (\d+)')

    flow_entries = pattern.findall(flow_data)
    flow_info = []

    for entry_id, selector_info, packet_count, byte_count, duration in flow_entries:
        entry_info = {
            "Flow ID": entry_id.strip(),
            "Selector": selector_info.strip(),
            "Packet Count": int(packet_count),
            "Byte Count": int(byte_count),
            "Duration": int(duration)
        }
        flow_info.append(entry_info)

    return flow_info

def convert_ipv4_address(hex_address):
    return socket.inet_ntoa(int(hex_address, 16).to_bytes(4, byteorder='big'))

def process_flows(flows):
    for entry in flows:
        tunnel_id_match = re.search(r'hdr.gtp.teid=(0x[0-9a-fA-F]+)', entry['Selector'])
        if tunnel_id_match:
            tunnel_id = tunnel_id_match.group(1)
            print(f"Tunnel ID: {tunnel_id}")

        selector_info = entry['Selector'].split(', ')
        ipv4_src = convert_ipv4_address(selector_info[1].split('=')[1].strip())
        ipv4_dst = convert_ipv4_address(selector_info[2].split('=')[1].strip())
        protocol = int(selector_info[3].split('=')[1].strip(), 16)
        src_port = int(selector_info[4].split('=')[1].strip(), 16)
        dst_port = int(selector_info[5].split('=')[1].strip(), 16)

        print(f"IPv4 Src: {ipv4_src}")
        print(f"IPv4 Dst: {ipv4_dst}")
        print(f"Protocol: {protocol}")
        print(f"Src Port: {src_port}")
        print(f"Dst Port: {dst_port}")
        print(f"Packet Count: {entry['Packet Count']}")
        print(f"Byte Count: {entry['Byte Count']}")
        print(f"Duration: {entry['Duration']}")
        print("\n")

def start_server():
    server_address = ('10.102.211.38', 7000)
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    try:
        server_socket.bind(server_address)
        server_socket.listen(1)
        print(f"Server is listening on {server_address[0]}:{server_address[1]}.")

        prev_flow_time = None  # Initialize the variable to store the time of the previous flow

        while True:
            client_socket, client_address = server_socket.accept()
            print("\n\n")
            print("Received connection from", client_address)

            start_time = time.time()  # Record the start time

            flow_data = client_socket.recv(4096).decode('utf-8')
            print("Received flow:", flow_data)

            flow_info = extract_flow_info(flow_data)
            print("Extracted Flow Information:")
            
            # Process flows in batch
            process_flows(flow_info)

            end_time = time.time()  # Record the end time
            elapsed_time = end_time - start_time
            print(f"Time to receive and process flows: {elapsed_time:.4f} seconds")

            if prev_flow_time is not None:
                gap_time = start_time - prev_flow_time
                print(f"Gap between flows: {gap_time:.4f} seconds")
                
            prev_flow_time = start_time

            # Send "1" to the client to acknowledge receipt
            client_socket.send("1".encode('utf-8'))

            client_socket.close()

    finally:
        # Ensure the server socket is closed even if an exception occurs
        server_socket.close()

if __name__ == '__main__':
    start_server()
