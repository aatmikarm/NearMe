// src/services/mock/mockAuth.ts
import { AuthService } from '../types';

// This will simulate local authentication until we can integrate Firebase
export class MockAuthService implements AuthService {
  private mockUser: {uid: string} | null = null;
  
  async sendPhoneVerification(phoneNumber: string): Promise<boolean> {
    console.log('Mock: Sending verification to', phoneNumber);
    return true;
  }
  
  async verifyOtp(otp: string): Promise<boolean> {
    console.log('Mock: Verifying OTP', otp);
    // Simulate successful verification
    this.mockUser = { uid: 'mock-user-' + Date.now() };
    return true;
  }
  
  async getCurrentUser(): Promise<{uid: string} | null> {
    return this.mockUser;
  }
  
  async signOut(): Promise<boolean> {
    this.mockUser = null;
    return true;
  }
}

// Create a singleton instance
export const mockAuthService = new MockAuthService();