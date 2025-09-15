const express = require('express');
const app = express();
app.get('/source', (req, res) => {
  const dice = Math.random();
  // 40% falla por timeout (no responde), 20% 500, 40% OK
  if (dice < 0.4) {
    // no respondemos => simula timeout
  } else if (dice < 0.6) {
    res.status(500).send('Upstream error');
  } else {
    const id = req.query.trackId || 'T0';
    res.send(`https://cdn.example/high-bitrate/${id}`);
  }
});
app.listen(9090, () => console.log('flaky-service on 9090'));
