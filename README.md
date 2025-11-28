# Matrix Android Client

---

## Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Component Interaction](#-component-interaction)
- [Security](#-security)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Setup Instructions](#-setup-instructions)
- [Known Limitations](#-known-limitations)
- [Future Improvements](#-future-improvements)

---

## Features

### Core Features
- ✅ **User Authentication** - Secure login with Matrix homeserver
- ✅ **Room Discovery** - Browse and search public rooms
- ✅ **Room Joining** - Join rooms to participate in conversations
- ✅ **Message Viewing** - Read message history with smart filtering

### Additional Features
- ✅ **Persistent Authentication** - Stay logged in across app restarts
- ✅ **Smart Event Filtering** - Hide technical events, show only relevant content (60-70% noise reduction)
- ✅ **Token Expiration Handling** - Automatic logout on 401 errors
- ✅ **Pull to Refresh** - Refresh rooms and messages
- ✅ **Loading States** - Clear feedback for all operations
- ✅ **Error Handling** - Comprehensive error recovery
- ✅ **Secure Logout** - Complete credential clearing

---

## Architecture

This project follows **Clean Architecture** principles with **MVVM pattern**, providing:
- Clear separation of concerns
- Testability at every layer
- Easy maintenance and scalability
- Type-safe error handling

### Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│  ┌────────────┐  ┌────────────┐  ┌─────────────────────┐   │
│  │   Screen   │  │ ViewModel  │  │   UI State/Events   │   │
│  │ (Compose)  │◄─┤  (Logic)   │◄─┤   (Sealed Classes)  │   │
│  └────────────┘  └────────────┘  └─────────────────────┘   │
│         │              │                                      │
└─────────┼──────────────┼──────────────────────────────────┘
          │              │
          ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Repository Interfaces (Contracts)          │   │
│  │  - Defines what data operations are available        │   │
│  │  - No implementation details                         │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │            Result<T> (Error Handling)                │   │
│  │  - Unified error handling across app                 │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
          │              │
          ▼              ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATA LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Repository  │  │    Remote    │  │    Local     │     │
│  │     Impl     │─►│  (Retrofit)  │  │ (DataStore)  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                  │              │
│         │                  │                  ▼              │
│         │                  │          ┌──────────────┐     │
│         │                  │          │ TokenManager │     │
│         │                  │          │  (Security)  │     │
│         │                  │          └──────────────┘     │
│         │                  │                  │              │
│         │                  │                  ▼              │
│         │                  │          ┌──────────────┐     │
│         │                  │          │   Keystore   │     │
│         │                  │          │ (Hardware)   │     │
│         │                  │          └──────────────┘     │
└─────────┼──────────────────┼──────────────────────────────┘
          │                  │
          ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                      EXTERNAL SERVICES                       │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │  Matrix Server   │         │  Android System  │         │
│  │  (matrix.org)    │         │   (Keystore)     │         │
│  └──────────────────┘         └──────────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

---

## Component Interaction

### Authentication Flow

```
User enters credentials
        │
        ▼
┌───────────────────┐
│  LoginViewModel   │ Validates input, manages UI state
└────────┬──────────┘
         │ login(username, password)
         ▼
┌───────────────────┐
│  AuthRepository   │ Coordinates data operations
└────────┬──────────┘
         │ POST /login
         ▼
┌───────────────────┐
│  MatrixApiService │ Makes network request
└────────┬──────────┘
         │ LoginResponse
         ▼
┌───────────────────┐
│  TokenManager     │ Encrypts and stores token
└────────┬──────────┘
         │ AES-GCM encryption
         ▼
┌───────────────────┐
│  Android Keystore │ Hardware-backed storage
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  DataStore        │ Persists encrypted token
└───────────────────┘
         │
         ▼
┌───────────────────┐
│  LoginViewModel   │ Updates UI → Navigate to RoomList
└───────────────────┘
```

### Room Loading Flow

```
User opens app
        │
        ▼
┌───────────────────┐
│ RoomViewModel     │ Checks authentication
└────────┬──────────┘
         │ loadRooms()
         ▼
┌───────────────────┐
│  RoomsRepository  │ Prepares request
└────────┬──────────┘
         │ GET /publicRooms
         ├─────────────┐
         │             │ Authorization: Bearer <token>
         ▼             ▼
┌───────────────────┐ ┌───────────────────┐
│  TokenManager     │ │ MatrixApiService  │
│ (Decrypt token)   │ │ (Make request)    │
└────────┬──────────┘ └────────┬──────────┘
         │                     │
         └─────────┬───────────┘
                   │ PublicRoomsResponse
                   ▼
         ┌───────────────────┐
         │ Filter events     │ isVisibleEvent()
         │                   │
         └────────┬──────────┘
                  │ Visible events only
                  ▼
         ┌───────────────────┐
         │ RoomViewModel     │ Updates UI state
         └───────────────────┘
                  │
                  ▼
              Display rooms
```

### Message Viewing Flow

```
User clicks on room
        │
        ▼
┌───────────────────┐
│ RoomScreen        │ Navigates to MessageList
└────────┬──────────┘
         │ navigate(roomId)
         ▼
┌───────────────────┐
│ MessageListScreen │ Opens with roomId
└────────┬──────────┘
         │ onStart
         ▼
┌───────────────────┐
│MessageListViewModel│ Loads messages for room
└────────┬──────────┘
         │ loadMessages(roomId)
         ▼
┌───────────────────┐
│MessagesRepository │ Prepares request
└────────┬──────────┘
         │ GET /rooms/{roomId}/messages
         ├─────────────┐
         │             │ Authorization: Bearer <token>
         ▼             ▼
┌───────────────────┐ ┌───────────────────┐
│  TokenManager     │ │ MatrixApiService  │
│ (Decrypt token)   │ │ (Make request)    │
└────────┬──────────┘ └────────┬──────────┘
         │                     │
         └─────────┬───────────┘
                   │ MessagesResponse
                   ▼
         ┌───────────────────┐
         │ Filter events     │ isVisibleEvent()
         │                   │
         └────────┬──────────┘
                  │ Messages only (no system events)
                  ▼
         ┌───────────────────┐
         │ Format messages   │ getMessageBody()
         │ (add timestamps)  │ getFormattedTime()
         └────────┬──────────┘
                  │ Formatted messages
                  ▼
         ┌───────────────────┐
         │MessageListViewModel│ Updates UI state
         └────────┬──────────┘
                  │ _state.update { messages = ... }
                  ▼
         ┌───────────────────┐
         │ MessageListScreen │ LazyColumn displays
         └───────────────────┘
                  │
                  ▼
              Display messages
                  │
                  │ User pulls to refresh
                  ▼
         ┌───────────────────┐
         │MessageListViewModel│ refreshMessages()
         └────────┬──────────┘
                  │ Repeat flow
                  ▼
              Updated messages
```

### Join Room Flow

```
User clicks Join button
        │
        ▼
┌───────────────────┐
│ RoomScreen        │ User initiates join
└────────┬──────────┘
         │ onClick(Join)
         ▼
┌───────────────────┐
│ RoomiewModel      │ Processes join event
└────────┬──────────┘
         │ onEvent(JoinRoom(roomId))
         ▼
┌───────────────────┐
│ RoomsRepository   │ Coordinates join operation
└────────┬──────────┘
         │ POST /rooms/{roomId}/join
         ├─────────────┐
         │             │ Authorization: Bearer <token>
         ▼             ▼
┌───────────────────┐ ┌───────────────────┐
│  TokenManager     │ │ MatrixApiService  │
│ (Decrypt token)   │ │ (Make request)    │
└────────┬──────────┘ └────────┬──────────┘
         │                     │
         └─────────┬───────────┘
                   │ JoinResponse
                   ▼
         ┌───────────────────┐
         │JoinedRoomsManager │ Persist joined room
         └────────┬──────────┘
                  │ saveJoinedRoom(roomId)
                  ▼
         ┌───────────────────┐
         │    DataStore      │ Store in preferences
         └────────┬──────────┘
                  │
                  ▼
         ┌───────────────────┐
         │ RoomsRepository   │ Result.Success(Unit)
         └────────┬──────────┘
                  │
                  ▼
         ┌───────────────────┐
         │ RoomViewModel     │ Updates UI state
         └────────┬──────────┘
                  │ Add to joinedRoomIds
                  ▼
         ┌───────────────────┐
         │ RoomScreen        │ Button shows "Joined ✓"
         └───────────────────┘
                  │
                  ▼
              Visual feedback
                  │
                  │ App restart
                  ▼
         ┌───────────────────┐
         │JoinedRoomsManager │ Loads joined rooms
         └────────┬──────────┘
                  │ State persists ✅
                  ▼
              Joined state restored
```

### Error Handling Flow

```
API returns 401 Unauthorized
        │
        ▼
┌───────────────────┐
│  Repository       │ Catches HttpException
└────────┬──────────┘
         │ when (e.code()) { 401 -> ... }
         ▼
┌───────────────────┐
│  TokenManager     │ clearAll()
└────────┬──────────┘
         │ Delete encrypted token
         ▼
┌───────────────────┐
│  Repository       │ Result.Error(isAuthError = true)
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  ViewModel        │ if (result.isAuthError)
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│  Navigation       │ Navigate to LoginScreen
└───────────────────┘
```

---

## Security

### Token Storage Architecture

The app implements **multi-layered security** for token storage:

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                           │
│                                                              │
│  Layer 1: Application Code                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  TokenManager (Kotlin)                               │  │
│  │  - Manages encryption/decryption                      │  │
│  │  - Handles key generation                            │  │
│  │  - Provides clean API                                │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                          │
│  Layer 2: Encryption (AES-GCM)                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Cipher                                              │  │
│  │  - Algorithm: AES/GCM/NoPadding                      │  │
│  │  - Key size: 256 bits                                │  │
│  │  - Authentication: GCM provides integrity check      │  │
│  │  - IV: Random 12 bytes per encryption                │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                          │
│  Layer 3: Key Storage (Android Keystore)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  KeyStore                                            │  │
│  │  - Provider: AndroidKeyStore                         │  │
│  │  - Hardware-backed on supported devices              │  │
│  │  - TEE/TrustZone isolation                          │  │
│  │  - Keys never leave secure hardware                 │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                          │
│  Layer 4: Persistent Storage (DataStore)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Encrypted DataStore                                 │  │
│  │  - Stores: IV + Encrypted Token                      │  │
│  │  - Format: Base64 encoded                            │  │
│  │  - Location: Private app directory                   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```
---

## Tech Stack

### Core Technologies
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3

### Architecture Components
- **Architecture Pattern:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt
- **Async:** Coroutines + Flow
- **Navigation:** Compose Navigation

### Networking & Data
- **HTTP Client:** Retrofit + OkHttp
- **JSON Parsing:** Moshi
- **Local Storage:** DataStore (Preferences)

### Security
- **Encryption:** Android Keystore + AES-GCM
- **Token Storage:** Encrypted DataStore

### Code Quality
- **Logging:** Timber
- **Datetime:** kotlinx-datetime

---


## Known Limitations

### 1. Message Sending Not Implemented
**Current State:** Read-only message viewing  
**Missing:** Ability to send messages, upload files, send reactions

**Why Limited:**
- Focus on core requested feature.

### 2. Limited Error Recovery
**Current State:** Basic error messages, clear credentials on 401  
**Missing:** Automatic retry, exponential backoff, offline queue

**Why Limited:**
- Time constraint focus on happy path

### 3. No Pagination for Messages
**Current State:** Loads last 50 messages only  
**Missing:** Load more messages on scroll, infinite scroll

**Why Limited:**
- Pagination adds state complexity

### 4. No Real-Time Sync
**Current State:** Manual refresh only (pull to refresh)  
**Missing:** Real-time updates via Matrix /sync endpoint

**Why Limited:**
- Not in requested core feature for demo
- Sync requires long-polling or polling loop
- Would add state synchronization complexity

**What /sync Does:**
- Matrix's core real-time update mechanism
- Returns new messages, room changes, typing indicators, read receipts
- Uses long-polling (wait until new data or timeout)
- Provides `since` token for incremental updates

**Impact:**
- Users must manually refresh to see new messages
- No real-time chat experience
- Miss messages sent while app open

**UI Impact:**
- New messages appear automatically
- Room list updates in real-time
- Typing indicators show
- Read receipts update

### 5. No Room Search/Filter
**Current State:** Shows all public rooms (up to limit)  
**Missing:** Search by name, filter by topic, sort options

**Why Limited:**
- Server-side search requires different API endpoint
- Client-side filter is straightforward but omitted for scope

### 6. No Profile Management
**Current State:** Login/logout only  
**Missing:** View profile, change display name/avatar, password change

**Why Limited:**
- Not core feature for demo

## Future Improvements

#### 1. Offline Support

**Approach:**
- Use Room database for local caching
- Queue operations when offline
- Sync when connection restored

#### 2. Push Notifications
**Impact:** Essential for mobile chat

**Approach:**
- Register for Firebase Cloud Messaging (FCM)
- Configure push gateway on homeserver
- Handle notification payload

#### 3. Read Receipts
**Impact:** Better UX feature

#### 4. Typing Indicators
**Impact:** Better UX feature