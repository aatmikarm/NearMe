// src/screens/auth/ProfileCreationScreen.tsx
import React, { useState } from 'react';
import { CommonActions } from '@react-navigation/native';
// Import AuthContext
import { useAuth } from '../../contexts/AuthContext';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  ScrollView,
  Image,
  Platform
} from 'react-native';

type ProfileCreationProps = {
  navigation: any;
};

const ProfileCreationScreen = ({ navigation }: ProfileCreationProps) => {
  const [step, setStep] = useState(1);
  const [name, setName] = useState('');
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('');
  const [bio, setBio] = useState('');
  const [photos, setPhotos] = useState<string[]>([]);
  const [instagram, setInstagram] = useState('');

   // Add this inside your component
   const { setUserAuthenticated } = useAuth();

  // Handle photo selection
  const handleSelectPhoto = () => {
    // Here you would implement photo selection
    // For now, just add a placeholder
    setPhotos([...photos, 'https://via.placeholder.com/150']);
  };

  // Update the handleNextStep function
  const handleNextStep = () => {
    if (step < 3) {
      setStep(step + 1);
    } else {
      // Instead of using navigation.reset, use the auth context
      // This will trigger a re-render of the navigation container
      setUserAuthenticated(true);
    }
  };

  // Render step content
  const renderStepContent = () => {
    switch (step) {
      case 1:
        return (
          <View style={styles.stepContent}>
            <Text style={styles.label}>Your Name</Text>
            <TextInput
              style={styles.input}
              value={name}
              onChangeText={setName}
              placeholder="Enter your name"
              autoFocus
            />

            <Text style={styles.label}>Age</Text>
            <TextInput
              style={styles.input}
              value={age}
              onChangeText={setAge}
              placeholder="Your age"
              keyboardType="number-pad"
              maxLength={2}
            />

            <Text style={styles.label}>I am a</Text>
            <View style={styles.genderContainer}>
              <TouchableOpacity
                style={[
                  styles.genderButton,
                  gender === 'male' && styles.selectedGender,
                ]}
                onPress={() => setGender('male')}
              >
                <Text style={styles.genderText}>Man</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.genderButton,
                  gender === 'female' && styles.selectedGender,
                ]}
                onPress={() => setGender('female')}
              >
                <Text style={styles.genderText}>Woman</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.genderButton,
                  gender === 'other' && styles.selectedGender,
                ]}
                onPress={() => setGender('other')}
              >
                <Text style={styles.genderText}>Other</Text>
              </TouchableOpacity>
            </View>
          </View>
        );

      case 2:
        return (
          <View style={styles.stepContent}>
            <Text style={styles.label}>Add Photos</Text>
            <Text style={styles.subLabel}>
              Add at least one photo to continue
            </Text>

            <View style={styles.photosGrid}>
              {[...photos, null, null, null, null, null, null]
                .slice(0, 6)
                .map((photo, index) => (
                  <TouchableOpacity
                    key={index}
                    style={styles.photoBox}
                    onPress={handleSelectPhoto}
                  >
                    {photo ? (
                      <Image source={{ uri: photo }} style={styles.photo} />
                    ) : (
                      <View style={styles.addPhotoBox}>
                        <Text style={styles.addPhotoText}>+</Text>
                      </View>
                    )}
                  </TouchableOpacity>
                ))}
            </View>
          </View>
        );

      case 3:
        return (
          <View style={styles.stepContent}>
            <Text style={styles.label}>About Me</Text>
            <TextInput
              style={[styles.input, styles.bioInput]}
              value={bio}
              onChangeText={setBio}
              placeholder="Tell others about yourself..."
              multiline
              maxLength={500}
            />

            <Text style={styles.label}>Instagram (Optional)</Text>
            <View style={styles.instagramContainer}>
              <Text style={styles.instagramAt}>@</Text>
              <TextInput
                style={styles.instagramInput}
                value={instagram}
                onChangeText={setInstagram}
                placeholder="username"
              />
            </View>
            <Text style={styles.infoText}>
              Your Instagram will only be shared when you match with someone
            </Text>
          </View>
        );

      default:
        return null;
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <View style={styles.header}>
        <TouchableOpacity
          onPress={() => (step > 1 ? setStep(step - 1) : navigation.goBack())}
        >
          <Text style={styles.backButton}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Create Profile</Text>
      </View>

      <View style={styles.progressContainer}>
        <View style={styles.progressBar}>
          <View
            style={[
              styles.progressFill,
              { width: `${((step - 1) / 2) * 100}%` },
            ]}
          />
        </View>
        <Text style={styles.stepText}>Step {step} of 3</Text>
      </View>

      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
      >
        {renderStepContent()}
      </ScrollView>

      <View style={styles.footer}>
        <TouchableOpacity
          style={[
            styles.button,
            ((step === 1 && (!name || !age || !gender)) ||
              (step === 2 && photos.length === 0) ||
              (step === 3 && !bio)) &&
              styles.buttonDisabled,
          ]}
          onPress={handleNextStep}
          disabled={
            (step === 1 && (!name || !age || !gender)) ||
            (step === 2 && photos.length === 0) ||
            (step === 3 && !bio)
          }
        >
          <Text style={styles.buttonText}>
            {step === 3 ? 'Finish' : 'Continue'}
          </Text>
        </TouchableOpacity>
      </View>
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
  progressContainer: {
    padding: 16,
  },
  progressBar: {
    height: 4,
    backgroundColor: '#F0F0F0',
    borderRadius: 2,
    marginBottom: 8,
  },
  progressFill: {
    height: 4,
    backgroundColor: '#FF6B6B',
    borderRadius: 2,
  },
  stepText: {
    fontSize: 12,
    color: '#888888',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
  },
  stepContent: {
    flex: 1,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    marginTop: 16,
  },
  subLabel: {
    fontSize: 14,
    color: '#666666',
    marginBottom: 16,
  },
  input: {
    height: 50,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    paddingHorizontal: 12,
    fontSize: 16,
    marginBottom: 16,
  },
  bioInput: {
    height: 120,
    textAlignVertical: 'top',
    paddingTop: 12,
  },
  genderContainer: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  genderButton: {
    flex: 1,
    height: 50,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  selectedGender: {
    borderColor: '#FF6B6B',
    backgroundColor: 'rgba(255, 107, 107, 0.1)',
  },
  genderText: {
    fontSize: 16,
  },
  photosGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginHorizontal: -8,
  },
  photoBox: {
    width: '33.33%',
    aspectRatio: 1,
    padding: 8,
  },
  photo: {
    width: '100%',
    height: '100%',
    borderRadius: 8,
  },
  addPhotoBox: {
    width: '100%',
    height: '100%',
    borderWidth: 2,
    borderColor: '#DDDDDD',
    borderStyle: 'dashed',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  addPhotoText: {
    fontSize: 32,
    color: '#AAAAAA',
  },
  instagramContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    height: 50,
    paddingHorizontal: 12,
    marginBottom: 8,
  },
  instagramAt: {
    fontSize: 16,
    color: '#666666',
    marginRight: 4,
  },
  instagramInput: {
    flex: 1,
    fontSize: 16,
  },
  infoText: {
    fontSize: 12,
    color: '#888888',
    marginBottom: 16,
  },
  footer: {
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
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

export default ProfileCreationScreen;