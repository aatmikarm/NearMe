// src/screens/main/ProfileScreen.tsx
import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar
} from 'react-native';
import { useUser } from '../../contexts/UserContext';

type ProfileScreenProps = {
  navigation: any;
};

const ProfileScreen = ({ navigation }: ProfileScreenProps) => {
  const { userProfile, isProfileLoading } = useUser();
  
  // Mock data until we have a complete user profile
  const mockProfile = {
    displayName: userProfile?.displayName || 'User',
    age: userProfile?.age || 25,
    photos: userProfile?.photos || [{ url: 'https://randomuser.me/api/portraits/men/46.jpg', isPrimary: true }],
    bio: userProfile?.bio || 'I enjoy meeting new people and exploring interesting places.',
    interests: ['Travel', 'Photography', 'Food', 'Music'],
    instagramConnected: userProfile?.instagramConnected || false,
    instagramId: userProfile?.instagramId
  };
  
  const handleEditProfile = () => {
    // Navigate to edit profile screen (to be implemented)
    navigation.navigate('Settings');
  };
  
  const handleSettings = () => {
    navigation.navigate('Settings');
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      <View style={styles.header}>
        <Text style={styles.headerTitle}>My Profile</Text>
        <TouchableOpacity onPress={handleEditProfile}>
          <Text style={styles.editButton}>Edit</Text>
        </TouchableOpacity>
      </View>
      
      <ScrollView style={styles.content}>
        <View style={styles.profileHeader}>
          <Image 
            source={{ uri: mockProfile.photos[0].url }} 
            style={styles.profilePhoto} 
          />
          <Text style={styles.profileName}>{mockProfile.displayName}, {mockProfile.age}</Text>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>About</Text>
          <Text style={styles.bioText}>{mockProfile.bio}</Text>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Interests</Text>
          <View style={styles.interestsContainer}>
            {mockProfile.interests.map((interest, index) => (
              <View key={index} style={styles.interestTag}>
                <Text style={styles.interestText}>{interest}</Text>
              </View>
            ))}
          </View>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Instagram</Text>
          <View style={styles.instagramContainer}>
            {mockProfile.instagramConnected ? (
              <Text style={styles.instagramText}>@{mockProfile.instagramId}</Text>
            ) : (
              <TouchableOpacity 
                style={styles.connectButton}
                onPress={() => navigation.navigate('InstagramConnection')}
              >
                <Text style={styles.connectButtonText}>Connect Instagram</Text>
              </TouchableOpacity>
            )}
          </View>
        </View>
        
        <TouchableOpacity 
          style={styles.settingsButton}
          onPress={handleSettings}
        >
          <Text style={styles.settingsButtonText}>Settings</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  editButton: {
    color: '#FF6B6B',
    fontSize: 16,
  },
  content: {
    flex: 1,
  },
  profileHeader: {
    alignItems: 'center',
    paddingVertical: 24,
  },
  profilePhoto: {
    width: 120,
    height: 120,
    borderRadius: 60,
    marginBottom: 16,
  },
  profileName: {
    fontSize: 22,
    fontWeight: 'bold',
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
  instagramContainer: {
    marginVertical: 8,
  },
  instagramText: {
    fontSize: 16,
    color: '#333333',
  },
  connectButton: {
    backgroundColor: '#F5F5F5',
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  connectButtonText: {
    color: '#666666',
    fontSize: 14,
  },
  settingsButton: {
    margin: 16,
    backgroundColor: '#F5F5F5',
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
  },
  settingsButtonText: {
    fontSize: 16,
    color: '#333333',
  },
});

export default ProfileScreen;