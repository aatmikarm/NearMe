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
  Platform,
  ActivityIndicator,
  Alert
} from 'react-native';
import { sendPhoneVerification } from '../../services/authService';

const PhoneAuthScreen = ({ navigation }) => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [countryCode, setCountryCode] = useState('+91'); // Default to India
  const [isLoading, setIsLoading] = useState(false);
  
  const handleContinue = async () => {
    // Validate phone number
    if (!phoneNumber || phoneNumber.length < 10) {
      Alert.alert('Invalid Phone Number', 'Please enter a valid phone number.');
      return;
    }
    
    setIsLoading(true);
    
    // Format the full phone number
    const fullPhoneNumber = `${countryCode}${phoneNumber}`;
    
    // Send verification code
    const success = await sendPhoneVerification(fullPhoneNumber);
    
    setIsLoading(false);
    
    if (success) {
      // Navigate to OTP verification screen
      navigation.navigate('OtpVerification', { 
        phoneNumber: fullPhoneNumber 
      });
    } else {
      Alert.alert(
        'Verification Failed',
        'Failed to send verification code. Please try again.'
      );
    }
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
              (!phoneNumber || phoneNumber.length < 10 || isLoading) ? styles.buttonDisabled : {}
            ]} 
            onPress={handleContinue}
            disabled={!phoneNumber || phoneNumber.length < 10 || isLoading}
          >
            {isLoading ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <Text style={styles.buttonText}>Continue</Text>
            )}
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
  // Your existing styles here
});

export default PhoneAuthScreen;