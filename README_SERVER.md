# 🚀 QuickJS Test Server

This directory contains test scripts and server setup for testing remote JavaScript execution in the QuickJS Android App.

## 📁 Files Overview

### Server Scripts
- `start_server.sh` - Automated server launcher (Node.js or Python)
- `get_ip.sh` - Get local IP address for network configuration
- `js_server.js` - Node.js server with API endpoints

### Test Scripts (`test-server/`)
- `test_remote_script.js` - Basic remote JavaScript execution test
- `test_fetch_polyfill.js` - HTTP polyfills (fetch, XMLHttpRequest) testing
- `test_cache_system.js` - Comprehensive caching system test
- `test_cache_system_fast.js` - Fast version of cache test (no external HTTP)
- `test_bytecode_demo.js` - Bytecode compilation demonstration
- `test_cache_stats.js` - Cache statistics and monitoring
- `simple_fetch_test.js` - Simple HTTP fetch test
- `test_simple_return.js` - Minimal test script

## 🚀 Quick Start

### Option 1: Automatic Server (Recommended)
```bash
./start_server.sh
```

This will:
- Auto-detect Node.js or Python 3
- Start the best available server
- Display your local IP for Android configuration

### Option 2: Manual Server Selection
```bash
# Force Node.js server
./start_server.sh node

# Force Python server  
./start_server.sh python
```

### Option 3: Manual Python Server
```bash
cd test-server
python3 -m http.server 8000
```

### Option 4: Manual Node.js Server
```bash
node js_server.js
```

## 📱 Android App Configuration

1. **Start the server** using one of the methods above
2. **Note your IP address** (displayed when server starts)
3. **In the Android app**, go to "Remote JS" tab
4. **Configure IP** in the local server settings (e.g., `192.168.1.100:8000`)
5. **Test the scripts** using the provided examples

## 🧪 Test Scripts Explained

### Basic Remote Script (`test_remote_script.js`)
- Tests basic JavaScript execution
- JSON serialization
- Console logging
- Object creation

### HTTP Polyfills Test (`test_fetch_polyfill.js`)
- Tests `fetch()` polyfill availability
- Tests `XMLHttpRequest` polyfill
- Demonstrates async HTTP requests
- **Use this to test HTTP polyfills functionality**

### Cache System Tests
- **`test_cache_system.js`**: Full caching test with external HTTP
- **`test_cache_system_fast.js`**: Fast version without external HTTP
- Tests bytecode caching
- Fibonacci computation for performance testing
- Memory management testing

### Bytecode Demo (`test_bytecode_demo.js`)
- Demonstrates JavaScript compilation to bytecode
- Performance comparison tests
- Caching effectiveness measurement

## 🌐 URLs for Android App

When server is running on `192.168.1.100:8000`:

- **Basic Test**: `http://192.168.1.100:8000/test_remote_script.js`
- **HTTP Polyfills**: `http://192.168.1.100:8000/test_fetch_polyfill.js`
- **Cache System**: `http://192.168.1.100:8000/test_cache_system_fast.js`
- **Bytecode Demo**: `http://192.168.1.100:8000/test_bytecode_demo.js`

## 🔧 Troubleshooting

### Server Won't Start
- Ensure Node.js or Python 3 is installed
- Check if ports 8000 or 8080 are available
- Try different ports: `python3 -m http.server 8001`

### Android Can't Connect
- Ensure both devices are on the same WiFi network
- Check firewall settings
- Verify IP address with `./get_ip.sh`
- Try `http://` prefix (not `https://`)

### JavaScript Errors
- Check server logs for file access issues
- Ensure scripts are in `test-server/` directory
- Verify script syntax in a regular browser first

## 🎯 Testing HTTP Polyfills

The QuickJS Android App includes HTTP polyfills for:
- `fetch()` API
- `XMLHttpRequest` 
- Promise-based async operations

Use `test_fetch_polyfill.js` to verify these work correctly.

## 📊 Performance Testing

Use the cache system tests to measure:
- Script loading performance
- Bytecode compilation benefits
- Memory usage optimization
- HTTP caching effectiveness

## 🔍 Debugging

1. **Check server logs** for request details
2. **Monitor Android logcat** for QuickJS execution logs
3. **Use fast versions** of tests to avoid network timeouts
4. **Test locally** in browser first to verify script syntax

---

**Happy Testing! 🚀**
