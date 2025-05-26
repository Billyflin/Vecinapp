// Ejemplo de flujo OAuth (requiere un servidor)
const express = require('express');
const axios = require('axios');
const app = express();

const CLIENT_ID = '3964I8DY4NM0NIXJMZUH39SMPCBV3VYN';
const CLIENT_SECRET = '2SKR4LV73ATON6NYX7OPJ98D1DI6LWDMGBW0DW11W76OPXRDNJ4DGL79MG0T2JWC';
const REDIRECT_URI = 'http://localhost:3000/auth/callback';

// Paso 1: Redirigir al usuario a ClickUp para autorización
app.get('/auth', (req, res) => {
  res.redirect(`https://app.clickup.com/api?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}`);
});

// Paso 2: Recibir el código de autorización
app.get('/auth/callback', async (req, res) => {
  const code = req.query.code;

  try {
    // Paso 3: Intercambiar código por token
    const tokenResponse = await axios.post('https://api.clickup.com/api/v2/oauth/token', {
      client_id: CLIENT_ID,
      client_secret: CLIENT_SECRET,
      code: code,
      redirect_uri: REDIRECT_URI
    });

    const accessToken = tokenResponse.data.access_token;

    // Ahora puedes usar este token para hacer solicitudes a la API
    // Almacénalo de forma segura

    res.send('Autorización exitosa!');
  } catch (error) {
    console.error('Error:', error.message);
    res.status(500).send('Error en la autorización');
  }
});

app.listen(3000, () => {
  console.log('Servidor iniciado en http://localhost:3000');
});