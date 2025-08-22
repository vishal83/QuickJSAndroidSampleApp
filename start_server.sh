#!/bin/bash

# 🚀 QuickJS Remote Script Server Launcher
# Automatically detects and starts the best available server

set -e

echo "🚀 QuickJS Remote Script Server Launcher"
echo "========================================"

# Function to get local IP address
get_local_ip() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1 | awk '{print $2}')
    else
        # Linux
        LOCAL_IP=$(hostname -I | awk '{print $1}')
    fi
    echo "$LOCAL_IP"
}

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1  # Port is in use
    else
        return 0  # Port is available
    fi
}

# Function to start Python server
start_python_server() {
    local port=${1:-8000}
    echo "🐍 Starting Python HTTP server on port $port..."
    echo "📁 Serving files from: $(pwd)"
    echo "📱 Android URL: http://$(get_local_ip):$port/test_remote_script.js"
    echo "🌐 Local URL: http://localhost:$port/test_remote_script.js"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "----------------------------------------"
    python3 -m http.server $port
}

# Function to start Node.js server
start_node_server() {
    echo "🟢 Starting Node.js server..."
    echo "📁 Serving files from: $(pwd)"
    echo "📱 Android URL: http://$(get_local_ip):8080/test_remote_script.js"
    echo "🌐 Local URL: http://localhost:8080/"
    echo "📡 API URL: http://localhost:8080/api/files"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "----------------------------------------"
    node js_server.js
}

# Main logic
echo "🔍 Checking available options..."

# Check if test file exists
if [ ! -f "test_remote_script.js" ]; then
    echo "❌ test_remote_script.js not found in current directory"
    echo "Please run this script from the project root directory"
    exit 1
fi

echo "✅ test_remote_script.js found"

# Check for Node.js
if command -v node >/dev/null 2>&1; then
    echo "✅ Node.js available ($(node --version))"
    NODE_AVAILABLE=true
else
    echo "❌ Node.js not available"
    NODE_AVAILABLE=false
fi

# Check for Python
if command -v python3 >/dev/null 2>&1; then
    echo "✅ Python 3 available ($(python3 --version))"
    PYTHON_AVAILABLE=true
else
    echo "❌ Python 3 not available"
    PYTHON_AVAILABLE=false
fi

echo ""

# Parse command line arguments
SERVER_TYPE=""
if [ $# -gt 0 ]; then
    case $1 in
        "node"|"nodejs")
            SERVER_TYPE="node"
            ;;
        "python"|"py")
            SERVER_TYPE="python"
            ;;
        "auto")
            SERVER_TYPE="auto"
            ;;
        *)
            echo "Usage: $0 [node|python|auto]"
            echo "  node   - Force Node.js server"
            echo "  python - Force Python server"
            echo "  auto   - Auto-detect best server (default)"
            exit 1
            ;;
    esac
else
    SERVER_TYPE="auto"
fi

# Start appropriate server
case $SERVER_TYPE in
    "node")
        if [ "$NODE_AVAILABLE" = true ]; then
            start_node_server
        else
            echo "❌ Node.js not available. Install with: brew install node"
            exit 1
        fi
        ;;
    "python")
        if [ "$PYTHON_AVAILABLE" = true ]; then
            start_python_server
        else
            echo "❌ Python 3 not available. Please install Python 3"
            exit 1
        fi
        ;;
    "auto")
        if [ "$NODE_AVAILABLE" = true ] && check_port 8080; then
            echo "🎯 Auto-selected: Node.js server (recommended)"
            start_node_server
        elif [ "$PYTHON_AVAILABLE" = true ] && check_port 8000; then
            echo "🎯 Auto-selected: Python server"
            start_python_server
        elif [ "$PYTHON_AVAILABLE" = true ]; then
            # Try alternative ports for Python
            for port in 8001 8002 8003; do
                if check_port $port; then
                    echo "🎯 Auto-selected: Python server on port $port"
                    start_python_server $port
                    exit 0
                fi
            done
            echo "❌ No available ports found"
            exit 1
        else
            echo "❌ No suitable server found. Please install Node.js or Python 3"
            exit 1
        fi
        ;;
esac
