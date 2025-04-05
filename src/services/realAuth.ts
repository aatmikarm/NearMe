// src/services/realAuth.ts
import {
  sendPhoneVerification as sendPhoneVerificationImpl,
  verifyOtp as verifyOtpImpl,
  createUserProfile as createUserProfileImpl,
  getUserProfile as getUserProfileImpl,
  getCurrentUser as getCurrentUserImpl,
  signOut as signOutImpl,
} from './auth';
import {AuthService} from './types';

// Real implementation of the AuthService interface
export const realAuthService: AuthService = {
  sendPhoneVerification: sendPhoneVerificationImpl,
  verifyOtp: verifyOtpImpl,
  createUserProfile: createUserProfileImpl,
  getUserProfile: getUserProfileImpl,
  getCurrentUser: getCurrentUserImpl,
  signOut: signOutImpl,
};
