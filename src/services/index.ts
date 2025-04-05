// src/services/index.ts
import {AuthService, UserService, MatchService} from './types';
import {mockAuthService} from './mock/mockAuth';
import {mockUserService} from './mock/mockUser';
import {mockMatchService} from './mock/mockMatch';
import {realAuthService} from './realAuth';

// This will be used to determine which implementation to use
let useMockServices = false;

// Service factory functions
export function getAuthService(): AuthService {
  return useMockServices ? mockAuthService : realAuthService;
}

export function getUserService(): UserService {
  return mockUserService;
}

export function getMatchService(): MatchService {
  return mockMatchService;
}

// This will be useful for toggling between mock and real services
export function setUseMockServices(useMock: boolean): void {
  useMockServices = useMock;
}
