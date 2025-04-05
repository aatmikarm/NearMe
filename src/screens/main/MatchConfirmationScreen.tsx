// src/screens/main/MatchConfirmationScreen.tsx
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Animated,
  ActivityIndicator
} from 'react-native';
import { useMatch } from '../../contexts/MatchContext';
import { useUser } from '../../contexts/UserContext';

// Mock user data - this would come from the API in a real app
const getUserData = (userId) => ({
  id: userId,
  name: userId === 'user1' ? 'Priya' : userId === 'user2' ? 'Rahul' : 'Aisha',
  photo: `https://randomuser.me/api/portraits/${userId === 'user1' || userId === 'user3' ? 'women' : 'men'}/${userId === 'user1' ? 12 : userId === 'user2' ? 32 : 44}.jpg`,
});

type MatchConfirmationProps = {
  route: {
    params: {
      matchId: string;
      userId: string;
    };
  };
  navigation: any;
};

const MatchConfirmationScreen = ({ route, navigation }: MatchConfirmationProps) => {
  const { matchId, userId } = route.params;
  
  const { matches, shareInstagram } = useMatch();
  const { userProfile } = useUser();
  
  const [animation] = useState(new Animated.Value(0));
  const [isInstagramShared, setIsInstagramShared] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [matchedUser, setMatchedUser] = useState(null);
  
  // Find the match
  const match = matches.find(m => m.id === matchId);
  
  useEffect(() => {
    // Load matched user data
    setMatchedUser(getUserData(userId));
    
    // Animate in
    Animated.timing(animation, {
      toValue: 1,
      duration: 800,
      useNativeDriver: true,
    }).start();
  }, [userId]);
  
  // Derived animation values
  const opacity = animation;
  const scale = animation.interpolate({
    inputRange: [0, 0.5, 1],
    outputRange: [0.5, 1.2, 1]
  });
  
  const handleShareInstagram = async () => {
    if (!match) return;
    
    setIsLoading(true);
    try {
      const success = await shareInstagram(matchId, true);
      if (success) {
        setIsInstagramShared(true);
      }
    } catch (error) {
      console.error('Error sharing Instagram:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleDontShare = async () => {
    if (!match) return;
    
    setIsLoading(true);
    try {
      await shareInstagram(matchId, false);
      setIsInstagramShared(false);
    } catch (error) {
      console.error('Error updating Instagram sharing:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleMessage = () => {
    navigation.navigate('Chat', { 
      matchId: matchId, 
      userName: matchedUser?.name || 'Match',
      userPhoto: matchedUser?.photo
    });
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#FF6B6B" />
      
      <View style={styles.content}>
        <Animated.View 
          style={[
            styles.animatedContent,
            { opacity, transform: [{ scale }] }
          ]}
        >
          <Text style={styles.title}>It's a Match!</Text>
          {matchedUser && (
            <Text style={styles.subtitle}>
              You and {matchedUser.name} have connected
            </Text>
          )}
          
          <View style={styles.photosContainer}>
            {userProfile && (
              <Image 
                source={{ uri: userProfile.photos?.[0]?.url || 'https://randomuser.me/api/portraits/men/46.jpg' }} 
                style={[styles.userPhoto, styles.currentUserPhoto]} 
              />
            )}
            {matchedUser && (
              <Image 
                source={{ uri: matchedUser.photo }} 
                style={[styles.userPhoto, styles.matchedUserPhoto]} 
              />
            )}
          </View>
          
          <View style={styles.instagramQuestion}>
            <Text style={styles.instagramTitle}>Share Instagram?</Text>
            <Text style={styles.instagramDescription}>
              Would you like to share your Instagram with {matchedUser?.name || 'your match'}?
            </Text>
            
            <View style={styles.instagramButtons}>
              <TouchableOpacity 
                style={[
                  styles.instagramButton,
                  isInstagramShared && styles.instagramButtonActive,
                  isLoading && styles.instagramButtonDisabled
                ]}
                onPress={handleShareInstagram}
                disabled={isLoading}
              >
                <Text style={[
                  styles.instagramButtonText,
                  isInstagramShared && styles.instagramButtonTextActive
                ]}>Yes</Text>
              </TouchableOpacity>
              
              <TouchableOpacity 
                style={[
                  styles.instagramButton,
                  !isInstagramShared && styles.instagramButtonActive,
                  isLoading && styles.instagramButtonDisabled
                ]}
                onPress={handleDontShare}
                disabled={isLoading}
              >
                <Text style={[
                  styles.instagramButtonText,
                  !isInstagramShared && styles.instagramButtonTextActive
                ]}>No</Text>
              </TouchableOpacity>
            </View>
            
            {isLoading && (
              <ActivityIndicator 
                style={styles.activityIndicator} 
                color="#FF6B6B" 
              />
            )}
          </View>
          
          <TouchableOpacity 
            style={styles.messageButton}
            onPress={handleMessage}
          >
            <Text style={styles.messageButtonText}>Message</Text>
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={styles.closeButton}
            onPress={() => navigation.navigate('Nearby')}
          >
            <Text style={styles.closeButtonText}>Close</Text>
          </TouchableOpacity>
        </Animated.View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FF6B6B',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  animatedContent: {
    width: '100%',
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
    marginBottom: 32,
    textAlign: 'center',
  },
  photosContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginBottom: 32,
  },
  userPhoto: {
    width: 120,
    height: 120,
    borderRadius: 60,
    borderWidth: 3,
    borderColor: '#FFFFFF',
  },
  currentUserPhoto: {
    marginRight: -20,
    zIndex: 1,
  },
  matchedUserPhoto: {
    marginLeft: -20,
  },
  instagramQuestion: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 24,
    width: '100%',
    marginBottom: 24,
    alignItems: 'center',
  },
  instagramTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  instagramDescription: {
    fontSize: 14,
    color: '#666666',
    textAlign: 'center',
    marginBottom: 16,
  },
  instagramButtons: {
    flexDirection: 'row',
    width: '100%',
  },
  instagramButton: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#DDDDDD',
    marginHorizontal: 8,
    borderRadius: 8,
  },
  instagramButtonActive: {
    backgroundColor: '#FF6B6B',
    borderColor: '#FF6B6B',
  },
  instagramButtonDisabled: {
    opacity: 0.5,
  },
  instagramButtonText: {
    fontSize: 16,
    color: '#666666',
  },
  instagramButtonTextActive: {
    color: '#FFFFFF',
  },
  activityIndicator: {
    marginTop: 16,
  },
  messageButton: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 30,
    marginBottom: 16,
    width: '100%',
    alignItems: 'center',
  },
  messageButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FF6B6B',
  },
  closeButton: {
    paddingVertical: 8,
  },
  closeButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
  },
});

export default MatchConfirmationScreen;