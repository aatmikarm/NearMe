// src/contexts/AuthContext.tsx
import React, {createContext, useContext, useState, useEffect} from 'react';
import {
  sendPhoneVerification,
  verifyOtp,
  createUserProfile,
  getUserProfile,
  getCurrentUser,
  signOut as authSignOut,
} from '../services/auth';
import {User} from 'firebase/auth';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  sendVerification: (phoneNumber: string) => Promise<boolean>;
  verifyCode: (otp: string) => Promise<boolean>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{children: React.ReactNode}> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const checkUser = async () => {
      try {
        const currentUser = await getCurrentUser();
        if (currentUser) {
          const userProfile = await getUserProfile(currentUser.uid);
          setUser({...currentUser, ...userProfile} as User);
        }
      } catch (err) {
        setError('Failed to check user status');
        console.error('Error checking user:', err);
      } finally {
        setLoading(false);
      }
    };

    checkUser();
  }, []);

  const sendVerification = async (phoneNumber: string) => {
    try {
      setError(null);
      return await sendPhoneVerification(phoneNumber);
    } catch (err) {
      setError('Failed to send verification code');
      console.error('Error sending verification:', err);
      return false;
    }
  };

  const verifyCode = async (otp: string) => {
    try {
      setError(null);
      const success = await verifyOtp(otp);
      if (success) {
        const currentUser = await getCurrentUser();
        if (currentUser) {
          const userProfile = await getUserProfile(currentUser.uid);
          setUser({...currentUser, ...userProfile} as User);
        }
      }
      return success;
    } catch (err) {
      setError('Failed to verify code');
      console.error('Error verifying code:', err);
      return false;
    }
  };

  const signOut = async () => {
    try {
      setError(null);
      await authSignOut();
      setUser(null);
    } catch (err) {
      setError('Failed to sign out');
      console.error('Error signing out:', err);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        sendVerification,
        verifyCode,
        signOut,
      }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
