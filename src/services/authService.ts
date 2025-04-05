// src/services/authService.ts
import { auth, firestore } from './firebase';
import {
  PhoneAuthProvider,
  signInWithCredential,
  RecaptchaVerifier,
  signOut as firebaseSignOut
} from 'firebase/auth';
import { doc, setDoc, getDoc, updateDoc } from 'firebase/firestore';

// For storing verification state
let verificationId: string | null = null;
let confirmationResult: any = null;

// Send phone verification code (for android/ios)
export const sendPhoneVerification = async (phoneNumber: string) => {
  try {
    // For testing/development, we'll just simulate the process
    // In a real implementation, you'd use FirebaseAuth.instance.verifyPhoneNumber
    console.log('Sending verification to', phoneNumber);
    
    // Simulate a successful verification
    // We're skipping the actual Firebase phone auth integration for now
    // as it requires additional setup with native code
    verificationId = 'mock-verification-id';
    
    return true;
  } catch (error) {
    console.error('Error sending verification:', error);
    return false;
  }
};

// Verify OTP code
export const verifyOtp = async (otp: string) => {
  try {
    // Simulate OTP verification
    // In a real app, you would verify with Firebase
    console.log('Verifying OTP:', otp);
    
    // Pretend the code is valid if it's "123456"
    if (otp === '123456') {
      return true;
    }
    
    return false;
  } catch (error) {
    console.error('Error verifying OTP:', error);
    return false;
  }
};

// Create user profile
export const createUserProfile = async (userId: string, profileData: any) => {
  try {
    // Store user profile in Firestore
    await setDoc(doc(firestore, 'users', userId), {
      ...profileData,
      createdAt: new Date(),
      updatedAt: new Date()
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

// Update user profile
export const updateUserProfile = async (userId: string, profileData: any) => {
  try {
    await updateDoc(doc(firestore, 'users', userId), {
      ...profileData,
      updatedAt: new Date()
    });
    
    return true;
  } catch (error) {
    console.error('Error updating user profile:', error);
    return false;
  }
};

// Sign out
export const signOut = async () => {
  try {
    await firebaseSignOut(auth);
    return true;
  } catch (error) {
    console.error('Error signing out:', error);
    return false;
  }
};