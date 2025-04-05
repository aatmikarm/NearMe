import React, {useState} from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ActivityIndicator,
  ScrollView,
} from 'react-native';
import {useAuth} from '../../contexts/AuthContext';
import {useNavigation} from '@react-navigation/native';
import {StackNavigationProp} from '@react-navigation/stack';
import {MainStackParamList} from '../../navigation';

type LoginScreenNavigationProp = StackNavigationProp<
  MainStackParamList,
  'Login'
>;

export const LoginScreen = () => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otp, setOtp] = useState('');
  const [verificationSent, setVerificationSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const {sendVerification, verifyCode, error} = useAuth();
  const navigation = useNavigation<LoginScreenNavigationProp>();

  const handleSendVerification = async () => {
    if (!phoneNumber) {
      console.log('Phone number is empty');
      Alert.alert('Error', 'Please enter a phone number');
      return;
    }

    console.log('=== Starting Verification Process ===');
    console.log('Phone number entered:', phoneNumber);

    setLoading(true);
    try {
      console.log('Calling sendVerification...');
      const success = await sendVerification(phoneNumber);
      console.log('sendVerification result:', success);

      if (success) {
        console.log('Verification sent successfully');
        setVerificationSent(true);
        Alert.alert(
          'Success',
          'Verification code sent! Please check your messages.\n\nFor test numbers, use any 6-digit code.',
          [{text: 'OK'}],
        );
      }
    } catch (err) {
      console.error('=== Verification Error ===');
      console.error('Error details:', {
        error: err,
        phoneNumber: phoneNumber,
        timestamp: new Date().toISOString(),
      });

      let errorMessage = 'An error occurred while sending verification code';
      if (err instanceof Error) {
        errorMessage = err.message;
        console.error('Error stack:', err.stack);
      }

      Alert.alert('Verification Error', errorMessage, [
        {
          text: 'Try Again',
          onPress: () => setLoading(false),
        },
        {
          text: 'Contact Support',
          onPress: () => {
            // You can add support contact logic here
            setLoading(false);
          },
        },
      ]);
    } finally {
      console.log('=== Verification Process Completed ===');
      setLoading(false);
    }
  };

  const handleVerifyCode = async () => {
    if (!otp) {
      console.log('OTP is empty');
      Alert.alert('Error', 'Please enter the verification code');
      return;
    }

    console.log('=== Starting OTP Verification ===');
    console.log('OTP entered:', otp);

    setLoading(true);
    try {
      console.log('Calling verifyCode...');
      const success = await verifyCode(otp);
      console.log('verifyCode result:', success);

      if (success) {
        console.log('OTP verification successful, navigating to MainTabs');
        navigation.replace('MainTabs');
      }
    } catch (err) {
      console.error('=== OTP Verification Error ===');
      console.error('Error details:', {
        error: err,
        otp: otp,
        timestamp: new Date().toISOString(),
      });

      Alert.alert(
        'Error',
        err instanceof Error
          ? err.message
          : 'An error occurred while verifying the code',
        [{text: 'OK'}],
      );
    } finally {
      console.log('=== OTP Verification Process Completed ===');
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Welcome to NearMe</Text>

      {!verificationSent ? (
        <>
          <Text style={styles.subtitle}>Enter your phone number</Text>
          <Text style={styles.helpText}>
            For Indian numbers:
            {'\n'}• Enter 10-digit number (e.g., 7891638838)
            {'\n'}• Or enter with country code (e.g., +917891638838)
            {'\n\n'}For testing, you can use these Indian test numbers:
            {'\n'}+919876543210
            {'\n'}+919876543211
            {'\n'}+919876543212
            {'\n\n'}Note: For test numbers, use any 6-digit code
          </Text>
          <TextInput
            style={styles.input}
            placeholder="Enter phone number (e.g., 7891638838)"
            value={phoneNumber}
            onChangeText={setPhoneNumber}
            keyboardType="phone-pad"
            autoCapitalize="none"
          />
          <TouchableOpacity
            style={styles.button}
            onPress={handleSendVerification}
            disabled={loading}>
            {loading ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.buttonText}>Send Verification Code</Text>
            )}
          </TouchableOpacity>
        </>
      ) : (
        <>
          <Text style={styles.subtitle}>Enter verification code</Text>
          <Text style={styles.helpText}>
            For test numbers, use any 6-digit code
            {'\n'}For real numbers, enter the code sent to your phone
          </Text>
          <TextInput
            style={styles.input}
            placeholder="Enter verification code"
            value={otp}
            onChangeText={setOtp}
            keyboardType="number-pad"
            autoCapitalize="none"
            maxLength={6}
          />
          <TouchableOpacity
            style={styles.button}
            onPress={handleVerifyCode}
            disabled={loading}>
            {loading ? (
              <ActivityIndicator color="#fff" />
            ) : (
              <Text style={styles.buttonText}>Verify Code</Text>
            )}
          </TouchableOpacity>
        </>
      )}

      {error && <Text style={styles.errorText}>{error}</Text>}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 18,
    marginBottom: 10,
    textAlign: 'center',
  },
  helpText: {
    fontSize: 14,
    color: '#666',
    marginBottom: 20,
    textAlign: 'center',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    padding: 15,
    borderRadius: 8,
    marginBottom: 15,
    fontSize: 16,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  errorText: {
    color: 'red',
    textAlign: 'center',
    marginTop: 10,
  },
});
