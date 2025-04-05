// src/contexts/MatchContext.tsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import { getMatchService } from '../services';
import { Match, ProximityEvent } from '../services/types';
import { useAuth } from './AuthContext';
import { useLocation } from './LocationContext';

type NearbyUser = {
  id: string;
  name: string;
  age: number;
  photo: string;
  distance: number;
};

type MatchContextType = {
  matches: Match[];
  proximityEvents: ProximityEvent[];
  nearbyUsers: NearbyUser[];
  isLoading: boolean;
  refreshNearbyUsers: () => Promise<void>;
  createMatch: (proximityEventId: string) => Promise<string | null>;
  shareInstagram: (matchId: string, shared: boolean) => Promise<boolean>;
  simulateProximityEvent: (otherUserId: string, distance: number) => string;
};

const MatchContext = createContext<MatchContextType>({
  matches: [],
  proximityEvents: [],
  nearbyUsers: [],
  isLoading: false,
  refreshNearbyUsers: async () => {},
  createMatch: async () => null,
  shareInstagram: async () => false,
  simulateProximityEvent: () => '',
});

export const MatchProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const { currentLocation } = useLocation();
  const [matches, setMatches] = useState<Match[]>([]);
  const [proximityEvents, setProximityEvents] = useState<ProximityEvent[]>([]);
  const [nearbyUsers, setNearbyUsers] = useState<NearbyUser[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  
  const matchService = getMatchService();
  
  // Load data when user or location changes
  useEffect(() => {
    if (!user || !currentLocation) return;
    
    const loadData = async () => {
      setIsLoading(true);
      try {
        // Load matches
        const userMatches = await matchService.getMatches(user.uid);
        setMatches(userMatches);
        
        // Load proximity events
        const events = await matchService.getProximityEvents(user.uid);
        setProximityEvents(events);
        
        // Find nearby users
        await refreshNearbyUsers();
      } catch (error) {
        console.error('Error loading match data:', error);
      } finally {
        setIsLoading(false);
      }
    };
    
    loadData();
  }, [user, currentLocation]);
  
  // Refresh nearby users
  const refreshNearbyUsers = async (): Promise<void> => {
    if (!user || !currentLocation) return;
    
    setIsLoading(true);
    try {
      const nearby = await matchService.findNearbyUsers(
        { lat: currentLocation.latitude, lng: currentLocation.longitude },
        500 // 500 meters radius
      );
      setNearbyUsers(nearby);
    } catch (error) {
      console.error('Error finding nearby users:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  // Create a match
  const createMatch = async (proximityEventId: string): Promise<string | null> => {
    if (!user) return null;
    
    try {
      const matchId = await matchService.createMatch(proximityEventId);
      if (matchId) {
        // Refresh matches
        const userMatches = await matchService.getMatches(user.uid);
        setMatches(userMatches);
        
        // Update proximity events
        const events = await matchService.getProximityEvents(user.uid);
        setProximityEvents(events);
      }
      return matchId;
    } catch (error) {
      console.error('Error creating match:', error);
      return null;
    }
  };
  
  // Share Instagram
  const shareInstagram = async (matchId: string, shared: boolean): Promise<boolean> => {
    if (!user) return false;
    
    try {
      const success = await matchService.updateInstagramSharing(matchId, user.uid, shared);
      if (success) {
        // Update matches state
        setMatches(matches.map(match => 
          match.id === matchId 
            ? { ...match, instagramShared: { ...match.instagramShared, [user.uid]: shared } }
            : match
        ));
      }
      return success;
    } catch (error) {
      console.error('Error sharing Instagram:', error);
      return false;
    }
  };
  
  // This is for testing - simulate a proximity event
  const simulateProximityEvent = (otherUserId: string, distance: number): string => {
    if (!user) return '';
    
    const eventId = (matchService as any).simulateProximityEvent(user.uid, otherUserId, distance);
    
    // Update proximity events
    setProximityEvents([...proximityEvents, {
      id: eventId,
      users: [user.uid, otherUserId],
      distance,
      startTime: new Date(),
      status: 'active',
      location: { geohash: 'mock-geohash' }
    }]);
    
    return eventId;
  };
  
  return (
    <MatchContext.Provider
      value={{
        matches,
        proximityEvents,
        nearbyUsers,
        isLoading,
        refreshNearbyUsers,
        createMatch,
        shareInstagram,
        simulateProximityEvent
      }}
    >
      {children}
    </MatchContext.Provider>
  );
};

export const useMatch = () => useContext(MatchContext);