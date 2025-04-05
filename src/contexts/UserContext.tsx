// src/contexts/UserContext.tsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import { getUserService } from '../services';
import { UserProfile, UserPreferences } from '../services/types';
import { useAuth } from './AuthContext';

type UserContextType = {
  userProfile: UserProfile | null;
  userPreferences: UserPreferences | null;
  isProfileLoading: boolean;
  updateProfile: (profileData: Partial<UserProfile>) => Promise<boolean>;
  updatePreferences: (preferencesData: Partial<UserPreferences>) => Promise<boolean>;
};

const UserContext = createContext<UserContextType>({
  userProfile: null,
  userPreferences: null,
  isProfileLoading: true,
  updateProfile: async () => false,
  updatePreferences: async () => false,
});

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [userPreferences, setUserPreferences] = useState<UserPreferences | null>(null);
  const [isProfileLoading, setIsProfileLoading] = useState(true);
  
  const userService = getUserService();
  
  // Load user profile when auth state changes
  useEffect(() => {
    const loadUserProfile = async () => {
      if (!user) {
        setUserProfile(null);
        setUserPreferences(null);
        setIsProfileLoading(false);
        return;
      }
      
      setIsProfileLoading(true);
      try {
        const profile = await userService.getUserProfile(user.uid);
        setUserProfile(profile);
        
        // Mock preferences for now
        setUserPreferences({
          discoveryDistance: 500,
          ageRangeMin: 18,
          ageRangeMax: 35,
          genderPreference: ['male', 'female'],
          showInstagramTo: 'matches'
        });
      } catch (error) {
        console.error('Error loading profile:', error);
      } finally {
        setIsProfileLoading(false);
      }
    };
    
    loadUserProfile();
  }, [user]);
  
  const updateProfile = async (profileData: Partial<UserProfile>): Promise<boolean> => {
    if (!user || !userProfile) return false;
    
    try {
      const success = await userService.updateUserProfile(user.uid, profileData);
      if (success) {
        setUserProfile({ ...userProfile, ...profileData });
      }
      return success;
    } catch (error) {
      console.error('Error updating profile:', error);
      return false;
    }
  };
  
  const updatePreferences = async (preferencesData: Partial<UserPreferences>): Promise<boolean> => {
    if (!user || !userPreferences) return false;
    
    try {
      const success = await userService.updateUserPreferences(user.uid, preferencesData);
      if (success) {
        setUserPreferences({ ...userPreferences, ...preferencesData });
      }
      return success;
    } catch (error) {
      console.error('Error updating preferences:', error);
      return false;
    }
  };
  
  return (
    <UserContext.Provider 
      value={{ 
        userProfile, 
        userPreferences, 
        isProfileLoading, 
        updateProfile, 
        updatePreferences 
      }}
    >
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => useContext(UserContext);