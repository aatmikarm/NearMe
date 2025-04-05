// src/services/matchingService.ts

// Types
type User = {
    id: string;
    name: string;
    age: number;
    gender: string;
    interests: string[];
    // other properties
  };
  
  type Match = {
    id: string;
    users: string[];
    matchedAt: Date;
    isActive: boolean;
  };
  
  // Check if users have mutual interests
  export const calculateInterestCompatibility = (user1: User, user2: User): number => {
    if (!user1.interests || !user2.interests) return 0;
    
    const user1Interests = new Set(user1.interests);
    const matchingInterests = user2.interests.filter(interest => 
      user1Interests.has(interest)
    );
    
    return matchingInterests.length;
  };
  
  // Create a match between two users
  export const createMatch = (user1Id: string, user2Id: string): Match => {
    return {
      id: `match_${Date.now()}`,
      users: [user1Id, user2Id],
      matchedAt: new Date(),
      isActive: true
    };
  };
  
  // Get mock matches for a user
  export const getMockMatchesForUser = (
    userId: string, 
    allUsers: User[]
  ): Match[] => {
    // Generate some mock matches
    return allUsers
      .filter(user => user.id !== userId)
      .slice(0, 3)
      .map(user => createMatch(userId, user.id));
  };