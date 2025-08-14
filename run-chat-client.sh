#!/bin/bash

echo "Starting Chat Client..."
echo "Press Ctrl+C to exit"

mvn compile exec:java -Dexec.mainClass="com.example.ChatClient"
