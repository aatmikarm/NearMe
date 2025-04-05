// src/services/location.ts
import { firestore } from './firebase';
import { doc, updateDoc, collection, query, where, getDocs } from 'firebase/firestore';
import * as geohash from 'geofire-common';

// Update user location
export const updateUserLocation = async (
  userId: string, 
  latitude: number, 
  longitude: number
): Promise<boolean> => {
  try {
    // Generate a geohash for the location
    const locationGeohash = geohash.geohashForLocation([latitude, longitude]);
    
    // Update user's location in Firestore
    await updateDoc(doc(firestore, 'users', userId), {
      'location.geohash': locationGeohash,
      'location.lat': latitude,
      'location.lng': longitude,
      'location.updatedAt': new Date()
    });
    
    return true;
  } catch (error) {
    console.error('Error updating location:', error);
    return false;
  }
};

// Find nearby users
export const findNearbyUsers = async (
  latitude: number, 
  longitude: number, 
  radiusInM = 500
): Promise<any[]> => {
  try {
    // Find locations within the radius
    const center = [latitude, longitude];
    const bounds = geohash.geohashQueryBounds(center, radiusInM);
    const promises = [];
    
    for (const b of bounds) {
      const q = query(
        collection(firestore, 'users'),
        where('location.geohash', '>=', b[0]),
        where('location.geohash', '<=', b[1])
      );
      promises.push(getDocs(q));
    }
    
    // Collect all the query results
    const snapshots = await Promise.all(promises);
    
    // Filter out users that are not within the radius
    const matchingUsers = [];
    
    for (const snap of snapshots) {
      for (const doc of snap.docs) {
        const userLat = doc.data().location.lat;
        const userLng = doc.data().location.lng;
        
        // Filter without calculating distance first
        if (!userLat || !userLng) continue;
        
        // Calculate actual distance
        const distanceInM = geohash.distanceBetween([userLat, userLng], center);
        
        // Add user if within the radius
        if (distanceInM <= radiusInM) {
          matchingUsers.push({
            id: doc.id,
            distance: Math.round(distanceInM),
            ...doc.data()
          });
        }
      }
    }
    
    return matchingUsers;
  } catch (error) {
    console.error('Error finding nearby users:', error);
    return [];
  }
};