const express = require('express');
const https = require('https');
const fs = require('fs');
const path = require('path');

const app = express();
const port = 3000;

const httpsOptions = {
  key: fs.readFileSync(path.join(__dirname, 'key.pem')), // Đường dẫn tới key.pem
  cert: fs.readFileSync(path.join(__dirname, 'cert.pem')), // Đường dẫn tới cert.pem
};

app.use(express.static(path.join(__dirname, 'public')));

const server = https.createServer(httpsOptions, app);

// Chạy server
server.listen(port, () => {
  console.log(`Frontend running at https://localhost:${port}`);
});