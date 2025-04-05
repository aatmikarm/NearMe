// src/contexts/AuthContext.tsx
import React, {createContext, useContext, useState, useEffect, useRef} from 'react';
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
  verificationId: string | null;
  sendVerification: (phoneNumber: string) => Promise<boolean>;
  verifyCode: (otp: string) => Promise<boolean>;
  signOut: () => Promise<void>;
  updateProfile: (profileData: any) => Promise<boolean>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{children: React.ReactNode}> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [verificationId, setVerificationId] = useState<string | null>(null);
  const [phoneNumber, setPhoneNumber] = useState<string | null>(null);

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
      setLoading(true);
      setPhoneNumber(phoneNumber);
      
      // Call the auth service to send verification
      const result = await sendPhoneVerification(phoneNumber);
      
      // In a real implementation, Firebase would return a verificationId
      // For now, we'll simulate it with the service's return value
      if (typeof result === 'object' && result.verificationId) {
        setVerificationId(result.verificationId);
        return true;
      }
      
      return result as boolean;
    } catch (err) {
      setError('Failed to send verification code');
      console.error('Error sending verification:', err);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const verifyCode = async (otp: string) => {
    try {
      setError(null);
      setLoading(true);
      
      if (!verificationId || !phoneNumber) {
        setError('Verification session expired, please try again');
        return false;
      }
      
      // Call the auth service to verify the code
      const success = await verifyOtp(verificationId, otp);
      
      if (success) {
        const currentUser = await getCurrentUser();
        if (currentUser) {
          const userProfile = await getUserProfile(currentUser.uid);
          if (userProfile) {
            setUser({...currentUser, ...userProfile} as User);
          } else {
            // If no profile exists, just set the user with auth info
            setUser(currentUser);
          }
        }
      }
      
      return success;
    } catch (err) {
      setError('Failed to verify code');
      console.error('Error verifying code:', err);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (profileData: any) => {
    try {
      setError(null);
      setLoading(true);
      
      if (!user) {
        setError('No user is authenticated');
        return false;
      }
      
      const success = await createUserProfile(user.uid, profileData);
      
      if (success) {
        // Update the local user state with the new profile data
        setUser(prev => ({...prev!, ...profileData} as User));
      }
      
      return success;
    } catch (err) {
      setError('Failed to update profile');
      console.error('Error updating profile:', err);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const signOut = async () => {
    try {
      setError(null);
      setLoading(true);
      await authSignOut();
      setUser(null);
      setVerificationId(null);
      setPhoneNumber(null);
    } catch (err) {
      setError('Failed to sign out');
      console.error('Error signing out:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        verificationId,
        sendVerification,
        verifyCode,
        signOut,
        updateProfile,
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