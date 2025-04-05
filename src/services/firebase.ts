// src/services/firebase.ts
import {initializeApp} from 'firebase/app';
import {getAuth} from 'firebase/auth';
import {getFirestore} from 'firebase/firestore';

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: 'AIzaSyBwYz4l38FkVsCc0WsNWypCPgPjWaHCugI',
  authDomain: 'nearme2025-ba8d5.firebaseapp.com',
  projectId: 'nearme2025-ba8d5',
  storageBucket: 'nearme2025-ba8d5.firebasestorage.app',
  messagingSenderId: '795317137845',
  appId: '1:795317137845:ios:d5226e8a33139ece7069fc',
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Auth
const auth = getAuth(app);

// Initialize Firestore
const firestore = getFirestore(app);

export {auth, firestore};
