// src/services/auth.ts
import { auth, firestore } from './firebase';
import { 
  PhoneAuthProvider, 
  signInWithCredential,
  signOut as firebaseSignOut
} from 'firebase/auth';
import { collection, doc, setDoc, getDoc } from 'firebase/firestore';

// Store verification ID
let verificationId: string | null = null;

// Send phone verification code
export const sendPhoneVerification = async (phoneNumber: string): Promise<boolean> => {
  try {
    // Note: In a real app, you would use Firebase SDK for phone auth in React Native
    // This is a simplified version
    // You'll need react-native-firebase package for actual implementation
    
    // Simulated success for now
    console.log('Sending verification to', phoneNumber);
    return true;
  } catch (error) {
    console.error('Error sending verification:', error);
    return false;
  }
};

// Verify OTP
export const verifyOtp = async (otp: string): Promise<boolean> => {
  try {
    // Note: In a real app with react-native-firebase, you would:
    // 1. Get verification ID from previous step
    // 2. Create credential with PhoneAuthProvider
    // 3. Sign in with that credential
    
    // Simulated success for now
    console.log('Verifying OTP:', otp);
    return true;
  } catch (error) {
    console.error('Error verifying OTP:', error);
    return false;
  }
};

// Create or update user profile
export const createUserProfile = async (
  userId: string, 
  profileData: any
): Promise<boolean> => {
  try {
    await setDoc(doc(firestore, 'users', userId), {
      ...profileData,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
    return true;
  } catch (error) {
    console.error('Error creating user profile:', error);
    return false;
  }
};

// Get user profile
export const getUserProfile = async (userId: string) => {
  try {
    const userDoc = await getDoc(doc(firestore, 'users', userId));
    if (userDoc.exists()) {
      return userDoc.data();
    }
    return null;
  } catch (error) {
    console.error('Error getting user profile:', error);
    return null;
  }
};

// Sign out
export const signOut = async (): Promise<boolean> => {
  try {
    await firebaseSignOut(auth);
    return true;
  } catch (error) {
    console.error('Error signing out:', error);
    return false;
  }
};