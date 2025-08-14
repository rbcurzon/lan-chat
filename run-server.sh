#!/bin/bash

echo "Starting Broadcast Echo Server..."
echo "Press Ctrl+C to stop"

mvn compile exec:java -Dexec.mainClass="com.example.BroadcastEchoServer"
