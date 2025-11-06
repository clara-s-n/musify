/**
 * Environment configuration for development
 */
export const environment = {
  production: false,
  apiBaseUrl: `http://${window.location.hostname}:8080`,  // Dynamically use the current hostname
  spotifyApiUrl: 'https://api.spotify.com/v1'
};