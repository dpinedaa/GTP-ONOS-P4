import sys

# Check if the correct number of arguments is provided
if len(sys.argv) != 3:
    print("Usage: python3 generatepipe.py <input_json_file> <input_p4info_file>")
    sys.exit(1)

# Get the input file paths from command-line arguments
json_file_path = sys.argv[1]
p4info_file_path = sys.argv[2]

# Open the JSON file
with open(json_file_path, 'r') as f:
    data = f.read()

# Replace newline characters with "\n" and escape double quotes
data_str = data.replace('\n', '\\n').replace('"', r'\"')

# Add double quotes to the beginning and end of the string
#data_str = '"' + data_str + '"'

# Write the modified string to a new text file
with open('output_file.txt', 'w') as f:
    f.write(data_str)

# Read the content from the p4info file and preserve indentation
with open(p4info_file_path, "r") as example_file:
    example_content = example_file.read()

# Indent the example content
indented_example_content = "\n".join([" " * 5 + line for line in example_content.splitlines()])

# Read the content from output_file.json
with open("output_file.txt", "r") as output_file:
    output_content = output_file.read()

# Indent the output content
indented_output_content = "\n".join([" " * 0 + line for line in output_content.splitlines()])

# Define the base template with proper indentation
base_content = f"""
node_id_to_config {{
  key: 1
  value {{
    p4info {{
{indented_example_content}
    }}
    p4_device_config: "{indented_output_content}"
    cookie {{
      cookie: 3710752237043695765
    }}
  }}
}}
"""

# Write the modified content to a text file
with open("pipe.txt", "w") as file:
    file.write(base_content)

print("File 'pipe.txt' has been created with the specified content.")
