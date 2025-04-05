// src/screens/auth/OtpVerificationScreen.tsx
import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
  Alert
} from 'react-native';
import { useAuth } from '../../contexts/AuthContext';

type OtpScreenProps = {
  route: {
    params: {
      phoneNumber: string;
    };
  };
  navigation: any;
};

const OtpVerificationScreen = ({ route, navigation }: OtpScreenProps) => {
  //const { phoneNumber } = route.params;
  //const { signIn } = useAuth();

  const { phoneNumber, verificationId } = route.params;
  const { confirmOtp } = useAuth();
  
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [timer, setTimer] = useState(60);
  const [isVerifying, setIsVerifying] = useState(false);
  
  // Create refs for TextInputs
  const inputRefs = useRef<any[]>([]);
  
  // Initialize refs
  useEffect(() => {
    inputRefs.current = inputRefs.current.slice(0, 6);
  }, []);

  // Handle OTP input change
  const handleOtpChange = (value: string, index: number) => {
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);
    
    // Move to next input if value is entered
    if (value && index < 5 && inputRefs.current[index + 1]) {
      inputRefs.current[index + 1].focus();
    }
  };
  
  // Handle backspace key press
  const handleKeyPress = (e: any, index: number) => {
    if (e.nativeEvent.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1].focus();
    }
  };
  
  // Countdown timer
  useEffect(() => {
    const interval = setInterval(() => {
      if (timer > 0) {
        setTimer(timer - 1);
      }
    }, 1000);
    
    return () => clearInterval(interval);
  }, [timer]);
  
  // Format seconds to MM:SS
  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };
  
  // Resend OTP
  const handleResend = () => {
    setTimer(60);
    // Here you would use the auth service to resend the OTP
    // For now, just reset the timer
  };
  
  // Verify OTP
  // const handleVerify = async () => {
  //   const enteredOtp = otp.join('');
    
  //   setIsVerifying(true);
  //   try {
  //     const success = await signIn(phoneNumber, enteredOtp);
      
  //     if (success) {
  //       navigation.reset({
  //         index: 0,
  //         routes: [{ name: 'ProfileCreation' }],
  //       });
  //     } else {
  //       Alert.alert(
  //         "Verification Failed", 
  //         "The code you entered is incorrect. Please try again."
  //       );
  //     }
  //   } catch (error) {
  //     Alert.alert(
  //       "Verification Error",
  //       "Something went wrong. Please try again."
  //     );
  //   } finally {
  //     setIsVerifying(false);
  //   }
  // };


  const handleVerify = async () => {
    const enteredOtp = otp.join('');
    setIsVerifying(true);
    
    try {
      const success = await confirmOtp(verificationId, enteredOtp);
      if (success) {
        navigation.reset({
          index: 0,
          routes: [{ name: 'ProfileCreation' }],
        });
      } else {
        Alert.alert("Verification Failed", "The code you entered is incorrect.");
      }
    } catch (error) {
      Alert.alert("Error", "Something went wrong. Please try again.");
    } finally {
      setIsVerifying(false);
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
          <Text style={styles.headerTitle}>Verification Code</Text>
        </View>
        
        <View style={styles.content}>
          <Text style={styles.description}>
            We've sent a 6-digit code to {phoneNumber}
          </Text>
          
          <View style={styles.otpContainer}>
            {otp.map((digit, index) => (
              <TextInput
                key={index}
                ref={el => inputRefs.current[index] = el}
                style={styles.otpInput}
                // src/screens/auth/OtpVerificationScreen.tsx (continued)
                value={digit}
                onChangeText={(value) => handleOtpChange(value, index)}
                onKeyPress={(e) => handleKeyPress(e, index)}
                keyboardType="number-pad"
                maxLength={1}
                autoFocus={index === 0}
              />
            ))}
          </View>
          
          <View style={styles.timerContainer}>
            {timer > 0 ? (
              <Text style={styles.timer}>Resend code in {formatTime(timer)}</Text>
            ) : (
              <TouchableOpacity onPress={handleResend}>
                <Text style={styles.resendButton}>Resend Code</Text>
              </TouchableOpacity>
            )}
          </View>
        </View>
        
        <View style={styles.footer}>
          <TouchableOpacity 
            style={[
              styles.button, 
              (otp.includes('') || isVerifying) ? styles.buttonDisabled : {}
            ]} 
            onPress={handleVerify}
            disabled={otp.includes('') || isVerifying}
          >
            {isVerifying ? (
              <ActivityIndicator color="#FFFFFF" size="small" />
            ) : (
              <Text style={styles.buttonText}>Verify</Text>
            )}
          </TouchableOpacity>
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
    marginBottom: 32,
    color: '#666666',
    textAlign: 'center',
  },
  otpContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 32,
  },
  otpInput: {
    width: 45,
    height: 50,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    textAlign: 'center',
    fontSize: 20,
  },
  timerContainer: {
    alignItems: 'center',
  },
  timer: {
    color: '#888888',
    fontSize: 14,
  },
  resendButton: {
    color: '#FF6B6B',
    fontSize: 16,
    fontWeight: '600',
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
  },
  buttonDisabled: {
    backgroundColor: '#FFADAD',
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
  },
});

export default OtpVerificationScreen;