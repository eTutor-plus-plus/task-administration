#!/bin/bash

INDEX_FILE="/app/BOOT-INF/classes/static/app/index.html"

if [ -f "$INDEX_FILE" ]; then
    # Check if SERVER_SERVLET_CONTEXT_PATH is set and not empty
    if [ -z "$SERVER_SERVLET_CONTEXT_PATH" ]; then
        sed -i.bak "s|<base href=\".*\"|<base href=\"/app/\"|" "$INDEX_FILE"
        echo "Updated base href in $INDEX_FILE to /app/"
    else
        sed -i.bak "s|<base href=\".*\"|<base href=\"$SERVER_SERVLET_CONTEXT_PATH/app/\"|" "$INDEX_FILE"
        echo "Updated base href in $INDEX_FILE to $SERVER_SERVLET_CONTEXT_PATH/app/"
    fi
else
    echo "Warning: $INDEX_FILE not found."
fi

# Start app
echo "Starting task administration ..."
java -Xmx1g org.springframework.boot.loader.launch.JarLauncher
