#!/bin/bash
# Script to start the Python AI service for Student Attendance Management System

# Get the directory of the script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if venv exists
if [ ! -d "venv" ]; then
    echo "Error: Virtual environment 'venv' not found in $SCRIPT_DIR"
    echo "Please create it using: python3 -m venv venv"
    exit 1
fi

# Activate venv and run app
echo "Starting Python AI service..."
source venv/bin/activate
python app.py
