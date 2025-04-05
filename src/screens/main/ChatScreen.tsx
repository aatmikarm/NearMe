// src/screens/main/ChatScreen.tsx
import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  FlatList,
  SafeAreaView,
  StatusBar,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator
} from 'react-native';
import { useUser } from '../../contexts/UserContext';

type Message = {
  id: string;
  text: string;
  senderId: string;
  createdAt: Date;
};

type ChatScreenProps = {
  route: {
    params: {
      matchId: string;
      userName: string;
      userPhoto: string;
    }
  };
  navigation: any;
};

const ChatScreen = ({ route, navigation }: ChatScreenProps) => {
  const { matchId, userName, userPhoto } = route.params;
  const { userProfile } = useUser();
  
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  
  const flatListRef = useRef<FlatList>(null);
  
  // Load mock messages for demo
  useEffect(() => {
    // Simulate API call delay
    setTimeout(() => {
      const mockMessages = generateMockMessages();
      setMessages(mockMessages);
      setIsLoading(false);
    }, 1000);
  }, []);
  
  // Scroll to bottom when messages change
  useEffect(() => {
    if (messages.length > 0 && flatListRef.current) {
      flatListRef.current.scrollToEnd({ animated: true });
    }
  }, [messages]);
  
  // Generate some mock messages for demo
  const generateMockMessages = (): Message[] => {
    const mockMessages: Message[] = [];
    const currentUserId = userProfile?.uid || 'current-user';
    const otherUserId = 'other-user';
    
    // Create messages from last 3 days
    const now = new Date();
    
    // Day 1
    mockMessages.push({
      id: '1',
      text: 'Hi there!',
      senderId: otherUserId,
      createdAt: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000)
    });
    
    mockMessages.push({
      id: '2',
      text: 'Hey! Nice to match with you',
      senderId: currentUserId,
      createdAt: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000 + 2 * 60 * 1000)
    });
    
    // Day 2
    mockMessages.push({
      id: '3',
      text: 'What kind of places do you like to visit?',
      senderId: otherUserId,
      createdAt: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000)
    });
    
    mockMessages.push({
      id: '4',
      text: 'I love coffee shops and art galleries. How about you?',
      senderId: currentUserId,
      createdAt: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000 + 5 * 60 * 1000)
    });
    
    // Today
    mockMessages.push({
      id: '5',
      text: 'Me too! There\'s a new gallery opening this weekend. Would you like to go?',
      senderId: otherUserId,
      createdAt: new Date(now.getTime() - 30 * 60 * 1000)
    });
    
    return mockMessages;
  };
  
  // Send a message
  const handleSend = () => {
    if (inputText.trim() === '') return;
    
    const newMessage: Message = {
      id: Date.now().toString(),
      text: inputText.trim(),
      senderId: userProfile?.uid || 'current-user',
      createdAt: new Date()
    };
    
    setMessages([...messages, newMessage]);
    setInputText('');
  };
  
  // Format date to show differently based on recency
  const formatMessageDate = (date: Date) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    
    if (messageDate.getTime() === today.getTime()) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else if (messageDate.getTime() === yesterday.getTime()) {
      return 'Yesterday ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' }) + 
        ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  };
  
  // Group messages by date
  const groupedMessages = () => {
    const groupedData: { title: string; data: Message[] }[] = [];
    let currentDate = '';
    
    messages.forEach(message => {
      const date = new Date(message.createdAt);
      const dateString = date.toLocaleDateString();
      
      if (dateString !== currentDate) {
        currentDate = dateString;
        groupedData.push({
          title: dateString,
          data: [message]
        });
      } else {
        groupedData[groupedData.length - 1].data.push(message);
      }
    });
    
    return groupedData;
  };
  
  // Render a single message bubble
  const renderMessage = ({ item }: { item: Message }) => {
    const isCurrentUser = item.senderId === (userProfile?.uid || 'current-user');
    
    return (
      <View style={[
        styles.messageBubble,
        isCurrentUser ? styles.currentUserBubble : styles.otherUserBubble
      ]}>
        <Text style={[
          styles.messageText,
          isCurrentUser ? styles.currentUserText : styles.otherUserText
        ]}>
          {item.text}
        </Text>
        <Text style={[
          styles.messageTime,
          isCurrentUser ? styles.currentUserTime : styles.otherUserTime
        ]}>
          {formatMessageDate(new Date(item.createdAt))}
        </Text>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      
      <View style={styles.header}>
        <TouchableOpacity 
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>{userName}</Text>
        <View style={{ width: 40 }} />
      </View>
      
      <KeyboardAvoidingView
        style={styles.content}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 0}
      >
        {isLoading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator color="#FF6B6B" size="large" />
            <Text style={styles.loadingText}>Loading conversation...</Text>
          </View>
        ) : (
          <FlatList
            ref={flatListRef}
            data={messages}
            renderItem={renderMessage}
            keyExtractor={item => item.id}
            contentContainerStyle={styles.messageList}
          />
        )}
        
        <View style={styles.inputContainer}>
          <TextInput
            style={styles.input}
            value={inputText}
            onChangeText={setInputText}
            placeholder="Type a message..."
            multiline
          />
          <TouchableOpacity 
            style={[
              styles.sendButton,
              !inputText.trim() && styles.sendButtonDisabled
            ]}
            onPress={handleSend}
            disabled={!inputText.trim()}
          >
            <Text style={styles.sendButtonText}>→</Text>
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
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  backButton: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  backButtonText: {
    fontSize: 24,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  content: {
    flex: 1,
    justifyContent: 'space-between',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    color: '#666666',
  },
  messageList: {
    padding: 16,
  },
  messageBubble: {
    maxWidth: '80%',
    padding: 12,
    borderRadius: 18,
    marginBottom: 8,
  },
  currentUserBubble: {
    backgroundColor: '#FF6B6B',
    alignSelf: 'flex-end',
    borderTopRightRadius: 4,
  },
  otherUserBubble: {
    backgroundColor: '#F0F0F0',
    alignSelf: 'flex-start',
    borderTopLeftRadius: 4,
  },
  messageText: {
    fontSize: 16,
  },
  currentUserText: {
    color: '#FFFFFF',
  },
  otherUserText: {
    color: '#333333',
  },
  messageTime: {
    fontSize: 10,
    marginTop: 4,
    alignSelf: 'flex-end',
  },
  currentUserTime: {
    color: 'rgba(255, 255, 255, 0.7)',
  },
  otherUserTime: {
    color: '#999999',
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 12,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
    alignItems: 'center',
  },
  input: {
    flex: 1,
    backgroundColor: '#F0F0F0',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingTop: 10,
    paddingBottom: 10,
    maxHeight: 120,
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#FF6B6B',
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: 8,
  },
  sendButtonDisabled: {
    backgroundColor: '#FFADAD',
  },
  sendButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
  },
});

export default ChatScreen;