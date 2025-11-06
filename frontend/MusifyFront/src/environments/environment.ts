/**
 * Environment configuration for production
 */
export const environment = {
  production: true,
  apiBaseUrl: `http://${window.location.hostname}:8080`,  // Dynamically use the current hostname
  spotifyApiUrl: 'https://api.spotify.com/v1'
};