// src/services/mock/mockUser.ts
import { UserProfile, UserService, UserLocation, UserPreferences } from '../types';

// Mock user data storage
const users: Record<string, UserProfile> = {};
const locations: Record<string, UserLocation> = {};
const preferences: Record<string, UserPreferences> = {};

export class MockUserService implements UserService {
  async createUserProfile(userId: string, profileData: Partial<UserProfile>): Promise<boolean> {
    // Create a new user profile with defaults
    users[userId] = {
      uid: userId,
      displayName: profileData.displayName || 'User',
      age: profileData.age || 25,
      gender: profileData.gender || 'other',
      photos: profileData.photos || [],
      instagramConnected: profileData.instagramConnected || false,
      ...profileData
    };
    return true;
  }
  
  async getUserProfile(userId: string): Promise<UserProfile | null> {
    return users[userId] || null;
  }
  
  async updateUserProfile(userId: string, profileData: Partial<UserProfile>): Promise<boolean> {
    if (!users[userId]) return false;
    
    users[userId] = {
      ...users[userId],
      ...profileData
    };
    return true;
  }
  
  async updateUserLocation(userId: string, location: Partial<UserLocation>): Promise<boolean> {
    // Generate a simple geohash for testing
    const geohash = `${location.lat?.toFixed(5)}-${location.lng?.toFixed(5)}`;
    
    locations[userId] = {
      geohash,
      lat: location.lat || 0,
      lng: location.lng || 0,
      accuracy: location.accuracy || 10,
      updatedAt: new Date(),
      ...location
    };
    return true;
  }
  
  async updateUserPreferences(userId: string, prefs: Partial<UserPreferences>): Promise<boolean> {
    preferences[userId] = {
      discoveryDistance: prefs.discoveryDistance || 500,
      ageRangeMin: prefs.ageRangeMin || 18,
      ageRangeMax: prefs.ageRangeMax || 35,
      genderPreference: prefs.genderPreference || ['male', 'female'],
      showInstagramTo: prefs.showInstagramTo || 'matches',
      ...prefs
    };
    return true;
  }
}

export const mockUserService = new MockUserService();