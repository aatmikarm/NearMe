// src/components/FilterModal.tsx
import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Modal,
  ScrollView
} from 'react-native';
import { useUser } from '../contexts/UserContext';

type FilterModalProps = {
  visible: boolean;
  onClose: () => void;
  onApply: (filters: any) => void;
};

const FilterModal = ({ visible, onClose, onApply }: FilterModalProps) => {
  const { userPreferences } = useUser();
  
  const [distance, setDistance] = useState(userPreferences?.discoveryDistance || 500);
  const [ageMin, setAgeMin] = useState(userPreferences?.ageRangeMin || 18);
  const [ageMax, setAgeMax] = useState(userPreferences?.ageRangeMax || 35);
  const [showMen, setShowMen] = useState(
    userPreferences?.genderPreference?.includes('male') || true
  );
  const [showWomen, setShowWomen] = useState(
    userPreferences?.genderPreference?.includes('female') || true
  );
  const [showOther, setShowOther] = useState(
    userPreferences?.genderPreference?.includes('other') || false
  );
  
  const handleApply = () => {
    const genderPreference = [];
    if (showMen) genderPreference.push('male');
    if (showWomen) genderPreference.push('female');
    if (showOther) genderPreference.push('other');
    
    onApply({
      discoveryDistance: distance,
      ageRangeMin: ageMin,
      ageRangeMax: ageMax,
      genderPreference,
    });
  };

  // Distance increment/decrement
  const incrementDistance = () => {
    const newDistance = Math.min(2000, distance + 100);
    setDistance(newDistance);
  };

  const decrementDistance = () => {
    const newDistance = Math.max(100, distance - 100);
    setDistance(newDistance);
  };

  // Age increment/decrement
  const incrementMinAge = () => {
    const newAge = Math.min(ageMax, ageMin + 1);
    setAgeMin(newAge);
  };

  const decrementMinAge = () => {
    const newAge = Math.max(18, ageMin - 1);
    setAgeMin(newAge);
  };

  const incrementMaxAge = () => {
    const newAge = Math.min(70, ageMax + 1);
    setAgeMax(newAge);
  };

  const decrementMaxAge = () => {
    const newAge = Math.max(ageMin, ageMax - 1);
    setAgeMax(newAge);
  };
  
  return (
    <Modal
      transparent
      visible={visible}
      animationType="slide"
      onRequestClose={onClose}
    >
      <View style={styles.modalContainer}>
        <View style={styles.modalContent}>
          <View style={styles.header}>
            <Text style={styles.headerTitle}>Filters</Text>
            <TouchableOpacity onPress={onClose}>
              <Text style={styles.closeButton}>✕</Text>
            </TouchableOpacity>
          </View>
          
          <ScrollView contentContainerStyle={styles.scrollContent}>
            <View style={styles.filterSection}>
              <Text style={styles.filterLabel}>Maximum Distance</Text>
              <Text style={styles.filterValue}>{distance}m</Text>
              
              <View style={styles.rangeControl}>
                <TouchableOpacity 
                  style={styles.rangeButton}
                  onPress={decrementDistance}
                >
                  <Text style={styles.rangeButtonText}>-</Text>
                </TouchableOpacity>
                
                <View style={styles.rangeBar}>
                  <View 
                    style={[
                      styles.rangeProgress, 
                      { width: `${((distance - 100) / 1900) * 100}%` }
                    ]} 
                  />
                </View>
                
                <TouchableOpacity 
                  style={styles.rangeButton}
                  onPress={incrementDistance}
                >
                  <Text style={styles.rangeButtonText}>+</Text>
                </TouchableOpacity>
              </View>
              
              <View style={styles.rangeLabels}>
                <Text style={styles.rangeLabel}>100m</Text>
                <Text style={styles.rangeLabel}>2km</Text>
              </View>
            </View>
            
            <View style={styles.filterSection}>
              <Text style={styles.filterLabel}>Age Range</Text>
              <Text style={styles.filterValue}>{ageMin} - {ageMax}</Text>
              
              <View style={styles.ageControls}>
                <View style={styles.ageRangeControl}>
                  <Text style={styles.ageRangeLabel}>Min Age</Text>
                  <View style={styles.ageButtonGroup}>
                    <TouchableOpacity
                      style={styles.ageButton}
                      onPress={decrementMinAge}
                    >
                      <Text style={styles.ageButtonText}>-</Text>
                    </TouchableOpacity>
                    
                    <Text style={styles.ageValue}>{ageMin}</Text>
                    
                    <TouchableOpacity
                      style={styles.ageButton}
                      onPress={incrementMinAge}
                    >
                      <Text style={styles.ageButtonText}>+</Text>
                    </TouchableOpacity>
                  </View>
                </View>
                
                <View style={styles.ageRangeControl}>
                  <Text style={styles.ageRangeLabel}>Max Age</Text>
                  <View style={styles.ageButtonGroup}>
                    <TouchableOpacity
                      style={styles.ageButton}
                      onPress={decrementMaxAge}
                    >
                      <Text style={styles.ageButtonText}>-</Text>
                    </TouchableOpacity>
                    
                    <Text style={styles.ageValue}>{ageMax}</Text>
                    
                    <TouchableOpacity
                      style={styles.ageButton}
                      onPress={incrementMaxAge}
                    >
                      <Text style={styles.ageButtonText}>+</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              </View>
            </View>
            
            <View style={styles.filterSection}>
              <Text style={styles.filterLabel}>Show</Text>
              <View style={styles.genderOptions}>
                <TouchableOpacity
                  style={[
                    styles.genderOption,
                    showMen && styles.genderOptionSelected
                  ]}
                  onPress={() => setShowMen(!showMen)}
                >
                  <Text style={[
                    styles.genderOptionText,
                    showMen && styles.genderOptionTextSelected
                  ]}>Men</Text>
                </TouchableOpacity>
                
                <TouchableOpacity
                  style={[
                    styles.genderOption,
                    showWomen && styles.genderOptionSelected
                  ]}
                  onPress={() => setShowWomen(!showWomen)}
                >
                  <Text style={[
                    styles.genderOptionText,
                    showWomen && styles.genderOptionTextSelected
                  ]}>Women</Text>
                </TouchableOpacity>
                
                <TouchableOpacity
                  style={[
                    styles.genderOption,
                    showOther && styles.genderOptionSelected
                  ]}
                  onPress={() => setShowOther(!showOther)}
                >
                  <Text style={[
                    styles.genderOptionText,
                    showOther && styles.genderOptionTextSelected
                  ]}>Other</Text>
                </TouchableOpacity>
              </View>
            </View>
          </ScrollView>
          
          <View style={styles.buttonContainer}>
            <TouchableOpacity 
              style={styles.applyButton}
              onPress={handleApply}
            >
              <Text style={styles.applyButtonText}>Apply Filters</Text>
            </TouchableOpacity>
            
            <TouchableOpacity 
              style={styles.resetButton}
              onPress={() => {
                setDistance(500);
                setAgeMin(18);
                setAgeMax(35);
                setShowMen(true);
                setShowWomen(true);
                setShowOther(false);
              }}
            >
              <Text style={styles.resetButtonText}>Reset to Defaults</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContent: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    maxHeight: '80%',
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
  closeButton: {
    fontSize: 20,
    color: '#666666',
  },
  scrollContent: {
    padding: 16,
  },
  filterSection: {
    marginBottom: 24,
  },
  filterLabel: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
  },
  filterValue: {
    fontSize: 14,
    color: '#666666',
    marginBottom: 12,
  },
  rangeControl: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginVertical: 8,
  },
  rangeButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F5F5F5',
    justifyContent: 'center',
    alignItems: 'center',
  },
  rangeButtonText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#666666',
  },
  rangeBar: {
    flex: 1,
    height: 6,
    backgroundColor: '#DDDDDD',
    borderRadius: 3,
    marginHorizontal: 12,
    overflow: 'hidden',
  },
  rangeProgress: {
    height: '100%',
    backgroundColor: '#FF6B6B',
  },
  rangeLabels: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 4,
  },
  rangeLabel: {
    fontSize: 12,
    color: '#999999',
  },
  ageControls: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  ageRangeControl: {
    width: '48%',
  },
  ageRangeLabel: {
    fontSize: 14,
    color: '#666666',
    marginBottom: 8,
  },
  ageButtonGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F5F5F5',
    borderRadius: 8,
    padding: 8,
  },
  ageButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  ageButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#666666',
  },
  ageValue: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  genderOptions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  genderOption: {
    flex: 1,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: '#DDDDDD',
    borderRadius: 8,
    alignItems: 'center',
    marginHorizontal: 4,
  },
  genderOptionSelected: {
    backgroundColor: '#FF6B6B',
    borderColor: '#FF6B6B',
  },
  genderOptionText: {
    color: '#666666',
  },
  genderOptionTextSelected: {
    color: '#FFFFFF',
    fontWeight: '600',
  },
  buttonContainer: {
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
  },
  applyButton: {
    backgroundColor: '#FF6B6B',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 12,
  },
  applyButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  resetButton: {
    alignItems: 'center',
    padding: 12,
  },
  resetButtonText: {
    color: '#666666',
    fontSize: 14,
  },
});

export default FilterModal;