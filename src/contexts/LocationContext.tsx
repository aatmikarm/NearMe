// src/contexts/LocationContext.tsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import { getUserService } from '../services';
import { useAuth } from './AuthContext';

type Location = {
  latitude: number;
  longitude: number;
  accuracy?: number;
};

type LocationContextType = {
  currentLocation: Location | null;
  isTrackingLocation: boolean;
  locationPermissionGranted: boolean;
  locationError: string | null;
  startLocationTracking: () => Promise<void>;
  stopLocationTracking: () => void;
  requestLocationPermission: () => Promise<boolean>;
};

const defaultLocation = {
  // Jaipur coordinates as default
  latitude: 26.9124,
  longitude: 75.7873,
};

const LocationContext = createContext<LocationContextType>({
  currentLocation: null,
  isTrackingLocation: false,
  locationPermissionGranted: false,
  locationError: null,
  startLocationTracking: async () => {},
  stopLocationTracking: () => {},
  requestLocationPermission: async () => false,
});

export const LocationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [currentLocation, setCurrentLocation] = useState<Location | null>(null);
  const [isTrackingLocation, setIsTrackingLocation] = useState(false);
  const [locationPermissionGranted, setLocationPermissionGranted] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [locationWatchId, setLocationWatchId] = useState<number | null>(null);
  
  const userService = getUserService();
  
  // Mock requesting location permission
  const requestLocationPermission = async (): Promise<boolean> => {
    // Simulate asking for permission
    console.log('Requesting location permission...');
    // In production, this would use the Geolocation API permission request
    setLocationPermissionGranted(true);
    return true;
  };
  
  // Start tracking location
  const startLocationTracking = async (): Promise<void> => {
    if (!locationPermissionGranted) {
      const granted = await requestLocationPermission();
      if (!granted) {
        setLocationError('Location permission denied');
        return;
      }
    }
    
    if (!user) return;
    
    setIsTrackingLocation(true);
    
    // Simulate getting location in development
    // In production, this would use navigator.geolocation.watchPosition
    const mockLocationUpdate = () => {
      // Generate location with small random variations
      const newLocation = {
        latitude: defaultLocation.latitude + (Math.random() - 0.5) * 0.01,
        longitude: defaultLocation.longitude + (Math.random() - 0.5) * 0.01,
        accuracy: 10
      };
      
      setCurrentLocation(newLocation);
      
      // Update location in service
      userService.updateUserLocation(user.uid, {
        lat: newLocation.latitude,
        lng: newLocation.longitude,
        accuracy: newLocation.accuracy
      });
    };
    
    // Initial update
    mockLocationUpdate();
    
    // Update every 30 seconds
    const intervalId = setInterval(mockLocationUpdate, 30000) as unknown as number;
    setLocationWatchId(intervalId);
  };
  
  // Stop tracking location
  const stopLocationTracking = () => {
    if (locationWatchId !== null) {
      clearInterval(locationWatchId);
      setLocationWatchId(null);
    }
    setIsTrackingLocation(false);
  };
  
  // Clean up on unmount
  useEffect(() => {
    return () => {
      if (locationWatchId !== null) {
        clearInterval(locationWatchId);
      }
    };
  }, [locationWatchId]);
  
  return (
    <LocationContext.Provider
      value={{
        currentLocation,
        isTrackingLocation,
        locationPermissionGranted,
        locationError,
        startLocationTracking,
        stopLocationTracking,
        requestLocationPermission
      }}
    >
      {children}
    </LocationContext.Provider>
  );
};

export const useLocation = () => useContext(LocationContext);