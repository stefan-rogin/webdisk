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

# Function to format the elapsed time
format_time() {
    local seconds=$1
    printf "%02d:%02d:%02d\n" $((seconds/3600)) $((seconds%3600/60)) $((seconds%60))
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

# Generate test files not randomized
echo "one" > "$directory/one"
echo "andone" > "$directory/andone"
touch "$directory/two"

# Start time
start_time=$(date +%s)
last_report_time=$start_time

# Generate the specified number of empty files
for ((i=0; i<num_files; i++)); do
    touch "$directory/$(generate_filename)"
    
    # Report progress every 10% if total number of files > 10000
    if [ "$num_files" -gt 10000 ] && [ $((i % (num_files / 10))) -eq 0 ] && [ "$i" -ne 0 ]; then
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        time_since_last_report=$((current_time - last_report_time))
        echo "Progress: $((i * 100 / num_files))% completed. Total elapsed time: $(format_time $elapsed_time). Time since last report: $(format_time $time_since_last_report)."
        last_report_time=$current_time
    fi
done

# Final report
current_time=$(date +%s)
elapsed_time=$((current_time - start_time))
echo "Generated $num_files empty files in $directory. Total elapsed time: $(format_time $elapsed_time)."
