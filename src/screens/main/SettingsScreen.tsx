// src/screens/main/SettingsScreen.tsx
import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Switch,
  Alert
} from 'react-native';

type SettingsScreenProps = {
  navigation: any;
};

const SettingsScreen = ({ navigation }: SettingsScreenProps) => {
  const [locationEnabled, setLocationEnabled] = useState(true);
  const [backgroundLocation, setBackgroundLocation] = useState(true);
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [showDistance, setShowDistance] = useState(true);
  const [profileVisibility, setProfileVisibility] = useState(true);
  
  const toggleLocationEnabled = () => setLocationEnabled(!locationEnabled);
  const toggleBackgroundLocation = () => setBackgroundLocation(!backgroundLocation);
  const toggleNotifications = () => setNotificationsEnabled(!notificationsEnabled);
  const toggleShowDistance = () => setShowDistance(!showDistance);
  const toggleProfileVisibility = () => setProfileVisibility(!profileVisibility);
  
  const handleLogout = () => {
    Alert.alert(
      "Logout",
      "Are you sure you want to logout?",
      [
        {
          text: "Cancel",
          style: "cancel"
        },
        { 
          text: "Logout", 
          onPress: () => {
            // In a real app, clear auth state
            navigation.reset({
              index: 0,
              routes: [{ name: 'Auth' }],
            });
          },
          style: "destructive"
        }
      ]
    );
  };

  
const handleEditProfile = () => {
  // Navigate back to profile screen and open edit modal
  navigation.goBack();
  // You would trigger the edit modal on the profile screen
  // This is a workaround since we don't have a state management solution yet
  setTimeout(() => {
    // You would trigger the edit modal here in a real app
    alert('Edit Profile functionality will be implemented in the future.');
  }, 500);
};

const handleChangePhone = () => {
  alert('Change Phone Number functionality will be implemented in the future.');
};

const handleConnectInstagram = () => {
  navigation.navigate('InstagramConnection');
};

const handleBlockedUsers = () => {
  alert('Blocked Users functionality will be implemented in the future.');
};

const handleProximityRange = () => {
  alert('Proximity Range settings will be implemented in the future.');
};

const handleNotificationPreferences = () => {
  alert('Notification Preferences will be implemented in the future.');
};

const handleHelpCenter = () => {
  alert('Help Center will be implemented in the future.');
};

const handlePrivacyPolicy = () => {
  alert('Privacy Policy will be displayed in the future.');
};

const handleTerms = () => {
  alert('Terms of Service will be displayed in the future.');
};
  
  const handleDeleteAccount = () => {
    Alert.alert(
      "Delete Account",
      "Are you sure you want to delete your account? This action cannot be undone.",
      [
        {
          text: "Cancel",
          style: "cancel"
        },
        { 
          text: "Delete", 
          onPress: () => {
            // In a real app, delete account
            navigation.reset({
              index: 0,
              routes: [{ name: 'Auth' }],
            });
          },
          style: "destructive"
        }
      ]
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backButton}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Settings</Text>
        <View style={{ width: 24 }} />
      </View>
      
      <ScrollView style={styles.content}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Account</Text>
          
          <TouchableOpacity style={styles.settingItem} onPress={handleEditProfile}>
  <Text style={styles.settingLabel}>Edit Profile</Text>
  <Text style={styles.settingAction}>></Text>
</TouchableOpacity>

          
<TouchableOpacity style={styles.settingItem} onPress={handleChangePhone}>
  <Text style={styles.settingLabel}>Change Phone Number</Text>
  <Text style={styles.settingAction}>></Text>
</TouchableOpacity>
          
<TouchableOpacity style={styles.settingItem} onPress={handleConnectInstagram}>
  <Text style={styles.settingLabel}>Connect Instagram</Text>
  <Text style={styles.settingAction}>></Text>
</TouchableOpacity>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Privacy</Text>
          
          <View style={styles.settingItem}>
            <Text style={styles.settingLabel}>Profile Visibility</Text>
            <Switch
              trackColor={{ false: '#DDDDDD', true: '#FF6B6B' }}
              thumbColor="#FFFFFF"
              onValueChange={toggleProfileVisibility}
              value={profileVisibility}
            />
          </View>
          
          <View style={styles.settingItem}>
            <Text style={styles.settingLabel}>Show Distance</Text>
            <Switch
              trackColor={{ false: '#DDDDDD', true: '#FF6B6B' }}
              thumbColor="#FFFFFF"
              onValueChange={toggleShowDistance}
              value={showDistance}
            />
          </View>
          
          <TouchableOpacity style={styles.settingItem}>
            <Text style={styles.settingLabel}>Blocked Users</Text>
            <Text style={styles.settingAction}>></Text>
          </TouchableOpacity>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Location</Text>
          
          <View style={styles.settingItem}>
            <View>
              <Text style={styles.settingLabel}>Location Services</Text>
              <Text style={styles.settingDescription}>Required for discovering nearby users</Text>
            </View>
            <Switch
              trackColor={{ false: '#DDDDDD', true: '#FF6B6B' }}
              thumbColor="#FFFFFF"
              onValueChange={toggleLocationEnabled}
              value={locationEnabled}
            />
          </View>
          
          <View style={styles.settingItem}>
            <View>
              <Text style={styles.settingLabel}>Background Location</Text>
              <Text style={styles.settingDescription}>Allow detection when app is closed</Text>
            </View>
            <Switch
              trackColor={{ false: '#DDDDDD', true: '#FF6B6B' }}
              thumbColor="#FFFFFF"
              onValueChange={toggleBackgroundLocation}
              value={backgroundLocation}
            />
          </View>
          
          <TouchableOpacity style={styles.settingItem}>
            <Text style={styles.settingLabel}>Proximity Range</Text>
            <Text style={styles.settingValue}>100m</Text>
          </TouchableOpacity>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Notifications</Text>
          
          <View style={styles.settingItem}>
            <Text style={styles.settingLabel}>Push Notifications</Text>
            <Switch
              trackColor={{ false: '#DDDDDD', true: '#FF6B6B' }}
              thumbColor="#FFFFFF"
              onValueChange={toggleNotifications}
              value={notificationsEnabled}
            />
          </View>
          
          <TouchableOpacity style={styles.settingItem}>
            <Text style={styles.settingLabel}>Notification Preferences</Text>
            <Text style={styles.settingAction}>></Text>
          </TouchableOpacity>
        </View>
        
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Support</Text>
          
          <TouchableOpacity style={styles.settingItem}>
            <Text style={styles.settingLabel}>Help Center</Text>
            <Text style={styles.settingAction}>></Text>
          </TouchableOpacity>
          
          <TouchableOpacity style={styles.settingItem}>
            <Text style={styles.settingLabel}>Privacy Policy</Text>
            <Text style={styles.settingAction}>></Text>
          </TouchableOpacity>
          
          <TouchableOpacity style={styles.settingItem}>
            <Text style={styles.settingLabel}>Terms of Service</Text>
            <Text style={styles.settingAction}>></Text>
          </TouchableOpacity>
        </View>
        
        <TouchableOpacity 
          style={styles.logoutButton}
          onPress={handleLogout}
        >
          <Text style={styles.logoutButtonText}>Logout</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={styles.deleteAccountButton}
          onPress={handleDeleteAccount}
        >
          <Text style={styles.deleteAccountButtonText}>Delete Account</Text>
        </TouchableOpacity>


        
        <View style={styles.versionInfo}>
          <Text style={styles.versionText}>NearMe v1.0.0</Text>
        </View>
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
  backButton: {
    fontSize: 24,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  content: {
    flex: 1,
  },
  section: {
    paddingTop: 24,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#666666',
    marginBottom: 8,
    paddingHorizontal: 16,
  },
  settingItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  settingLabel: {
    fontSize: 16,
    color: '#333333',
  },
  settingDescription: {
    fontSize: 12,
    color: '#666666',
    marginTop: 4,
  },
  settingAction: {
    fontSize: 16,
    color: '#CCCCCC',
  },
  settingValue: {
    fontSize: 16,
    color: '#666666',
  },
  logoutButton: {
    marginTop: 24,
    marginHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    backgroundColor: '#F5F5F5',
    alignItems: 'center',
  },
  logoutButtonText: {
    fontSize: 16,
    color: '#FF6B6B',
    fontWeight: '600',
  },
  deleteAccountButton: {
    marginTop: 12,
    marginHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  deleteAccountButtonText: {
    fontSize: 16,
    color: '#FF3B30',
  },
  versionInfo: {
    alignItems: 'center',
    marginTop: 24,
    marginBottom: 32,
  },
  versionText: {
    fontSize: 12,
    color: '#999999',
  },
});

export default SettingsScreen;