// src/components/ProximityAlertModal.tsx
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  TouchableOpacity,
  Modal,
  Animated,
  Easing,
  Dimensions,
  Vibration
} from 'react-native';

const { width } = Dimensions.get('window');

type ProximityAlertModalProps = {
  visible: boolean;
  user: {
    id: string;
    name: string;
    photo: string;
    distance: number;
  };
  onClose: () => void;
  onView: () => void;
};

const ProximityAlertModal = ({ visible, user, onClose, onView }: ProximityAlertModalProps) => {
  const [slideAnimation] = useState(new Animated.Value(0));
  const [pulseAnimation] = useState(new Animated.Value(1));
  
  // Start animations when modal becomes visible
  useEffect(() => {
    if (visible) {
      // Slide in animation
      Animated.spring(slideAnimation, {
        toValue: 1,
        useNativeDriver: true,
        tension: 50,
        friction: 7,
      }).start();
      
      // Pulsing animation for distance indicator
      Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnimation, {
            toValue: 1.2,
            duration: 800,
            easing: Easing.out(Easing.ease),
            useNativeDriver: true,
          }),
          Animated.timing(pulseAnimation, {
            toValue: 1,
            duration: 800,
            easing: Easing.in(Easing.ease),
            useNativeDriver: true,
          })
        ])
      ).start();
      
      // Vibrate to alert the user
      Vibration.vibrate([0, 100, 100, 100]);
    } else {
      // Reset animations
      slideAnimation.setValue(0);
      pulseAnimation.setValue(1);
    }
  }, [visible]);
  
  // Slide animation values
  const translateY = slideAnimation.interpolate({
    inputRange: [0, 1],
    outputRange: [-150, 0],
  });
  
  const opacity = slideAnimation.interpolate({
    inputRange: [0, 0.5, 1],
    outputRange: [0, 0.5, 1],
  });

  if (!user) return null;

  return (
    <Modal
      transparent
      visible={visible}
      animationType="none"
      onRequestClose={onClose}
    >
      <View style={styles.modalContainer}>
        <Animated.View 
          style={[
            styles.alertContainer,
            { transform: [{ translateY }], opacity }
          ]}
        >
          <View style={styles.alertHeader}>
            <Animated.View 
              style={[
                styles.distanceIndicator,
                { transform: [{ scale: pulseAnimation }] }
              ]}
            >
              <Text style={styles.distanceText}>{user.distance}m</Text>
            </Animated.View>
            <Text style={styles.alertHeaderText}>Nearby!</Text>
          </View>
          
          <View style={styles.alertContent}>
            <Image source={{ uri: user.photo }} style={styles.userPhoto} />
            <View style={styles.alertTextContainer}>
              <Text style={styles.alertTitle}>{user.name} is close by</Text>
              <Text style={styles.alertSubtitle}>
                You're both in the same area right now
              </Text>
            </View>
          </View>
          
          <View style={styles.actionButtons}>
            <TouchableOpacity 
              style={styles.viewButton}
              onPress={onView}
            >
              <Text style={styles.viewButtonText}>View Profile</Text>
            </TouchableOpacity>
            
            <TouchableOpacity 
              style={styles.dismissButton}
              onPress={onClose}
            >
              <Text style={styles.dismissButtonText}>Later</Text>
            </TouchableOpacity>
          </View>
        </Animated.View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalContainer: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.4)',
    alignItems: 'center',
    paddingTop: 48,
  },
  alertContainer: {
    width: width - 32,
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 16,
    elevation: 10,
    overflow: 'hidden',
  },
  alertHeader: {
    backgroundColor: '#FF6B6B',
    paddingVertical: 12,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  alertHeaderText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
  distanceIndicator: {
    backgroundColor: '#FFFFFF',
    height: 30,
    paddingHorizontal: 12,
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  distanceText: {
    color: '#FF6B6B',
    fontWeight: 'bold',
    fontSize: 14,
  },
  alertContent: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  userPhoto: {
    width: 70,
    height: 70,
    borderRadius: 35,
  },
  alertTextContainer: {
    marginLeft: 16,
    flex: 1,
  },
  alertTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  alertSubtitle: {
    fontSize: 14,
    color: '#666666',
  },
  actionButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 16,
  },
  viewButton: {
    backgroundColor: '#FF6B6B',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
    flex: 1,
    marginRight: 8,
    alignItems: 'center',
  },
  viewButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  dismissButton: {
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    alignItems: 'center',
  },
  dismissButtonText: {
    fontSize: 16,
    color: '#666666',
  },
});

export default ProximityAlertModal;