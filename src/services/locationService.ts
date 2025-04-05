// src/services/locationService.ts
import * as geofire from 'geofire-common';

// Types for location data
type UserLocation = {
  latitude: number;
  longitude: number;
  geohash: string;
  timestamp: number;
};

// Generate geohash from coordinates
export const generateGeohash = (lat: number, lng: number): string => {
  return geofire.geohashForLocation([lat, lng]);
};

// Calculate distance between two points
export const calculateDistance = (
  lat1: number, 
  lng1: number, 
  lat2: number, 
  lng2: number
): number => {
  return geofire.distanceBetween([lat1, lng1], [lat2, lng2]);
};

// Get geohash query bounds for querying nearby users
export const getGeohashBounds = (
  centerLat: number, 
  centerLng: number, 
  radiusInM: number
): string[][] => {
  return geofire.geohashQueryBounds([centerLat, centerLng], radiusInM);
};

// Mock function to find nearby users
export const findNearbyUsersMock = (
  userLocation: UserLocation,
  allUsers: any[],
  radiusInM: number = 500
): any[] => {
  return allUsers.filter(user => {
    if (!user.location) return false;
    
    const distance = calculateDistance(
      userLocation.latitude,
      userLocation.longitude,
      user.location.latitude,
      user.location.longitude
    );
    
    // Add distance to user object
    user.distance = Math.round(distance);
    
    return distance <= radiusInM;
  }).sort((a, b) => a.distance - b.distance);
};

// Mock the user's current location (for development)
export const getCurrentLocationMock = (): Promise<UserLocation> => {
  // Simulated location (e.g., Mumbai)
  const lat = 19.076;
  const lng = 72.877;
  
  return Promise.resolve({
    latitude: lat,
    longitude: lng,
    geohash: generateGeohash(lat, lng),
    timestamp: Date.now()
  });
};