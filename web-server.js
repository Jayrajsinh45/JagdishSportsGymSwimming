const http = require("http");
const fs = require("fs");
const path = require("path");

const requestedPort = Number(process.env.PORT || 3000);
const root = path.join(__dirname, "web");

const contentTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml; charset=utf-8",
  ".png": "image/png",
  ".ico": "image/x-icon"
};

function resolveRequestPath(url) {
  const parsed = new URL(url, "http://localhost");
  const requested = parsed.pathname === "/" ? "/index.html" : parsed.pathname;
  const safePath = path.normalize(decodeURIComponent(requested)).replace(/^(\.\.[/\\])+/, "");
  return path.join(root, safePath);
}

const server = http.createServer((req, res) => {
  if (!["GET", "HEAD"].includes(req.method)) {
    res.writeHead(405);
    res.end("Method Not Allowed");
    return;
  }

  const filePath = resolveRequestPath(req.url);
  if (!filePath.startsWith(root)) {
    res.writeHead(403);
    res.end("Forbidden");
    return;
  }

  fs.readFile(filePath, (error, content) => {
    if (error) {
      res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
      res.end("Not Found");
      return;
    }

    const contentType = contentTypes[path.extname(filePath)] || "application/octet-stream";
    res.writeHead(200, {
      "Content-Type": contentType,
      "Cache-Control": "no-store"
    });

    if (req.method === "HEAD") {
      res.end();
      return;
    }

    res.end(content);
  });
});

function startServer(port, attemptsLeft = 20) {
  server.once("error", (error) => {
    if (error.code === "EADDRINUSE" && attemptsLeft > 0) {
      const nextPort = port + 1;
      console.log(`Port ${port} is already in use. Trying http://localhost:${nextPort} ...`);
      startServer(nextPort, attemptsLeft - 1);
      return;
    }

    console.error(error.message);
    process.exit(1);
  });

  server.listen(port, () => {
    console.log(`Jagdish Sports web tester running at http://localhost:${port}`);
    console.log("Press Ctrl+C to stop.");
  });
}

startServer(requestedPort);
