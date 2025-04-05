// src/services/firebaseTest.ts
import { firestore } from './firebase';
import { collection, getDocs } from 'firebase/firestore';

export const testFirebaseConnection = async () => {
  try {
    // Create a test collection reference
    const testCollection = collection(firestore, 'test');
    
    // Try to get documents (this should work even if collection is empty)
    const snapshot = await getDocs(testCollection);
    
    console.log('Firebase connection successful!');
    console.log(`Retrieved ${snapshot.size} documents`);
    
    return true;
  } catch (error) {
    console.error('Firebase connection failed:', error);
    return false;
  }
};