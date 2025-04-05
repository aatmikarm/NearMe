// src/screens/main/NearbyScreen.tsx
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Image,
  SafeAreaView,
  StatusBar,
  Dimensions,
  ActivityIndicator,
  RefreshControl,
  Alert
} from 'react-native';
import ProximityAlertModal from '../../components/ProximityAlertModal';
import FilterModal from '../../components/FilterModal';
import { useLocation } from '../../contexts/LocationContext';
import { useMatch } from '../../contexts/MatchContext';
import { useUser } from '../../contexts/UserContext';

const { width } = Dimensions.get('window');
const CARD_WIDTH = width / 2 - 24; // Two cards per row with margins

type NearbyScreenProps = {
  navigation: any;
};

const NearbyScreen = ({ navigation }: NearbyScreenProps) => {
  const { currentLocation, isTrackingLocation, startLocationTracking, locationPermissionGranted, requestLocationPermission } = useLocation();
  const { nearbyUsers, isLoading: isLoadingMatches, refreshNearbyUsers, simulateProximityEvent } = useMatch();
  const { userProfile, userPreferences, updatePreferences } = useUser();
  
  const [refreshing, setRefreshing] = useState(false);
  
  // State for proximity alert
  const [alertVisible, setAlertVisible] = useState(false);
  const [nearbyUser, setNearbyUser] = useState<any>(null);
  
  // State for filter modal
  const [filterModalVisible, setFilterModalVisible] = useState(false);
  
  // Check location permissions on mount
  useEffect(() => {
    if (!locationPermissionGranted) {
      requestLocationPermission().then(granted => {
        if (granted && !isTrackingLocation) {
          startLocationTracking();
        }
      });
    } else if (!isTrackingLocation) {
      startLocationTracking();
    }
  }, [locationPermissionGranted, isTrackingLocation]);
  
  // Simulate proximity alert after delay (for demo purposes)
  useEffect(() => {
    if (nearbyUsers.length > 0) {
      const timer = setTimeout(() => {
        const randomUser = nearbyUsers[Math.floor(Math.random() * nearbyUsers.length)];
        if (randomUser) {
          setNearbyUser({
            id: randomUser.id,
            name: randomUser.name,
            photo: randomUser.photo,
            distance: Math.floor(Math.random() * 30) + 5 // Random close distance 5-35m
          });
          setAlertVisible(true);
          
          // Simulate a proximity event
          simulateProximityEvent(randomUser.id, nearbyUser?.distance || 20);
        }
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [nearbyUsers]);
  
  // Handle refresh
  const onRefresh = async () => {
    setRefreshing(true);
    await refreshNearbyUsers();
    setRefreshing(false);
  };
  
  // Alert handlers
  const handleCloseAlert = () => {
    setAlertVisible(false);
  };
  
  const handleViewUser = () => {
    setAlertVisible(false);
    navigation.navigate('UserDetail', { userId: nearbyUser.id });
  };
  
  // Filter handlers
  const handleFilterPress = () => {
    setFilterModalVisible(true);
  };
  
  const handleApplyFilters = async (filters) => {
    if (userProfile) {
      // Update user preferences in the context
      await updatePreferences(filters);
      
      // Refresh with new filters
      onRefresh();
    }
    
    setFilterModalVisible(false);
  };

  // Render empty state
  const renderEmptyState = () => (
    <View style={styles.emptyContainer}>
      <View style={styles.emptyIconContainer}>
        <Text style={styles.emptyIcon}>📍</Text>
      </View>
      <Text style={styles.emptyTitle}>No one nearby right now</Text>
      <Text style={styles.emptySubtitle}>
        We'll notify you when someone is close by
      </Text>
      
      {!locationPermissionGranted && (
        <TouchableOpacity 
          style={styles.permissionButton}
          onPress={() => requestLocationPermission()}
        >
          <Text style={styles.permissionButtonText}>Enable Location</Text>
        </TouchableOpacity>
      )}
    </View>
  );

  // Render a user card
  const renderUserCard = ({ item }) => (
    <TouchableOpacity 
      style={styles.card}
      onPress={() => navigation.navigate('UserDetail', { userId: item.id })}
    >
      <Image source={{ uri: item.photo }} style={styles.cardImage} />
      <View style={styles.cardInfo}>
        <Text style={styles.cardName}>{item.name}, {item.age}</Text>
        <Text style={styles.cardDistance}>{item.distance}m away</Text>
      </View>
    </TouchableOpacity>
  );

  // Loading state
  if (isLoadingMatches && !refreshing) {
    return (
      <SafeAreaView style={styles.container}>
        <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Nearby</Text>
          <TouchableOpacity 
            style={styles.filterButton}
            onPress={handleFilterPress}
          >
            <Text style={styles.filterText}>Filter</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#FF6B6B" />
          <Text style={styles.loadingText}>Finding people nearby...</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Nearby</Text>
        <TouchableOpacity 
          style={styles.filterButton}
          onPress={handleFilterPress}
        >
          <Text style={styles.filterText}>Filter</Text>
        </TouchableOpacity>
      </View>
      
      {!currentLocation ? (
        <View style={styles.locationMessage}>
          <Text style={styles.locationMessageText}>
            Waiting for your location...
          </Text>
        </View>
      ) : (
        <FlatList
          data={nearbyUsers}
          renderItem={renderUserCard}
          keyExtractor={(item) => item.id}
          numColumns={2}
          contentContainerStyle={styles.listContent}
          ListEmptyComponent={renderEmptyState}
          refreshControl={
            <RefreshControl
              refreshing={refreshing}
              onRefresh={onRefresh}
              colors={['#FF6B6B']}
            />
          }
        />
      )}
      
      {/* Proximity alert modal */}
      {nearbyUser && (
        <ProximityAlertModal
          visible={alertVisible}
          user={nearbyUser}
          onClose={handleCloseAlert}
          onView={handleViewUser}
        />
      )}
      
      {/* Filter Modal */}
      <FilterModal
        visible={filterModalVisible}
        onClose={() => setFilterModalVisible(false)}
        onApply={handleApplyFilters}
      />
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
  filterButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    backgroundColor: '#F5F5F5',
  },
  filterText: {
    fontSize: 14,
    color: '#666666',
  },
  listContent: {
    padding: 12,
    flexGrow: 1,
  },
  card: {
    width: CARD_WIDTH,
    marginHorizontal: 8,
    marginBottom: 16,
    borderRadius: 12,
    backgroundColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
    overflow: 'hidden',
  },
  cardImage: {
    width: '100%',
    height: CARD_WIDTH * 1.3,
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
  },
  cardInfo: {
    padding: 12,
  },
  cardName: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
  },
  cardDistance: {
    fontSize: 14,
    color: '#666666',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  loadingText: {
    marginTop: 12,
    fontSize: 16,
    color: '#666666',
  },
  locationMessage: {
    padding: 12,
    backgroundColor: '#FFF9C4',
    margin: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  locationMessageText: {
    color: '#5D4037',
    fontSize: 14,
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
  permissionButton: {
    backgroundColor: '#FF6B6B',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
  },
  permissionButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default NearbyScreen;