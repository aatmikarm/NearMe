// src/services/types.ts

// User types
export interface UserProfile {
  uid: string;
  displayName: string;
  age: number;
  gender: string;
  bio?: string;
  photos: UserPhoto[];
  instagramId?: string;
  instagramConnected: boolean;
}

export interface UserPhoto {
  id: string;
  url: string;
  thumbnailUrl?: string;
  isPrimary: boolean;
}

export interface UserLocation {
  geohash: string;
  lat: number;
  lng: number;
  accuracy?: number;
  updatedAt: Date;
}

export interface UserPreferences {
  discoveryDistance: number;
  ageRangeMin: number;
  ageRangeMax: number;
  genderPreference: string[];
  showInstagramTo: 'all' | 'matches' | 'none';
}

// Match types
export interface ProximityEvent {
  id: string;
  users: string[];
  distance: number;
  startTime: Date;
  endTime?: Date;
  status: 'active' | 'ended' | 'matched' | 'ignored';
  location?: {
    geohash: string;
    placeName?: string;
  };
}

export interface Match {
  id: string;
  users: string[];
  matchedAt: Date;
  proximityEventId: string;
  status: 'active' | 'deleted';
  instagramShared: {
    [uid: string]: boolean;
  };
  lastInteraction?: Date;
}

// Service interfaces
export interface AuthService {
  sendPhoneVerification(phoneNumber: string): Promise<boolean>;
  verifyOtp(otp: string): Promise<boolean>;
  getCurrentUser(): Promise<{uid: string} | null>;
  signOut(): Promise<boolean>;
}

export interface UserService {
  createUserProfile(userId: string, profileData: Partial<UserProfile>): Promise<boolean>;
  getUserProfile(userId: string): Promise<UserProfile | null>;
  updateUserProfile(userId: string, profileData: Partial<UserProfile>): Promise<boolean>;
  updateUserLocation(userId: string, location: Partial<UserLocation>): Promise<boolean>;
  updateUserPreferences(userId: string, preferences: Partial<UserPreferences>): Promise<boolean>;
}

export interface MatchService {
  findNearbyUsers(location: {lat: number, lng: number}, radiusInM?: number): Promise<any[]>;
  getProximityEvents(userId: string): Promise<ProximityEvent[]>;
  getMatches(userId: string): Promise<Match[]>;
  createMatch(proximityEventId: string): Promise<string | null>;
  updateInstagramSharing(matchId: string, userId: string, shared: boolean): Promise<boolean>;
}