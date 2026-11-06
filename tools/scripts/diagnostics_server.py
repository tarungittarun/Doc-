#!/usr/bin/env python3
"""OneDoc Diagnostics Local Server.

Provides a local HTTP server to view and manage diagnostics logs
without requiring Java/Android tools. Works natively in the sandbox.

Endpoints:
  GET  /                    - List all diagnostics log files
  GET  /logs/<name>         - View a specific log file
  POST /logs                - Upload a new diagnostics log
  DELETE /logs/<name>       - Delete a log file
  GET  /stats               - Get summary statistics
"""

import os
import sys
import json
import hashlib
from datetime import datetime
from pathlib import Path
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

LOGS_DIR = Path(__file__).resolve().parent.parent / "diagnostics_logs"
LOGS_DIR.mkdir(exist_ok=True)


class DiagnosticsHandler(BaseHTTPRequestHandler):
    """HTTP request handler for diagnostics server."""

    def log_message(self, format, *args):
        """Suppress default log output."""
        pass

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path

        if path == "/":
            self._handle_list()
        elif path.startswith("/logs/"):
            name = path[len("/logs/"):]
            self._handle_get_log(name)
        elif path == "/stats":
            self._handle_stats()
        else:
            self._send_error(404, "Not found")

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path == "/logs":
            self._handle_upload()
        else:
            self._send_error(404, "Not found")

    def do_DELETE(self):
        parsed = urlparse(self.path)
        if parsed.path.startswith("/logs/"):
            name = path[len("/logs/"):]
            self._handle_delete(name)
        else:
            self._send_error(404, "Not found")

    def _handle_list(self):
        """List all diagnostic log files."""
        files = []
        for f in sorted(LOGS_DIR.iterdir()):
            if f.is_file():
                stat = f.stat()
                files.append({
                    "name": f.name,
                    "size": stat.st_size,
                    "modified": datetime.fromtimestamp(stat.st_mtime).isoformat(),
                    "sha256": hashlib.sha256(f.read_bytes()).hexdigest()[:16],
                })
        self._send_json({"files": files, "count": len(files)})

    def _handle_get_log(self, name):
        """Get a specific log file."""
        filepath = LOGS_DIR / name
        if not filepath.exists() or not filepath.is_file():
            self._send_error(404, "Log not found")
            return
        content = filepath.read_text(encoding="utf-8", errors="replace")
        self._send_json({"name": name, "content": content, "size": len(content)})

    def _handle_upload(self):
        """Upload a new diagnostics log."""
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length)
        try:
            data = json.loads(body)
            name = data.get("name", "diagnostics.log")
            content = data.get("content", "")
        except json.JSONDecodeError:
            name = "diagnostics.log"
            content = body.decode("utf-8", errors="replace")

        filepath = LOGS_DIR / name
        filepath.write_text(content, encoding="utf-8")
        self._send_json({"status": "ok", "name": name, "size": len(content)})

    def _handle_delete(self, name):
        """Delete a log file."""
        filepath = LOGS_DIR / name
        if not filepath.exists():
            self._send_error(404, "Log not found")
            return
        filepath.unlink()
        self._send_json({"status": "deleted", "name": name})

    def _handle_stats(self):
        """Get summary statistics."""
        files = list(LOGS_DIR.iterdir())
        total_size = sum(f.stat().st_size for f in files if f.is_file())
        self._send_json({
            "file_count": len([f for f in files if f.is_file()]),
            "total_size": total_size,
            "logs_dir": str(LOGS_DIR),
        })

    def _send_json(self, data, status=200):
        body = json.dumps(data, indent=2).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _send_error(self, code, message):
        body = json.dumps({"error": message}).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def main():
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8765
    server = HTTPServer(("0.0.0.0", port), DiagnosticsHandler)
    print(f"OneDoc Diagnostics Server running on http://0.0.0.0:{port}")
    print(f"Logs directory: {LOGS_DIR}")
    print("Press Ctrl+C to stop")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down...")
        server.shutdown()


if __name__ == "__main__":
    main()
