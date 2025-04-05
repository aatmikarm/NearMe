// src/services/firebase.ts
import { initializeApp } from 'firebase/app';
import { initializeAuth, getReactNativePersistence } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: 'AIzaSyBwYz4l38FkVsCc0WsNWypCPgPjWaHCugI',
  authDomain: 'nearme2025-ba8d5.firebasestorage.app',
  projectId: 'nearme2025-ba8d5',
  storageBucket: 'nearme2025-ba8d5.firebasestorage.app',
  messagingSenderId: '795317137845',
  appId: '1:795317137845:ios:d3b32658576acfba7069fc',
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Firebase Auth with AsyncStorage persistence
const auth = initializeAuth(app, {
  persistence: getReactNativePersistence(AsyncStorage)
});

// Initialize other Firebase services
const firestore = getFirestore(app);
const storage = getStorage(app);

export { auth, firestore, storage };
export default app;
