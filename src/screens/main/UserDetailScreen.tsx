// src/screens/main/UserDetailScreen.tsx
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Dimensions,
  ActivityIndicator,
  Alert
} from 'react-native';
import { useMatch } from '../../contexts/MatchContext';
import { useUser } from '../../contexts/UserContext';

const { width } = Dimensions.get('window');

// Mock user data - in real app this would come from API
const getUserData = (userId) => ({
  id: userId,
  name: userId === 'user1' ? 'Priya' : userId === 'user2' ? 'Rahul' : 'Aisha',
  age: userId === 'user1' ? 24 : userId === 'user2' ? 28 : 23,
  photos: [
    `https://randomuser.me/api/portraits/${userId === 'user1' || userId === 'user3' ? 'women' : 'men'}/${userId === 'user1' ? 12 : userId === 'user2' ? 32 : 44}.jpg`,
    `https://randomuser.me/api/portraits/${userId === 'user1' || userId === 'user3' ? 'women' : 'men'}/${userId === 'user1' ? 15 : userId === 'user2' ? 33 : 45}.jpg`,
    `https://randomuser.me/api/portraits/${userId === 'user1' || userId === 'user3' ? 'women' : 'men'}/${userId === 'user1' ? 19 : userId === 'user2' ? 35 : 46}.jpg`
  ],
  bio: userId === 'user1' 
    ? 'I love photography, travel, and trying new foods. Always looking for the next adventure!' 
    : userId === 'user2' 
    ? 'Tech enthusiast and coffee addict. Enjoy hiking on weekends and exploring new places.'
    : 'Art lover and bookworm. I enjoy quiet cafes and meaningful conversations.',
  interests: userId === 'user1' 
    ? ['Photography', 'Travel', 'Food', 'Reading'] 
    : userId === 'user2'
    ? ['Technology', 'Coffee', 'Hiking', 'Music']
    : ['Art', 'Books', 'Yoga', 'Cooking'],
  instagram: userId === 'user1' ? 'priya.travels' : userId === 'user2' ? 'rahul.tech' : 'aisha.reads'
});

type UserDetailScreenProps = {
  route: any;
  navigation: any;
};

const UserDetailScreen = ({ route, navigation }: UserDetailScreenProps) => {
  const { userId } = route.params;
  const [user, setUser] = useState(null);
  const [currentPhotoIndex, setCurrentPhotoIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  
  const { proximityEvents, createMatch } = useMatch();
  const { userProfile } = useUser();
  
  // Find active proximity event for this user
  const activeProximityEvent = proximityEvents.find(
    event => event.users.includes(userId) && event.status === 'active'
  );
  
  // Load user data
  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      setUser(getUserData(userId));
      setIsLoading(false);
    }, 1000);
  }, [userId]);
  
  const handleConnect = async () => {
    if (!activeProximityEvent) {
      Alert.alert(
        "Connection not available",
        "You need to be near this person to connect.",
        [{ text: "OK" }]
      );
      return;
    }
    
    const matchId = await createMatch(activeProximityEvent.id);
    
    if (matchId) {
      navigation.navigate('MatchConfirmation', { matchId, userId });
    } else {
      Alert.alert(
        "Connection failed",
        "Something went wrong. Please try again later.",
        [{ text: "OK" }]
      );
    }
  };
  
  const handleNextPhoto = () => {
    if (user && currentPhotoIndex < user.photos.length - 1) {
      setCurrentPhotoIndex(currentPhotoIndex + 1);
    }
  };
  
  const handlePrevPhoto = () => {
    if (currentPhotoIndex > 0) {
      setCurrentPhotoIndex(currentPhotoIndex - 1);
    }
  };
  
  if (isLoading) {
    return (
      <SafeAreaView style={styles.container}>
        <StatusBar barStyle="light-content" backgroundColor="#000000" />
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#FF6B6B" />
          <Text style={styles.loadingText}>Loading profile...</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#000000" />
      
      <View style={styles.photoContainer}>
        {user && (
          <Image source={{ uri: user.photos[currentPhotoIndex] }} style={styles.photo} />
        )}
        
        <TouchableOpacity 
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        
        {user && (
          <View style={styles.photoNavigation}>
            {user.photos.map((_, index) => (
              <View 
                key={index} 
                style={[
                  styles.photoIndicator,
                  index === currentPhotoIndex && styles.photoIndicatorActive
                ]} 
              />
            ))}
          </View>
        )}
        
        <TouchableOpacity 
          style={[styles.photoArrow, styles.photoArrowLeft]} 
          onPress={handlePrevPhoto}
          disabled={currentPhotoIndex === 0}
        >
          <Text style={[styles.photoArrowText, currentPhotoIndex === 0 && styles.photoArrowDisabled]}>{'<'}</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.photoArrow, styles.photoArrowRight]} 
          onPress={handleNextPhoto}
          disabled={!user || currentPhotoIndex === user.photos.length - 1}
        >
          <Text style={[
            styles.photoArrowText, 
            (!user || currentPhotoIndex === user.photos.length - 1) && styles.photoArrowDisabled
          ]}>{'>'}</Text>
        </TouchableOpacity>
      </View>
      
      {user && (
        <ScrollView style={styles.contentContainer}>
          <View style={styles.userInfoHeader}>
            <View>
              <Text style={styles.userName}>{user.name}, {user.age}</Text>
              {activeProximityEvent && (
                <Text style={styles.userDistance}>{activeProximityEvent.distance}m away</Text>
              )}
            </View>
            
            <TouchableOpacity 
              style={[
                styles.connectButton,
                !activeProximityEvent && styles.connectButtonDisabled
              ]}
              onPress={handleConnect}
              disabled={!activeProximityEvent}
            >
              <Text style={styles.connectButtonText}>Connect</Text>
            </TouchableOpacity>
          </View>
          
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>About</Text>
            <Text style={styles.bioText}>{user.bio}</Text>
          </View>
          
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Interests</Text>
            <View style={styles.interestsContainer}>
              {user.interests.map((interest, index) => (
                <View key={index} style={styles.interestTag}>
                  <Text style={styles.interestText}>{interest}</Text>
                </View>
              ))}
            </View>
          </View>
          
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Social</Text>
            <View style={styles.socialItem}>
              <Text style={styles.socialText}>
                Instagram will be revealed after you match
              </Text>
            </View>
          </View>
        </ScrollView>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 16,
    color: '#666666',
  },
  photoContainer: {
    height: width * 1.3,
    width: '100%',
    position: 'relative',
    backgroundColor: '#f0f0f0',
  },
  photo: {
    width: '100%',
    height: '100%',
  },
  backButton: {
    position: 'absolute',
    top: 20,
    left: 20,
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.4)',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 10,
  },
  backButtonText: {
    color: '#FFFFFF',
    fontSize: 24,
  },
  photoNavigation: {
    position: 'absolute',
    bottom: 20,
    left: 0,
    right: 0,
    flexDirection: 'row',
    justifyContent: 'center',
    zIndex: 10,
  },
  photoIndicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: 'rgba(255, 255, 255, 0.5)',
    marginHorizontal: 4,
  },
  photoIndicatorActive: {
    backgroundColor: '#FFFFFF',
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  photoArrow: {
    position: 'absolute',
    top: '50%',
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.4)',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: -20,
    zIndex: 10,
  },
  photoArrowLeft: {
    left: 10,
  },
  photoArrowRight: {
    right: 10,
  },
  photoArrowText: {
    color: '#FFFFFF',
    fontSize: 24,
  },
  photoArrowDisabled: {
    color: 'rgba(255, 255, 255, 0.3)',
  },
  contentContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    marginTop: -20,
    paddingTop: 20,
  },
  userInfoHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  userName: {
    fontSize: 22,
    fontWeight: 'bold',
  },
  userDistance: {
    fontSize: 16,
    color: '#666666',
    marginTop: 4,
  },
  connectButton: {
    backgroundColor: '#FF6B6B',
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 20,
  },
  connectButtonDisabled: {
    backgroundColor: '#FFADAD',
  },
  connectButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  section: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
  },
  bioText: {
    fontSize: 16,
    lineHeight: 24,
    color: '#333333',
  },
  interestsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  interestTag: {
    backgroundColor: '#F5F5F5',
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 16,
    marginRight: 8,
    marginBottom: 8,
  },
  interestText: {
    fontSize: 14,
    color: '#333333',
  },
  socialItem: {
    padding: 8,
  },
  socialText: {
    fontSize: 16,
    color: '#666666',
    fontStyle: 'italic',
  },
});

export default UserDetailScreen;