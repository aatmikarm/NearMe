// src/navigation/index.tsx
import React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createStackNavigator} from '@react-navigation/stack';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import {Text, ActivityIndicator, View, StyleSheet} from 'react-native';
import {useAuth} from '../contexts/AuthContext';
import {StackNavigationProp} from '@react-navigation/stack';
import {RouteProp} from '@react-navigation/native';

// Auth screens
import WelcomeScreen from '../screens/auth/WelcomeScreen';
import PhoneAuthScreen from '../screens/auth/PhoneAuthScreen';
import OtpVerificationScreen from '../screens/auth/OtpVerificationScreen';
import ProfileCreationScreen from '../screens/auth/ProfileCreationScreen';
import InstagramConnectionScreen from '../screens/auth/InstagramConnectionScreen';

// Main app screens
import NearbyScreen from '../screens/main/NearbyScreen';
import MatchesScreen from '../screens/main/MatchesScreen';
import ProfileScreen from '../screens/main/ProfileScreen';
import UserDetailScreen from '../screens/main/UserDetailScreen';
import SettingsScreen from '../screens/main/SettingsScreen';
import MatchConfirmationScreen from '../screens/main/MatchConfirmationScreen';
import ChatScreen from '../screens/main/ChatScreen';
//import TestFirebaseScreen from '../screens/main/TestFirebaseScreen';

// Define the type for the root stack parameter list
export type RootStackParamList = {
  Auth: undefined;
  Main: undefined;
};

// Define the type for the main stack parameter list
export type MainStackParamList = {
  Login: undefined;
  MainTabs: undefined;
  UserDetail: {userId: string};
  Settings: undefined;
  MatchConfirmation: {
    matchId: string;
    userId: string;
  };
  InstagramConnection: undefined;
  TestFirebase: undefined;
  Chat: {matchId: string; userName: string; userPhoto: string};
};

// Define the type for the auth stack parameter list
export type AuthStackParamList = {
  Welcome: undefined;
  PhoneAuth: undefined;
  OtpVerification: {phoneNumber: string};
  InstagramConnection: undefined;
  ProfileCreation: undefined;
};

// Define navigation prop types
export type MainStackNavigationProp = StackNavigationProp<MainStackParamList>;
export type AuthStackNavigationProp = StackNavigationProp<AuthStackParamList>;

const Stack = createStackNavigator<MainStackParamList>();
const Tab = createBottomTabNavigator();
const RootStack = createStackNavigator<RootStackParamList>();
const AuthStackNavigator = createStackNavigator<AuthStackParamList>();

// Define the MainTabs component with the tab navigator
const MainTabs = () => (
  <Tab.Navigator
    screenOptions={{
      headerShown: false,
    }}>
    <Tab.Screen
      name="Nearby"
      component={NearbyScreen}
      options={{
        tabBarIcon: ({color, size}) => (
          <Text style={{fontSize: size, color}}>📍</Text>
        ),
      }}
    />
    <Tab.Screen
      name="Matches"
      component={MatchesScreen}
      options={{
        tabBarIcon: ({color, size}) => (
          <Text style={{fontSize: size, color}}>❤️</Text>
        ),
      }}
    />
    <Tab.Screen
      name="Profile"
      component={ProfileScreen}
      options={{
        tabBarIcon: ({color, size}) => (
          <Text style={{fontSize: size, color}}>👤</Text>
        ),
      }}
    />
  </Tab.Navigator>
);

// Main Stack Navigator (includes tabs and modal screens)
const MainStack = () => (
  <Stack.Navigator
    screenOptions={{
      headerShown: false,
      cardStyle: {backgroundColor: '#FFFFFF'},
    }}>
    <Stack.Screen
      name="MainTabs"
      component={MainTabs}
      options={{headerShown: false}}
    />
    <Stack.Screen
      name="UserDetail"
      component={UserDetailScreen}
      options={{headerShown: false}}
    />
    <Stack.Screen
      name="Settings"
      component={SettingsScreen}
      options={{headerShown: false}}
    />
    <Stack.Screen
      name="MatchConfirmation"
      component={MatchConfirmationScreen}
      options={{
        headerShown: false,
        presentation: 'modal',
      }}
    />
    <Stack.Screen
      name="InstagramConnection"
      component={InstagramConnectionScreen}
      options={{headerShown: false}}
    />
    <Stack.Screen
      name="Chat"
      component={ChatScreen}
      options={{headerShown: false}}
    />
    {/* <Stack.Screen
      name="TestFirebase"
      component={TestFirebaseScreen}
      options={{headerShown: false}}
    /> */}
  </Stack.Navigator>
);

// Auth Stack Navigator
const AuthStack = () => (
  <AuthStackNavigator.Navigator screenOptions={{headerShown: false}}>
    <AuthStackNavigator.Screen name="Welcome" component={WelcomeScreen} />
    <AuthStackNavigator.Screen name="PhoneAuth" component={PhoneAuthScreen} />
    <AuthStackNavigator.Screen
      name="OtpVerification"
      component={OtpVerificationScreen}
    />
    <AuthStackNavigator.Screen
      name="InstagramConnection"
      component={InstagramConnectionScreen}
    />
    <AuthStackNavigator.Screen
      name="ProfileCreation"
      component={ProfileCreationScreen}
    />
  </AuthStackNavigator.Navigator>
);

// Loading component
const LoadingScreen = () => (
  <View style={styles.loadingContainer}>
    <ActivityIndicator size="large" color="#FF6B6B" />
    <Text style={styles.loadingText}>Loading NearMe...</Text>
  </View>
);

const AppNavigator = () => {
  const {user, loading} = useAuth();

  // Show loading screen while checking authentication state
  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <NavigationContainer>
      <RootStack.Navigator screenOptions={{headerShown: false}}>
        {!user ? (
          <RootStack.Screen name="Auth" component={AuthStack} />
        ) : (
          <RootStack.Screen name="Main" component={MainStack} />
        )}
      </RootStack.Navigator>
    </NavigationContainer>
  );
};

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666666',
  },
});

export default AppNavigator;
