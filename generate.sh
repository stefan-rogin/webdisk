#!/bin/bash

# Function to generate a random filename
generate_filename() {
    local length=$((RANDOM % 64 + 1))
    local chars=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_
    local filename=""
    for ((i=0; i<length; i++)); do
        filename+="${chars:RANDOM%${#chars}:1}"
    done
    echo "$filename"
}

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <directory_path> <number_of_files>"
    exit 1
fi

# Assign parameters to variables
directory=$1
num_files=$2

# Create the directory if it doesn't exist
mkdir -p "$directory"

# Generate the specified number of empty files
for ((i=0; i<num_files; i++)); do
    touch "$directory/$(generate_filename)"
done

echo "Generated $num_files empty files in $directory"
