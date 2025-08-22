#!/usr/bin/env node

/**
 * Simple Node.js HTTP Server for serving JavaScript files to Android app
 * Features: CORS support, proper headers, logging, multiple file support
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 8080;
const HOST = process.env.HOST || '0.0.0.0'; // Listen on all interfaces

// MIME types for different file extensions
const MIME_TYPES = {
    '.js': 'text/javascript',
    '.mjs': 'text/javascript',
    '.json': 'application/json',
    '.html': 'text/html',
    '.css': 'text/css',
    '.txt': 'text/plain'
};

// Available JavaScript files
const JS_FILES = {
    'test_remote_script.js': {
        description: 'Comprehensive QuickJS test script',
        features: ['ES2023', 'JSON', 'Math', 'Arrays', 'Functions']
    }
};

// CORS headers for mobile app compatibility
const CORS_HEADERS = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, HEAD, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, User-Agent',
    'Access-Control-Max-Age': '86400' // 24 hours
};

// Logging function
function log(message) {
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] ${message}`);
}

// Create HTTP server
const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;
    const method = req.method;
    
    // Log request
    log(`${method} ${pathname} - ${req.headers['user-agent'] || 'Unknown'}`);
    
    // Handle CORS preflight requests
    if (method === 'OPTIONS') {
        res.writeHead(200, CORS_HEADERS);
        res.end();
        return;
    }
    
    // Root endpoint - show available files
    if (pathname === '/' || pathname === '/index.html') {
        const html = generateIndexPage();
        res.writeHead(200, {
            'Content-Type': 'text/html',
            ...CORS_HEADERS
        });
        res.end(html);
        return;
    }
    
    // API endpoint - list available JavaScript files
    if (pathname === '/api/files') {
        const fileList = Object.entries(JS_FILES).map(([filename, info]) => ({
            filename,
            url: `http://${req.headers.host}/${filename}`,
            ...info
        }));
        
        res.writeHead(200, {
            'Content-Type': 'application/json',
            ...CORS_HEADERS
        });
        res.end(JSON.stringify({ files: fileList }, null, 2));
        return;
    }
    
    // Serve JavaScript files
    const filename = pathname.substring(1); // Remove leading slash
    const filePath = path.join(__dirname, filename);
    const ext = path.extname(filename).toLowerCase();
    
    // Check if file exists
    if (!fs.existsSync(filePath)) {
        log(`File not found: ${filename}`);
        res.writeHead(404, {
            'Content-Type': 'text/plain',
            ...CORS_HEADERS
        });
        res.end('File not found');
        return;
    }
    
    // Check if it's a supported file type
    const mimeType = MIME_TYPES[ext] || 'text/plain';
    
    try {
        const fileContent = fs.readFileSync(filePath, 'utf8');
        const stats = fs.statSync(filePath);
        
        log(`Serving ${filename} (${fileContent.length} chars, ${mimeType})`);
        
        res.writeHead(200, {
            'Content-Type': mimeType,
            'Content-Length': Buffer.byteLength(fileContent, 'utf8'),
            'Last-Modified': stats.mtime.toUTCString(),
            'Cache-Control': 'no-cache', // Prevent caching for development
            ...CORS_HEADERS
        });
        res.end(fileContent);
        
    } catch (error) {
        log(`Error serving ${filename}: ${error.message}`);
        res.writeHead(500, {
            'Content-Type': 'text/plain',
            ...CORS_HEADERS
        });
        res.end('Internal server error');
    }
});

// Generate HTML index page
function generateIndexPage() {
    const fileList = Object.entries(JS_FILES)
        .map(([filename, info]) => `
            <li>
                <strong><a href="/${filename}">${filename}</a></strong>
                <br><small>${info.description}</small>
                <br><em>Features: ${info.features.join(', ')}</em>
            </li>
        `).join('');
    
    return `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QuickJS Remote Script Server</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
        .header { background: #f4f4f4; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .file-list li { margin: 15px 0; padding: 10px; background: #f9f9f9; border-left: 4px solid #007cba; }
        .endpoints { background: #e8f4f8; padding: 15px; border-radius: 5px; margin: 20px 0; }
        code { background: #f4f4f4; padding: 2px 6px; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🚀 QuickJS Remote Script Server</h1>
        <p>Server for testing remote JavaScript execution in Android QuickJS app</p>
        <p><strong>Server:</strong> http://localhost:${PORT}</p>
    </div>
    
    <h2>📄 Available JavaScript Files</h2>
    <ul class="file-list">${fileList}</ul>
    
    <div class="endpoints">
        <h3>📡 API Endpoints</h3>
        <ul>
            <li><code>GET /</code> - This page</li>
            <li><code>GET /api/files</code> - JSON list of available files</li>
            <li><code>GET /filename.js</code> - Download JavaScript file</li>
        </ul>
    </div>
    
    <h3>📱 Android App Usage</h3>
    <ol>
        <li>Open your V8EngineAndroidApp</li>
        <li>Go to "Remote JS" tab</li>
        <li>Initialize QuickJS engine</li>
        <li>Enter URL: <code>http://YOUR_IP:${PORT}/test_remote_script.js</code></li>
        <li>Click "Execute Remote JS"</li>
    </ol>
    
    <p><small>💡 Replace YOUR_IP with your computer's IP address when testing on device</small></p>
</body>
</html>`;
}

// Start server
server.listen(PORT, HOST, () => {
    log(`🚀 JavaScript server started`);
    log(`📡 Listening on http://${HOST}:${PORT}`);
    log(`📱 For Android testing, use: http://YOUR_IP:${PORT}/test_remote_script.js`);
    log(`🌐 Web interface: http://localhost:${PORT}`);
    
    // Show local IP addresses
    const networkInterfaces = require('os').networkInterfaces();
    const addresses = [];
    
    for (const interfaceName in networkInterfaces) {
        for (const iface of networkInterfaces[interfaceName]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                addresses.push(iface.address);
            }
        }
    }
    
    if (addresses.length > 0) {
        log(`📍 Local IP addresses:`);
        addresses.forEach(addr => {
            log(`   http://${addr}:${PORT}/test_remote_script.js`);
        });
    }
});

// Handle server shutdown gracefully
process.on('SIGINT', () => {
    log('🛑 Shutting down server...');
    server.close(() => {
        log('✅ Server stopped');
        process.exit(0);
    });
});

// Handle uncaught exceptions
process.on('uncaughtException', (error) => {
    log(`❌ Uncaught exception: ${error.message}`);
    process.exit(1);
});
