import subprocess

def run_ping(interface, count, target):
    try:
        # Construct the command
        command = ["ping", "-I", interface, "-c", str(count), target, "&"]
        
        # Run the command
        subprocess.run(command, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Error: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

if __name__ == "__main__":
    count = 5
    target = "8.8.4.4"

    # Iterate over the range of interfaces uesimtun0 to uesimtun99
    for i in range(100):
        interface = f"uesimtun{i}"
        print(f"Running ping for {interface}")
        run_ping(interface, count, target)
