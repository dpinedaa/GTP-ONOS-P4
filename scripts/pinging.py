import subprocess
import sys

# Get the total number of uesimtun interfaces and the destination IP address from the command-line arguments
total_uesimtun = int(sys.argv[1])
dst_ip = sys.argv[2]

# List to store subprocesses
processes = []

for i in range(total_uesimtun):
    # Create the ping command for each uesimtun interface
    command = f"ping -I uesimtun{i} {dst_ip}"

    # Start each 'ping' command as a separate subprocess
    process = subprocess.Popen(command, shell=True)
    processes.append(process)

# Wait for all subprocesses to finish
for process in processes:
    process.wait()
