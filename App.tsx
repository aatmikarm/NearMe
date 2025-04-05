// App.tsx
import React from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import AppNavigator from './src/navigation';
import { AuthProvider } from './src/contexts/AuthContext';
import { UserProvider } from './src/contexts/UserContext';
import { LocationProvider } from './src/contexts/LocationContext';
import { MatchProvider } from './src/contexts/MatchContext';

const App = () => {
  return (
    <SafeAreaProvider>
      <AuthProvider>
        <UserProvider>
          <LocationProvider>
            <MatchProvider>
              <AppNavigator />
            </MatchProvider>
          </LocationProvider>
        </UserProvider>
      </AuthProvider>
    </SafeAreaProvider>
  );
};

export default App;