// src/services/mock/mockMatch.ts
import { MatchService, ProximityEvent, Match } from '../types';

// Mock data storage
const proximityEvents: ProximityEvent[] = [];
const matches: Match[] = [];

// src/services/mock/mockMatch.ts (update to the MOCK_USERS array)

// Mock nearby users - expanded with more variety
const MOCK_USERS = [
  {
    id: 'user1',
    name: 'Priya',
    age: 24,
    photo: 'https://randomuser.me/api/portraits/women/12.jpg',
  },
  {
    id: 'user2',
    name: 'Rahul',
    age: 28,
    photo: 'https://randomuser.me/api/portraits/men/32.jpg',
  },
  {
    id: 'user3',
    name: 'Aisha',
    age: 23,
    photo: 'https://randomuser.me/api/portraits/women/44.jpg',
  },
  {
    id: 'user4',
    name: 'Vikram',
    age: 26,
    photo: 'https://randomuser.me/api/portraits/men/22.jpg',
  },
  {
    id: 'user5',
    name: 'Maya',
    age: 25,
    photo: 'https://randomuser.me/api/portraits/women/24.jpg',
  },
  {
    id: 'user6',
    name: 'Arjun',
    age: 29,
    photo: 'https://randomuser.me/api/portraits/men/45.jpg',
  },
  {
    id: 'user7',
    name: 'Kavya',
    age: 22,
    photo: 'https://randomuser.me/api/portraits/women/35.jpg',
  },
  {
    id: 'user8',
    name: 'Rohan',
    age: 27,
    photo: 'https://randomuser.me/api/portraits/men/53.jpg',
  }
];

export class MockMatchService implements MatchService {
  async findNearbyUsers(location: {lat: number, lng: number}, radiusInM = 500): Promise<any[]> {
    // Simulate finding nearby users with random distances
    return MOCK_USERS.map(user => ({
      ...user,
      distance: Math.floor(Math.random() * radiusInM)
    }));
  }
  
  async getProximityEvents(userId: string): Promise<ProximityEvent[]> {
    return proximityEvents.filter(event => event.users.includes(userId));
  }
  
  async getMatches(userId: string): Promise<Match[]> {
    return matches.filter(match => match.users.includes(userId));
  }
  
  async createMatch(proximityEventId: string): Promise<string | null> {
    const event = proximityEvents.find(e => e.id === proximityEventId);
    if (!event) return null;
    
    const matchId = 'match-' + Date.now();
    matches.push({
      id: matchId,
      users: event.users,
      matchedAt: new Date(),
      proximityEventId,
      status: 'active',
      instagramShared: {
        [event.users[0]]: false,
        [event.users[1]]: false
      }
    });
    
    // Update event status
    event.status = 'matched';
    
    return matchId;
  }
  
  async updateInstagramSharing(matchId: string, userId: string, shared: boolean): Promise<boolean> {
    const match = matches.find(m => m.id === matchId);
    if (!match) return false;
    
    match.instagramShared[userId] = shared;
    return true;
  }
  
  // This is a special method for simulating a proximity event for testing
  simulateProximityEvent(userId: string, otherUserId: string, distance: number): string {
    const eventId = 'proximity-' + Date.now();
    const event: ProximityEvent = {
      id: eventId,
      users: [userId, otherUserId],
      distance,
      startTime: new Date(),
      status: 'active',
      location: {
        geohash: 'mock-geohash'
      }
    };
    
    proximityEvents.push(event);
    return eventId;
  }
}

export const mockMatchService = new MockMatchService();