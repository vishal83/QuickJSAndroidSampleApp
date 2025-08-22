#!/bin/bash

# 📱 Get IP Address for Android App Testing
# Quick script to get your local IP address for remote JavaScript testing

echo "🌐 Finding your local IP address for Android app testing..."
echo ""

# Get the primary local IP address
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1 | awk '{print $2}')
else
    # Linux
    LOCAL_IP=$(hostname -I | awk '{print $1}')
fi

if [ -n "$LOCAL_IP" ]; then
    echo "📍 Your local IP address: $LOCAL_IP"
    echo ""
    echo "🚀 Use these URLs in your Android app:"
    echo "   Python server (port 8000): http://$LOCAL_IP:8000/test_remote_script.js"
    echo "   Node.js server (port 8080): http://$LOCAL_IP:8080/test_remote_script.js"
    echo ""
    echo "📱 In the Android app:"
    echo "   1. Go to 'Remote JS' tab"
    echo "   2. Click 'Test Remote Script'"
    echo "   3. Enter IP: $LOCAL_IP"
    echo "   4. Enter Port: 8000 (or 8080 for Node.js)"
    echo "   5. Click 'Execute Remote JS'"
else
    echo "❌ Could not find local IP address"
    echo "💡 Try manually checking with: ifconfig (macOS/Linux) or ipconfig (Windows)"
fi
