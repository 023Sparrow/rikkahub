# Requirements Document: World Book and Memory Table Integration

## Introduction

This feature completes the integration of World Book (similar to SillyTavern World Info) and Memory Table (similar to memory enhancement) functionality into the RikkaHub Android application. These features provide AI-powered contextual knowledge injection and structured data management capabilities, enhancing the conversational AI experience by automatically matching and injecting relevant information based on conversation context.

The World Book feature allows users to create searchable knowledge entries with keywords that automatically trigger context injection during AI conversations. The Memory Table feature provides spreadsheet-like structured data storage that can be queried by AI assistants.

## Alignment with Product Vision

This feature aligns with RikkaHub's vision of providing a sophisticated AI chat platform by:
- Enhancing AI responses with contextual knowledge injection
- Providing structured data management for character/assistant information
- Offering advanced customization options for different AI personalities
- Supporting multimodal and complex AI use cases

## Requirements

### Requirement 1: Dependency Injection Integration

**User Story:** As a developer, I want to integrate WorldBookMatcher and WorldBookInjector services into the application's dependency injection system, so that they can be properly managed and accessed throughout the application.

#### Acceptance Criteria

1. WHEN ChatService is instantiated THEN it SHALL receive WorldBookMatcher and WorldBookInjector via constructor injection
2. IF AppModule.kt is updated with new service registrations THEN Koin SHALL successfully resolve these dependencies
3. WHEN WorldBookRepository is accessed THEN it SHALL provide active world book entries for the current assistant
4. IF any DI configuration is missing THEN the application SHALL fail fast at startup with clear error messages

### Requirement 2: ChatService Integration with World Book

**User Story:** As a user, I want world book entries to be automatically matched and injected into my AI conversations based on keywords, so that I receive more contextual and accurate responses.

#### Acceptance Criteria

1. WHEN a message is sent to the AI AND the assistant has enableWorldBook=true THEN the system SHALL match active world book entries using the current message and recent conversation history
2. IF world book entries are matched THEN they SHALL be injected as SYSTEM role messages at the position specified by injectionPosition (0=beginning, 1=end)
3. WHEN multiple entries match THEN they SHALL be sorted by priority (higher first) and deduplicated
4. IF recursive matching is enabled THEN matched entry content SHALL trigger additional matches up to the configured depth
5. WHEN context size limit is reached THEN excess content SHALL be truncated with appropriate markers
6. IF world book is disabled for an assistant THEN no matching or injection SHALL occur

### Requirement 3: World Book Management UI (Jetpack Compose)

**User Story:** As a user, I want to manage world book entries through an intuitive UI, so that I can easily create, edit, and organize my knowledge base.

#### Acceptance Criteria

1. WHEN the user navigates to World Book management THEN a list of all entries for the current assistant SHALL be displayed
2. WHEN the user taps the add button THEN a world book editor SHALL open with empty fields
3. WHEN the user selects an entry THEN the editor SHALL populate with existing values for editing
4. IF the user enters keywords and content THEN the entry SHALL be saved with proper validation
5. WHEN the user searches for entries THEN results SHALL filter by title, keywords, or content
6. IF the user toggles an entry's enabled status THEN the change SHALL persist immediately
7. WHEN the user deletes an entry THEN a confirmation dialog SHALL appear before deletion

### Requirement 4: World Book Editor with Advanced Features

**User Story:** As a power user, I want to configure advanced world book settings like regex matching, recursion depth, and priority, so that I can fine-tune the knowledge injection behavior.

#### Acceptance Criteria

1. WHEN editing an entry THEN the user SHALL be able to add/remove multiple keywords
2. IF useRegex is enabled THEN keywords SHALL be treated as regular expressions during matching
3. WHEN secondary keywords are configured AND isSelective=true THEN ALL secondary keywords MUST match (AND logic)
4. WHEN secondary keywords are configured AND isSelective=false THEN ANY secondary keyword MAY match (OR logic)
5. IF isConstant is enabled THEN the entry SHALL always be injected regardless of keywords
6. WHEN priority is set THEN entries SHALL be ordered by this value during injection
7. IF excludeRecursion is enabled THEN this entry's content SHALL NOT trigger recursive matches
8. WHEN injectionPosition is set THEN the formatted content SHALL be inserted at the specified location

### Requirement 5: Memory Table Management UI

**User Story:** As a user, I want to create and manage structured data tables, so that I can organize character information, story elements, or any tabular data for AI reference.

#### Acceptance Criteria

1. WHEN the user navigates to Memory Table management THEN a list of all tables for the current assistant SHALL be displayed
2. WHEN the user creates a new table THEN they SHALL be able to define table name, description, and column headers
3. WHEN editing a table THEN the user SHALL be able to add/remove rows and edit cell values
4. IF the user changes column headers THEN existing row data SHALL be preserved or migrated appropriately
5. WHEN the user deletes a table THEN all associated rows SHALL also be deleted
6. IF enableMemoryTable is true for an assistant THEN the table SHALL be available as a queryable resource
7. WHEN searching tables THEN results SHALL filter by table name or description

### Requirement 6: Chat Interface Knowledge Base Access

**User Story:** As a user, I want quick access to world book and memory table management from the chat interface, so that I can efficiently manage my knowledge base while conversing.

#### Acceptance Criteria

1. WHEN the user views the chat interface THEN a knowledge base button SHALL be visible in the top app bar
2. WHEN the user taps the knowledge base button THEN a navigation menu SHALL appear with options for World Book and Memory Table
3. IF the user selects World Book management THEN they SHALL be taken to the world book list filtered for the current assistant
4. IF the user selects Memory Table management THEN they SHALL be taken to the memory table list filtered for the current assistant
5. WHEN navigating back from knowledge base screens THEN the user SHALL return to the chat interface

### Requirement 7: Memory Table Query Service

**User Story:** As an AI assistant, I want to access memory table data through tool calls, so that I can provide more accurate and contextually relevant information based on structured data.

#### Acceptance Criteria

1. WHEN an AI assistant needs table data THEN it SHALL be able to query tables by keywords
2. IF a query matches table rows THEN the results SHALL be formatted as structured data (Markdown or JSON)
3. WHEN exporting a table THEN the user SHALL be able to choose CSV or Markdown format
4. IF importing table data THEN the system SHALL validate CSV format and create/update tables accordingly
5. WHEN table data is queried THEN the response SHALL include column headers and matching row data
6. IF no matches are found THEN an appropriate empty result SHALL be returned

### Requirement 8: Assistant Model Integration

**User Story:** As a user, I want to enable/disable world book and memory table features per assistant, so that different AI personalities can have different knowledge management capabilities.

#### Acceptance Criteria

1. WHEN editing an assistant THEN the user SHALL see toggle switches for enableWorldBook and enableMemoryTable
2. IF enableWorldBook is true THEN world book matching SHALL be active for this assistant
3. IF enableMemoryTable is true THEN memory table querying SHALL be available for this assistant
4. WHEN world book settings are configured THEN the system SHALL apply context size, history message count, and format style preferences
5. IF settings are changed THEN they SHALL be immediately saved to the assistant configuration

## Non-Functional Requirements

### Code Architecture and Modularity
- **Single Responsibility Principle**: Each service (WorldBookMatcher, WorldBookInjector, MemoryTableQueryService) SHALL have a single, well-defined purpose
- **Modular Design**: UI components, ViewModels, and services SHALL be isolated and reusable across the application
- **Dependency Management**: Use Koin for DI, minimizing interdependencies between modules
- **Clear Interfaces**: Define clean contracts between UI, ViewModels, and data layers following MVVM pattern
- **Jetpack Compose Integration**: All UI SHALL use Compose, replacing any legacy Android View implementations

### Performance
- World book matching SHALL complete within 100ms for typical workloads (≤50 entries)
- Memory table queries SHALL return results within 50ms for tables with ≤1000 rows
- UI interactions SHALL maintain 60fps during list scrolling and editing
- Database queries SHALL use appropriate indexes and LIMIT clauses for large datasets

### Security
- World book content SHALL be sanitized before injection to prevent prompt injection attacks
- User data in tables SHALL be encrypted at rest using Android Keystore
- No sensitive data (API keys, passwords) SHALL be stored in world book or memory table entries
- All data operations SHALL follow principle of least privilege

### Reliability
- Database operations SHALL use transactions to ensure data consistency
- Failed world book injection SHALL not break the chat flow (graceful degradation)
- All async operations SHALL have proper error handling and user feedback
- Data migrations SHALL preserve existing data and maintain compatibility

### Usability
- World book editor SHALL support keyword tag-based input with autocomplete
- Memory table editor SHALL provide Excel-like editing experience
- All forms SHALL have inline validation with clear error messages
- Keyboard shortcuts SHALL be supported for power users (Ctrl+S for save)
- Offline capability: All features SHALL work without internet connectivity

## Technical Context

Based on existing implementation:
- Database layer (WorldBookEntry, MemoryTable, MemoryTableRow) is complete
- Repository layer (WorldBookRepository, MemoryTableRepository) is complete
- Core services (WorldBookMatcher, WorldBookInjector) are implemented and tested
- Assistant model includes all necessary configuration fields
- Legacy UI exists but is commented out (uses Android View, needs Compose rewrite)

## Open Questions

1. Should world book injection be visible to users in the chat history or hidden?
2. What's the maximum number of world book entries an assistant should support?
3. Should memory tables support collaboration/multiple users or single-user only?
4. Do we need import/export functionality for world book entries (JSON format)?
5. Should world book matching consider message metadata (timestamps, attachments)?
