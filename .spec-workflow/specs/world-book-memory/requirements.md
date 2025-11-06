# Requirements Document - World Book & Memory Table Feature

## Introduction

This feature adds comprehensive knowledge management capabilities to RikkaHub Android application, inspired by SillyTavern's World Info and Memory Enhancement systems. The feature enables users to create, manage, and leverage contextual knowledge bases that automatically inject relevant information into AI conversations, significantly enhancing the AI assistant's understanding and response quality.

**World Book** acts as a contextual knowledge system where users can define keyword-triggered information entries that automatically enhance AI responses. **Memory Table** provides a structured, spreadsheet-like interface for managing complex information and relationships that can be dynamically updated during conversations.

## Alignment with Product Vision

This feature directly supports RikkaHub's core mission of being a powerful, intelligent Android LLM chat client by:

1. **Enhanced AI Understanding**: Providing structured context that significantly improves AI response accuracy and relevance
2. **Knowledge Management**: Enabling users to build and maintain personal knowledge bases for specialized use cases
3. **Context Persistence**: Maintaining conversation context across sessions and topics
4. **User Empowerment**: Giving users granular control over what information is presented to AI assistants

The feature aligns with the existing AI provider abstraction architecture and extends the chat experience beyond simple conversational AI to sophisticated knowledge-assisted interaction.

## Requirements

### Requirement 1: World Book Entry Management

**User Story:** As a RikkaHub user, I want to create and manage World Book entries with keywords and content, so that the system can automatically inject relevant information into my AI conversations based on detected keywords.

#### Acceptance Criteria

1. WHEN a user creates a new World Book entry with keywords ["character", "personality"] and content "Alice is a friendly AI assistant who loves coding" THEN the system SHALL store this entry in the local database and make it available for future keyword matching
2. WHEN the user sends a message containing any of the keywords "character" OR "personality" THEN the system SHALL match the corresponding World Book entries and inject their content into the AI prompt context
3. WHEN multiple entries match the same keywords THEN the system SHALL prioritize entries based on their configured priority level and apply them in the correct sequence
4. IF a World Book entry is marked as "constant" THEN the system SHALL always inject its content regardless of keyword matching
5. IF a World Book entry is disabled THEN the system SHALL ignore it during keyword matching and content injection
6. WHEN editing an existing World Book entry THEN the system SHALL preserve all metadata including creation timestamp, modification history, and keyword configurations

#### Extended Requirements

**Keyword Management:**
- Users can define primary keywords (key) and secondary keywords (keysecondary) for each entry
- Support for both exact text matching and regular expression matching patterns
- AND/OR logic combination for complex keyword matching scenarios
- Each entry maintains an order/priority value for controlling injection sequence

**Content Injection:**
- Configurable injection positions (beginning, end, or custom)
- Automatic character limit enforcement to prevent context overflow
- Support for recursive scanning where entry content can trigger additional entries
- Exclusion mechanisms to prevent circular references in recursive scenarios

**Enhanced Entry Features:**
- Title field for human-readable identification of entries
- Comment field for developer/organizer notes
- Selective matching option to control when entries are applied
- Disabled state toggle for temporary deactivation without deletion

### Requirement 2: Memory Table Management

**User Story:** As a RikkaHub user, I want to create and manage structured tables that can store and display complex information like character relationships, story elements, or project data, so that I can maintain organized knowledge that adapts to my conversation needs.

#### Acceptance Criteria

1. WHEN a user creates a new Memory Table named "Character Database" with columns ["Name", "Age", "Personality", "Role"] THEN the system SHALL create a table structure in the database and display it in a table interface
2. WHEN a user adds a row to the table with data {"Name": "Alice", "Age": "25", "Personality": "Friendly", "Role": "Main Character"} THEN the system SHALL store this row data and display it in the table view
3. WHEN editing a cell in an existing table row THEN the system SHALL immediately save the changes to the database and update the display
4. IF a Memory Table is associated with a specific AI assistant THEN the system SHALL ensure that only that assistant can view and modify the table data
5. WHEN deleting a Memory Table THEN the system SHALL remove all associated row data and table structure from the database
6. WHEN importing table data from external sources THEN the system SHALL parse the data format and create appropriate table structures while preserving data integrity

#### Extended Requirements

**Table Operations:**
- Full CRUD operations for both tables and individual rows
- Support for dynamic column addition and removal
- Row reordering and sorting capabilities
- Search and filtering functionality across table content

**Data Management:**
- Template system for creating commonly used table structures
- Export functionality to common formats (CSV, JSON)
- Import functionality with data validation and error handling
- Version history tracking for table modifications

**Integration Features:**
- Linked tables where rows in one table can reference rows in another
- Dynamic content injection where table data can be referenced by World Book entries
- Context-aware suggestions based on current conversation content

### Requirement 3: Keyword Matching Engine

**User Story:** As the RikkaHub system, I need to efficiently match conversation content against World Book entries using configurable matching strategies, so that relevant information can be automatically injected into AI responses.

#### Acceptance Criteria

1. WHEN processing user messages THEN the system SHALL extract all keywords from configured World Book entries and perform matching against the message content
2. IF an entry uses regular expression matching THEN the system SHALL compile and execute the regex pattern against the message text
3. WHEN multiple entries match with different priorities THEN the system SHALL sort them by priority order before content injection
4. IF recursive scanning is enabled THEN the system SHALL process injected content to identify additional trigger keywords
5. WHEN maximum recursion depth is reached THEN the system SHALL stop recursive processing to prevent infinite loops
6. IF an entry is excluded from recursion THEN the system SHALL not scan its injected content for additional triggers

#### Performance Requirements

1. WHEN matching 100+ World Book entries against a message THEN the system SHALL complete matching within 500ms on a typical Android device
2. IF keyword cache is available THEN the system SHALL use cached matching results for improved performance
3. WHEN no entries match THEN the system SHALL return an empty match list without errors or delays

### Requirement 4: Context Injection System

**User Story:** As the RikkaHub system, I need to intelligently inject matched World Book content into AI conversation contexts, so that AI assistants have access to relevant background information during response generation.

#### Acceptance Criteria

1. WHEN World Book entries are matched THEN the system SHALL construct an enhanced prompt that includes the relevant content in the configured injection position
2. IF multiple entries are matched THEN the system SHALL combine their content according to priority order and configured character limits
3. WHEN injecting content at the beginning THEN the system SHALL place the information before the user's actual message
4. WHEN injecting content at the end THEN the system SHALL append the information after the AI's generated response
5. IF custom injection position is configured THEN the system SHALL use a placeholder system to insert content at the specified location
6. WHEN character limits are exceeded THEN the system SHALL truncate content intelligently while preserving the most important information

#### Context Management

1. WHEN session starts THEN the system SHALL initialize clean context state for World Book and Memory Table data
2. WHEN conversation ends THEN the system SHALL persist any modified table data to maintain continuity
3. IF AI provider is changed THEN the system SHALL ensure context injection format adapts to the new provider's requirements

### Requirement 5: User Interface Components

**User Story:** As a RikkaHub user, I want intuitive interfaces for managing World Book entries and Memory Tables, so that I can efficiently create, edit, and organize my knowledge bases without technical complexity.

#### Acceptance Criteria

1. WHEN accessing World Book management THEN the system SHALL display a searchable list of all entries with keyword summary and status indicators
2. WHEN creating a new World Book entry THEN the system SHALL provide a form with fields for title, keywords, content, and configuration options
3. WHEN editing an existing entry THEN the system SHALL load current values and allow real-time preview of keyword matching
4. WHEN accessing Memory Table management THEN the system SHALL display a grid-like interface that resembles a spreadsheet application
5. WHEN performing table operations THEN the system SHALL provide context menus and keyboard shortcuts for efficient navigation
6. WHEN testing World Book entries THEN the system SHALL simulate keyword matching with sample text to show potential injection results

#### UI/UX Requirements

1. WHEN viewing World Book entries THEN users SHALL see color-coded priority indicators and keyword match statistics
2. WHEN working with Memory Tables THEN users SHALL be able to resize columns and reorder columns by drag-and-drop
3. IF device is in portrait mode THEN the system SHALL provide responsive layouts that adapt to smaller screens
4. WHEN making changes to tables THEN the system SHALL provide undo/redo functionality with visual feedback

### Requirement 6: Data Persistence and Integration

**User Story:** As a RikkaHub user, I want my World Book entries and Memory Tables to persist across app sessions and be backed up with other app data, so that my knowledge bases remain available and secure.

#### Acceptance Criteria

1. WHEN World Book entries are created or modified THEN the system SHALL immediately save changes to the local Room database
2. WHEN Memory Tables are updated THEN the system SHALL maintain data consistency across all related tables and indexes
3. IF app data is backed up THEN the system SHALL include World Book and Memory Table data in the backup process
4. IF app data is restored THEN the system SHALL validate and restore all World Book and Memory Table structures
5. WHEN migrating to a new app version THEN the system SHALL ensure backward compatibility with existing data structures
6. WHEN deleting app data THEN the system SHALL provide options to export World Book and Memory Table data before deletion

#### Integration Requirements

1. WHEN integrating with existing chat functionality THEN the system SHALL not disrupt current conversation flows or UI patterns
2. WHEN new AI assistants are created THEN they SHALL have isolated World Book and Memory Table contexts by default
3. IF sharing data between assistants is needed THEN users SHALL have explicit control over data sharing permissions
4. WHEN exporting conversation history THEN the system SHALL optionally include the World Book context that was active during those conversations

## Non-Functional Requirements

### Code Architecture and Modularity

- **Single Responsibility Principle**: Each database entity, DAO, and service class shall have a single, well-defined purpose
- **Modular Design**: World Book and Memory Table components shall be isolated from core chat functionality but easily integrable
- **Repository Pattern**: All data access shall use repository classes to abstract database operations from UI components
- **Dependency Injection**: All services shall be injectable through Koin DI container following existing patterns
- **Clear Interfaces**: Define contracts between data layer, business logic, and presentation layers
- **Testability**: Core business logic shall be testable without Android framework dependencies

### Performance

- **Database Performance**: Keyword matching queries shall complete within 100ms for typical datasets
- **Memory Usage**: World Book and Memory Table features shall not increase app memory footprint by more than 10%
- **UI Responsiveness**: All table operations and entry editing shall maintain 60fps UI rendering
- **Background Processing**: Keyword matching shall occur on background threads to prevent UI blocking
- **Caching**: Frequently accessed entries and table data shall be cached in memory for improved response times

### Security

- **Data Locality**: All World Book and Memory Table data shall be stored locally and not transmitted to external services
- **Access Control**: Each assistant's data shall be isolated and accessible only to the owning assistant
- **Input Validation**: All user inputs shall be validated to prevent injection attacks and malformed data
- **Data Sanitization**: Content display shall be sanitized to prevent XSS-like attacks in UI components
- **Backup Security**: Exported data shall be encrypted if sensitive information is included

### Reliability

- **Data Integrity**: Database transactions shall ensure atomic operations for related data updates
- **Error Recovery**: Graceful handling of database corruption with recovery mechanisms
- **Validation**: All database operations shall include data validation and constraint checking
- **Backup Integration**: World Book and Memory Table data shall be included in app's backup/restore functionality
- **Crash Recovery**: UI state shall be restored after app crashes without data loss

### Usability

- **Intuitive Interface**: All UI patterns shall follow Material Design 3 guidelines consistent with the app
- **Accessibility**: All interfaces shall support TalkBack and other accessibility features
- **Keyboard Navigation**: Tables shall support full keyboard navigation for power users
- **Gesture Support**: Common gestures like swipe to edit/delete shall be supported
- **Visual Feedback**: All operations shall provide immediate visual feedback to confirm user actions
- **Progressive Disclosure**: Advanced features shall be discoverable but not overwhelming for basic users
