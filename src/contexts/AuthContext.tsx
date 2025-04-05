// src/contexts/AuthContext.tsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import { getAuthService } from '../services';

type AuthContextType = {
  user: { uid: string } | null;
  loading: boolean;
  signIn: (phoneNumber: string, otp: string) => Promise<boolean>;
  signOut: () => Promise<boolean>;
  sendPhoneVerification: (phoneNumber: string) => Promise<boolean>;
};

const AuthContext = createContext<AuthContextType>({
  user: null,
  loading: true,
  signIn: async () => false,
  signOut: async () => false,
  sendPhoneVerification: async () => false,
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<{ uid: string } | null>(null);
  const [loading, setLoading] = useState(true);
  
  const authService = getAuthService();
  
  // Check auth state on mount
  useEffect(() => {
    const checkAuthState = async () => {
      try {
        const currentUser = await authService.getCurrentUser();
        setUser(currentUser);
      } catch (error) {
        console.error('Error checking auth state:', error);
      } finally {
        setLoading(false);
      }
    };
    
    checkAuthState();
  }, []);
  
  const signIn = async (phoneNumber: string, otp: string): Promise<boolean> => {
    try {
      const verified = await authService.verifyOtp(otp);
      
      if (verified) {
        const currentUser = await authService.getCurrentUser();
        setUser(currentUser);
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('Error signing in:', error);
      return false;
    }
  };
  
  const signOut = async (): Promise<boolean> => {
    try {
      const success = await authService.signOut();
      
      if (success) {
        setUser(null);
      }
      
      return success;
    } catch (error) {
      console.error('Error signing out:', error);
      return false;
    }
  };
  
  const sendPhoneVerification = async (phoneNumber: string): Promise<boolean> => {
    try {
      return await authService.sendPhoneVerification(phoneNumber);
    } catch (error) {
      console.error('Error sending verification:', error);
      return false;
    }
  };
  
  return (
    <AuthContext.Provider 
      value={{ 
        user, 
        loading, 
        signIn, 
        signOut, 
        sendPhoneVerification 
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);