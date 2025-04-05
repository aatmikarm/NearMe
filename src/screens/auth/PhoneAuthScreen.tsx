// src/screens/auth/PhoneAuthScreen.tsx
import React, { useState } from 'react';
import { 
  View, 
  Text, 
  StyleSheet, 
  TextInput, 
  TouchableOpacity, 
  StatusBar,
  SafeAreaView,
  KeyboardAvoidingView,
  Platform
} from 'react-native';

const PhoneAuthScreen = ({ navigation }) => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [countryCode, setCountryCode] = useState('+91'); // Default to India
  
  const handleContinue = () => {
    // For now, just navigate to the OTP screen
    // Later we'll implement actual Firebase phone auth
    navigation.navigate('OtpVerification', { 
      phoneNumber: countryCode + phoneNumber 
    });
  };
  
  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      <KeyboardAvoidingView 
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        style={styles.keyboardAvoid}
      >
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Text style={styles.backButton}>←</Text>
          </TouchableOpacity>
          <Text style={styles.headerTitle}>Verify Your Phone</Text>
        </View>
        
        <View style={styles.content}>
          <Text style={styles.description}>
            We'll send a verification code to your phone number.
          </Text>
          
          <View style={styles.phoneContainer}>
            <TextInput
              style={styles.countryCode}
              value={countryCode}
              onChangeText={setCountryCode}
              keyboardType="phone-pad"
            />
            <TextInput
              style={styles.phoneInput}
              value={phoneNumber}
              onChangeText={setPhoneNumber}
              placeholder="Phone number"
              keyboardType="phone-pad"
              autoFocus
            />
          </View>
        </View>
        
        <View style={styles.footer}>
          <TouchableOpacity 
            style={[
              styles.button, 
              (!phoneNumber || phoneNumber.length < 10) ? styles.buttonDisabled : {}
            ]} 
            onPress={handleContinue}
            disabled={!phoneNumber || phoneNumber.length < 10}
          >
            <Text style={styles.buttonText}>Continue</Text>
          </TouchableOpacity>
          
          <Text style={styles.terms}>
            By continuing, you agree to our Terms of Service and Privacy Policy
          </Text>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  keyboardAvoid: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  backButton: {
    fontSize: 24,
    marginRight: 16,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  content: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
  },
  description: {
    fontSize: 16,
    marginBottom: 24,
    color: '#666666',
  },
  phoneContainer: {
    flexDirection: 'row',
    marginBottom: 24,
  },
  countryCode: {
    width: 60,
    height: 50,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    marginRight: 8,
    paddingHorizontal: 12,
    fontSize: 16,
  },
  phoneInput: {
    flex: 1,
    height: 50,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    paddingHorizontal: 12,
    fontSize: 16,
  },
  footer: {
    padding: 24,
  },
  button: {
    backgroundColor: '#FF6B6B',
    borderRadius: 12,
    height: 56,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  buttonDisabled: {
    backgroundColor: '#FFADAD',
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
  },
  terms: {
    fontSize: 12,
    color: '#888888',
    textAlign: 'center',
  },
});

export default PhoneAuthScreen;