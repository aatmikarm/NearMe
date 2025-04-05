// src/services/auth.ts
import {auth, firestore} from './firebase';
import {
  getAuth,
  signInWithCredential,
  PhoneAuthProvider,
  PhoneMultiFactorGenerator,
  PhoneAuthCredential,
  onAuthStateChanged,
  signOut as firebaseSignOut,
  User as FirebaseUser,
  AuthError,
  ConfirmationResult,
  Auth,
} from 'firebase/auth';
import {collection, doc, setDoc, getDoc} from 'firebase/firestore';

// Store verification ID
let verificationId: string | null = null;

// Test phone numbers (add your test numbers from Firebase Console)
const TEST_PHONE_NUMBERS = [
  '+919876543210', // Example Indian test number
  '+919876543211',
  '+919876543212',
];

// Helper function to check if a phone number is a test number
const isTestPhoneNumber = (phoneNumber: string): boolean => {
  return TEST_PHONE_NUMBERS.includes(phoneNumber);
};

// Send phone verification code
export const sendPhoneVerification = async (
  phoneNumber: string,
): Promise<boolean> => {
  console.log('=== Starting Phone Verification ===');
  console.log('Input phone number:', phoneNumber);

  try {
    // Format the phone number for E.164 format
    let formattedNumber = phoneNumber;
    if (!phoneNumber.startsWith('+')) {
      if (phoneNumber.startsWith('91')) {
        formattedNumber = '+' + phoneNumber;
      } else if (phoneNumber.length === 10) {
        formattedNumber = '+91' + phoneNumber;
      }
    }

    console.log('Formatted phone number:', formattedNumber);
    console.log('Firebase Auth state:', {
      isInitialized: auth ? true : false,
      currentUser: auth?.currentUser ? 'exists' : 'none',
    });

    if (!auth) {
      console.error('Firebase Auth is not initialized');
      throw new Error('Authentication service is not initialized');
    }

    // Check if the number is a test number
    const isTestNumber = TEST_PHONE_NUMBERS.includes(formattedNumber);
    console.log('Is test number:', isTestNumber);

    try {
      console.log('Calling verifyPhoneNumber...');
      const provider = new PhoneAuthProvider(auth);
      verificationId = await provider.verifyPhoneNumber(formattedNumber);
      console.log('Verification ID received:', verificationId);

      if (!verificationId) {
        console.error('No verification ID received');
        throw new Error('Failed to get verification ID');
      }

      console.log('Verification process completed successfully');
      return true;
    } catch (verifyError) {
      console.error('=== Phone Verification Error ===');
      console.error('Error details:', {
        error: verifyError,
        name: verifyError instanceof Error ? verifyError.name : 'Unknown',
        message:
          verifyError instanceof Error ? verifyError.message : 'Unknown error',
        stack:
          verifyError instanceof Error ? verifyError.stack : 'No stack trace',
        phoneNumber: formattedNumber,
        timestamp: new Date().toISOString(),
      });

      if (verifyError instanceof Error) {
        if (verifyError.message.includes('auth/invalid-phone-number')) {
          throw new Error(
            'Invalid phone number format. Please enter a valid Indian phone number',
          );
        } else if (verifyError.message.includes('auth/quota-exceeded')) {
          throw new Error('Too many attempts. Please try again later');
        } else if (verifyError.message.includes('auth/operation-not-allowed')) {
          throw new Error(
            'Phone authentication is not enabled. Please contact support',
          );
        } else if (
          verifyError.message.includes('auth/network-request-failed')
        ) {
          throw new Error(
            'Network error. Please check your internet connection',
          );
        }
      }
      throw verifyError;
    }
  } catch (error) {
    console.error('=== Final Error Handling ===');
    console.error('Error caught in outer try-catch:', {
      error,
      name: error instanceof Error ? error.name : 'Unknown',
      message: error instanceof Error ? error.message : 'Unknown error',
      stack: error instanceof Error ? error.stack : 'No stack trace',
    });
    throw error;
  }
};

// Verify OTP
export const verifyOtp = async (otp: string): Promise<boolean> => {
  try {
    console.log('=== Starting OTP Verification ===');
    console.log('Input OTP:', otp);
    console.log('Stored verificationId:', verificationId);

    if (!verificationId) {
      console.error('No verification ID found');
      throw new Error(
        'No verification ID found. Please request a new verification code',
      );
    }

    console.log('Creating credential with verificationId and OTP...');
    const credential = PhoneAuthProvider.credential(verificationId, otp);
    console.log('Credential created successfully');

    console.log('Attempting to sign in with credential...');
    await signInWithCredential(auth, credential);
    console.log('Sign in successful');
    console.log('=== OTP Verification Successful ===');
    return true;
  } catch (error) {
    const authError = error as AuthError;
    console.error('=== OTP Verification Failed ===');
    console.error('Error details:', {
      code: authError.code,
      message: authError.message,
      timestamp: new Date().toISOString(),
      stack: authError.stack,
    });

    switch (authError.code) {
      case 'auth/invalid-verification-code':
        throw new Error('Invalid verification code. Please try again');
      case 'auth/code-expired':
        throw new Error(
          'Verification code has expired. Please request a new code',
        );
      default:
        throw new Error(`Failed to verify code: ${authError.message}`);
    }
  }
};

// Create or update user profile
export const createUserProfile = async (
  userId: string,
  profileData: any,
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

// Get current user
export const getCurrentUser = async () => {
  return new Promise<FirebaseUser | null>(resolve => {
    const unsubscribe = onAuthStateChanged(auth, user => {
      unsubscribe();
      resolve(user);
    });
  });
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
