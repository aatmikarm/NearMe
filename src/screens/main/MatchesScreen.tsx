// src/screens/main/MatchesScreen.tsx
import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Image,
  SafeAreaView,
  StatusBar
} from 'react-native';

// Mock data for matches
const MOCK_MATCHES = [
  {
    id: '1',
    name: 'Priya',
    photo: 'https://randomuser.me/api/portraits/women/12.jpg',
    lastSeen: 'Just now',
    hasNewActivity: true,
  },
  {
    id: '2',
    name: 'Rahul',
    photo: 'https://randomuser.me/api/portraits/men/32.jpg',
    lastSeen: '2 hours ago',
    hasNewActivity: false,
  },
  {
    id: '3',
    name: 'Aisha',
    photo: 'https://randomuser.me/api/portraits/women/44.jpg',
    lastSeen: 'Yesterday',
    hasNewActivity: true,
  }
];

// Mock data for new matches (recent matches)
const MOCK_NEW_MATCHES = [
  {
    id: '4',
    name: 'Vikram',
    photo: 'https://randomuser.me/api/portraits/men/22.jpg',
  },
  {
    id: '5',
    name: 'Maya',
    photo: 'https://randomuser.me/api/portraits/women/24.jpg',
  }
];

type MatchesScreenProps = {
  navigation: any;
};

const MatchesScreen = ({ navigation }: MatchesScreenProps) => {
  const [matches, setMatches] = useState(MOCK_MATCHES);
  const [newMatches, setNewMatches] = useState(MOCK_NEW_MATCHES);

  // Render new match item
  const renderNewMatchItem = ({ item }) => (
    <TouchableOpacity 
      style={styles.newMatchItem}
      onPress={() => navigation.navigate('Chat', { matchId: item.id })}
    >
      <Image source={{ uri: item.photo }} style={styles.newMatchPhoto} />
      <Text style={styles.newMatchName}>{item.name}</Text>
    </TouchableOpacity>
  );

  // Render match item
  const renderMatchItem = ({ item }) => (
    <TouchableOpacity 
    style={styles.matchItem}
    onPress={() => navigation.navigate('Chat', { 
      matchId: item.id,
      userName: item.name,
      userPhoto: item.photo
    })}
  >
      <View style={styles.matchItemLeft}>
        <Image source={{ uri: item.photo }} style={styles.matchPhoto} />
        <View>
          <Text style={styles.matchName}>{item.name}</Text>
          <Text style={styles.matchLastSeen}>{item.lastSeen}</Text>
        </View>
      </View>
      
      {item.hasNewActivity && (
        <View style={styles.activityDot} />
      )}
    </TouchableOpacity>
  );

  // Render empty state
  const renderEmptyState = () => (
    <View style={styles.emptyContainer}>
      <View style={styles.emptyIconContainer}>
        <Text style={styles.emptyIcon}>👋</Text>
      </View>
      <Text style={styles.emptyTitle}>No matches yet</Text>
      <Text style={styles.emptySubtitle}>
        Connect with people nearby to start matching
      </Text>
      <TouchableOpacity 
        style={styles.emptyButton}
        onPress={() => navigation.navigate('Nearby')}
      >
        <Text style={styles.emptyButtonText}>Explore Nearby</Text>
      </TouchableOpacity>
    </View>
  );

  // Check if there are any matches
  const hasMatches = matches.length > 0 || newMatches.length > 0;

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Matches</Text>
      </View>
      
      {!hasMatches ? (
        renderEmptyState()
      ) : (
        <FlatList
          ListHeaderComponent={
            newMatches.length > 0 ? (
              <View>
                <Text style={styles.sectionTitle}>New Matches</Text>
                <FlatList
                  horizontal
                  data={newMatches}
                  renderItem={renderNewMatchItem}
                  keyExtractor={(item) => item.id}
                  showsHorizontalScrollIndicator={false}
                  contentContainerStyle={styles.newMatchesList}
                />
                <Text style={styles.sectionTitle}>All Matches</Text>
              </View>
            ) : null
          }
          data={matches}
          renderItem={renderMatchItem}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.matchesList}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginTop: 16,
    marginBottom: 12,
    marginLeft: 16,
  },
  newMatchesList: {
    paddingLeft: 16,
  },
  newMatchItem: {
    marginRight: 16,
    alignItems: 'center',
  },
  newMatchPhoto: {
    width: 70,
    height: 70,
    borderRadius: 35,
    marginBottom: 8,
  },
  newMatchName: {
    fontSize: 14,
    maxWidth: 70,
    textAlign: 'center',
  },
  matchesList: {
    paddingBottom: 16,
  },
  matchItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  matchItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  matchPhoto: {
    width: 50,
    height: 50,
    borderRadius: 25,
    marginRight: 12,
  },
  matchName: {
    fontSize: 16,
    fontWeight: '500',
    marginBottom: 4,
  },
  matchLastSeen: {
    fontSize: 14,
    color: '#666666',
  },
  activityDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#FF6B6B',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  emptyIconContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#F5F5F5',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  emptyIcon: {
    fontSize: 32,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 8,
    textAlign: 'center',
  },
  emptySubtitle: {
    fontSize: 14,
    color: '#666666',
    textAlign: 'center',
    marginBottom: 24,
  },
  emptyButton: {
    backgroundColor: '#FF6B6B',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  emptyButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default MatchesScreen;