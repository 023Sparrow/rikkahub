# Tasks Document: World Book and Memory Table Integration

- [x] 1. Configure dependency injection in AppModule
  - File: app/src/main/java/me/rerere/rikkahub/di/AppModule.kt
  - Add WorldBookMatcher and WorldBookInjector as singletons
  - Purpose: Enable service injection throughout application
  - _Leverage: existing AppModule.kt pattern, WorldBookMatcher service, WorldBookInjector service
  - _Requirements: 1.1
  - _Prompt: Role: Android/Kotlin Developer specializing in dependency injection and Koin | Task: Configure dependency injection in AppModule.kt following requirement 1.1, registering WorldBookMatcher and WorldBookInjector as singletons using existing patterns | Restrictions: Must follow existing Koin configuration patterns, do not modify repository DI configuration, maintain singleton lifecycle | Success: Services are properly registered and resolvable, no circular dependencies, follows existing architecture patterns

- [x] 2. Integrate world book functionality in ChatService
  - File: app/src/main/java/me/rerere/rikkahub/service/ChatService.kt
  - Add constructor parameters for worldBookRepository, worldBookMatcher, worldBookInjector
  - Modify handleMessageComplete() to fetch, match, and inject world book entries
  - Pass injected messages to generationHandler.generateText()
  - Purpose: Enable automatic world book context injection during chat
  - _Leverage: existing ChatService.kt pattern, WorldBookRepository, WorldBookMatcher, WorldBookInjector, Assistant model
  - _Requirements: 2.1, 2.2
  - _Prompt: Role: Android Developer with expertise in MVVM architecture and chat systems | Task: Integrate world book functionality in ChatService following requirements 2.1 and 2.2, modifying handleMessageComplete() method to fetch active entries, match against context, and inject into messages before generation | Restrictions: Must not break existing chat functionality, maintain async/await patterns, ensure proper error handling with graceful degradation | Success: World book matching and injection works correctly, performance is maintained, errors do not break chat flow

- [x] 3. Implement MemoryTableQueryService
  - File: app/src/main/java/me/rerere/rikkahub/service/MemoryTableQueryService.kt
  - Implement searchTables() method for keyword-based search
  - Add formatTableAsMarkdown() for AI consumption
  - Add exportTableAsCSV() and importTableFromCSV() methods
  - Purpose: Provide structured data querying and export/import functionality
  - _Leverage: MemoryTableRepository, MemoryTable entity, MemoryTableRow entity, CSV utilities, Markdown utilities
  - _Requirements: 7.1, 7.2, 7.3
  - _Prompt: Role: Android/Kotlin Developer with expertise in data processing and formatting | Task: Implement MemoryTableQueryService following requirements 7.1, 7.2, and 7.3, creating methods for searching, formatting as Markdown, and exporting/importing CSV data | Restrictions: Must validate all inputs, handle large datasets efficiently, maintain data integrity during import/export | Success: All query methods work correctly, formatting produces valid output, import/export handles validation errors gracefully

- [x] 4. Register MemoryTableQueryService in AppModule
  - File: app/src/main/java/me/rerere/rikkahub/di/AppModule.kt
  - Add MemoryTableQueryService to DI configuration
  - Purpose: Enable service injection for memory table queries
  - _Leverage: existing AppModule.kt pattern, MemoryTableQueryService
  - _Requirements: 7.1
  - _Prompt: Role: Android/Kotlin Developer specializing in dependency injection | Task: Register MemoryTableQueryService in AppModule following requirement 7.1, configuring as singleton using existing patterns | Restrictions: Must follow existing Koin patterns, no circular dependencies, proper lifecycle management | Success: Service is properly registered and resolvable, no circular dependencies, follows architecture patterns

- [x] 5. Update ChatService constructor and dependency injection
  - File: app/src/main/java/me/rerere/rikkahub/di/AppModule.kt
  - Update ChatService constructor to include new dependencies
  - Add MemoryTableQueryService to ChatService if needed for AI tools
  - Purpose: Ensure ChatService has all required dependencies
  - _Leverage: existing ChatService constructor, AppModule DI configuration
  - _Requirements: 2.1, 7.1
  - _Prompt: Role: Android/Kotlin Developer with dependency injection expertise | Task: Update ChatService constructor and DI configuration following requirements 2.1 and 7.1, adding new dependencies to constructor and DI registration | Restrictions: Must maintain existing constructor signature compatibility, follow Koin patterns, no breaking changes | Success: All dependencies are properly injected, no DI resolution errors, follows existing patterns

- [x] 6. Implement WorldBookViewModel
  - File: app/src/main/java/me/rerere/rikkahub/ui/viewmodel/WorldBookViewModel.kt
  - Update existing implementation if needed (already exists but commented)
  - Add state management for entries, search query, assistant ID
  - Implement CRUD operations with proper error handling
  - Purpose: Manage UI state for world book management
  - _Leverage: existing WorldBookViewModel implementation, WorldBookRepository, StateFlow patterns
  - _Requirements: 3.1, 3.2, 3.3
  - _Prompt: Role: Android Developer with expertise in MVVM and Jetpack Compose state management | Task: Implement or update WorldBookViewModel following requirements 3.1, 3.2, and 3.3, managing state for entries list, search, and CRUD operations | Restrictions: Must use StateFlow/MutableStateFlow, handle async operations properly, implement proper error handling | Success: ViewModel manages state correctly, all operations work with proper loading/error states, follows MVVM patterns

- [-] 7. Implement MemoryTableViewModel
  - File: app/src/main/java/me/rerere/rikkahub/ui/viewmodel/MemoryTableViewModel.kt
  - Create new ViewModel for memory table management
  - Add state for tables list, selected table, rows, assistant ID
  - Implement CRUD operations with reactive updates
  - Purpose: Manage UI state for memory table management
  - _Leverage: existing ViewModel patterns from WorldBookViewModel, MemoryTableRepository, StateFlow
  - _Requirements: 5.1, 5.2
  - _Prompt: Role: Android Developer with MVVM and state management expertise | Task: implement MemoryTableViewModel following requirements 5.1 and 5.2, managing state for tables list, selection, rows, and CRUD operations | Restrictions: Must follow existing ViewModel patterns, use StateFlow, maintain data consistency | Success: ViewModel properly manages all state, operations update state reactively, follows architecture patterns

^- [x] 8. Create WorldBookManagementPage composable
  - File: app/src/main/java/me/rerere/rikkahub/ui/pages/worldbook/WorldBookManagementPage.kt
  - Implement list view with search functionality
  - Add floating action button for creating new entries
  - Implement entry cards with actions (edit, toggle enabled, delete)
  - Purpose: Provide UI for managing world book entries
  - _Leverage: existing Compose pages like AssistantPage.kt, Material 3 components, WorldBookViewModel
  - _Requirements: 3.1, 3.2, 3.3, 3.5, 3.6, 3.7
  - _Prompt: Role: Android/Jetpack Compose Developer with UI/UX expertise | Task: Create WorldBookManagementPage composable following requirements 3.1, 3.2, 3.3, 3.5, 3.6, and 3.7, implementing list view with search, FAB, entry cards with actions | Restrictions: Must use Material 3 components, follow existing page patterns, implement proper navigation, handle empty states | Success: Page displays entries correctly, search works, all actions function properly, follows design system

^- [x] 9. Create WorldBookEditorPage composable
  - File: app/src/main/java/me/rerere/rikkahub/ui/pages/worldbook/WorldBookEditorPage.kt
  - Implement form with all world book fields (title, keywords, content, etc.)
  - Add tag-based keyword input with autocomplete
  - Add toggle switches for advanced settings (regex, constant, selective, etc.)
  - Add priority slider and injection position selector
  - Implement save/cancel actions with validation
  - Purpose: Provide UI for creating and editing world book entries
  - _Leverage: existing Compose forms, Material 3 components, validation patterns
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8
  - _Prompt: Role: Android/Compose Developer with form design expertise | Task: Create WorldBookEditorPage composable following requirements 4.1 through 4.8, implementing comprehensive form with all fields, tag input, toggles, and validation | Restrictions: Must use Material 3, implement proper validation, handle keyboard input correctly, provide good UX | Success: All fields work correctly, validation prevents invalid data, form is intuitive and user-friendly

^- [-] 10. Create MemoryTableManagementPage composable
  - File: app/src/main/java/me/rerere/rikkahub/ui/pages/memorytable/MemoryTableManagementPage.kt
  - Implement list view for tables with search
  - Add FAB for creating new tables
  - Implement table cards with actions (edit, delete)
  - Purpose: Provide UI for managing memory tables
  - _Leverage: existing Compose pages like WorldBookManagementPage, Material 3, MemoryTableViewModel
  - _Requirements: 5.1, 5.2, 5.3, 5.5, 5.6
  - _Prompt: Role: Android/Compose Developer with list UI expertise | Task: Create MemoryTableManagementPage composable following requirements 5.1, 5.2, 5.3, 5.5, and 5.6, implementing table list with search, FAB, and actions | Restrictions: Must follow existing page patterns, Material 3 components, proper navigation | Success: Page displays tables correctly, search works, actions function properly, consistent with app design

^- [-] 11. Create MemoryTableEditorPage composable
  - File: app/src/main/java/me/rerere/rikkahub/ui/pages/memorytable/MemoryTableEditorPage.kt
  - Implement table editor with name/description fields
  - Add column header management (add/remove/rename)
  - Implement Excel-like row editing interface
  - Add import from CSV and export to CSV/Markdown buttons
  - Add save/cancel actions
  - Purpose: Provide UI for creating and editing memory tables
  - _Leverage: existing Compose forms, Reorderable library (in dependencies), Material 3
  - _Requirements: 5.4, 7.3
  - _Prompt: Role: Android/Compose Developer with data grid/editing expertise | Task: Create MemoryTableEditorPage composable following requirements 5.4 and 7.3, implementing comprehensive table editor with column/row management and import/export | Restrictions: Must handle large tables efficiently, implement proper scrolling, use Reorderable for row reordering | Success: Table editing works smoothly, import/export functions correctly, handles large datasets well

^- [-] 12. Add knowledge base button to ChatPage
  - File: app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt
  - Add knowledge base button to TopAppBar actions
  - Implement dropdown/modal with World Book and Memory Table options
  - Add navigation to respective management pages
  - Purpose: Provide quick access to knowledge base management
  - _Leverage: existing ChatPage TopAppBar, Material 3 components, navigation patterns
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
  - _Prompt: Role: Android/Compose Developer with navigation and UI integration expertise | Task: Add knowledge base button to ChatPage following requirements 6.1 through 6.5, implementing TopAppBar button with navigation menu | Restrictions: Must not interfere with existing actions, maintain design consistency, handle navigation properly | Success: Button is visible and accessible, navigation works correctly, maintains app design consistency

- [ ] 13. Add world book and memory table settings to AssistantDetailPage
  - File: app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailPage.kt
  - Add toggle switches for enableWorldBook and enableMemoryTable
  - Add configuration fields for world book settings (context size, history messages, format style)
  - Add world book tab to existing tab layout
  - Purpose: Allow users to configure knowledge base features per assistant
  - _Leverage: existing AssistantDetailPage tab layout, Material 3 toggles/sliders
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
  - _Prompt: Role: Android/Compose Developer with settings UI expertise | Task: Add knowledge base settings to AssistantDetailPage following requirements 8.1 through 8.5, implementing toggles and configuration fields | Restrictions: Must follow existing tab/layout patterns, persist settings correctly, validate inputs | Success: Settings UI integrates seamlessly, all toggles work, settings are saved and applied correctly

- [ ] 14. Enable navigation routes in RouteActivity
  - File: app/src/main/java/me/rerere/rikkahub/RouteActivity.kt
  - Uncomment/comment out world book and memory table composable routes
  - Add knowledge base routes to NavHost
  - Purpose: Enable navigation to knowledge base screens
  - _Leverage: existing RouteActivity navigation setup, Screen definitions
  - _Requirements: 3.1, 5.1, 6.1
  - _Prompt: Role: Android Developer with navigation architecture expertise | Task: Enable navigation routes in RouteActivity following requirements 3.1, 5.1, and 6.1, adding composable routes to NavHost for world book and memory table screens | Restrictions: Must follow existing route patterns, maintain proper argument passing, test navigation flows | Success: All routes navigate correctly, arguments are passed properly, navigation back works as expected

- [ ] 15. Clean up commented UI code
  - Files: app/src/main/java/me/rerere/rikkahub/ui/fragment/WorldBookFragment.kt, app/src/main/java/me/rerere/rikkahub/ui/fragment/MemoryTableFragment.kt, app/src/main/java/me/rerere/rikkahub/ui/adapter/MemoryTableEditorAdapter.kt, app/src/main/java/me/rerere/rikkahub/ui/viewmodel/MemoryTableEditorViewModel.kt
  - Remove commented/legacy code that uses Android View system
  - Remove any unused imports and dependencies
  - Purpose: Maintain clean codebase without legacy code
  - _Leverage: existing file structure, git history if needed
  - _Requirements: Code Quality
  - _Prompt: Role: Android Developer with code cleanup and refactoring expertise | Task: Clean up commented UI code in legacy Fragment/DataBinding files, removing unused code that might cause confusion | Restrictions: Must not remove any functionality, backup important code patterns in comments, verify no broken references | Success: Legacy code is removed or properly archived, no compilation warnings, codebase is clean and maintainable

- [ ] 16. Update Assistant model to use MemoryTableQueryService
  - File: app/src/main/java/me/rerere/rikkahub/data/model/Assistant.kt
  - Verify all world book and memory table fields are present and correct
  - Add any missing configuration fields if needed
  - Purpose: Ensure Assistant model supports all required configurations
  - _Leverage: existing Assistant.kt implementation, requirements specification
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
  - _Prompt: Role: Android/Kotlin Developer with data model expertise | Task: Verify and update Assistant model following requirements 8.1 through 8.5, ensuring all world book and memory table configuration fields are present and properly typed | Restrictions: Must maintain backward compatibility, use correct serialization annotations, follow existing patterns | Success: Model includes all required fields, serialization works correctly, backward compatibility maintained

- [ ] 17. Integrate MemoryTableQueryService with AI tools
  - File: app/src/main/java/me/rerere/rikkahub/data/ai/tools/LocalTools.kt
  - Add memory table query tool to local tools list
  - Implement tool definition and execution logic
  - Purpose: Enable AI assistants to query memory tables via tools
  - _Leverage: existing LocalTools.kt pattern, MemoryTableQueryService
  - _Requirements: 7.1
  - _Prompt: Role: Android/Kotlin Developer with AI integration expertise | Task: Integrate MemoryTableQueryService with AI tools following requirement 7.1, adding memory table query as a local tool | Restrictions: Must follow existing tool patterns, implement proper tool metadata, handle tool execution errors | Success: Tool is properly registered, AI can query tables, tool execution handles errors gracefully

- [ ] 18. Test world book integration in ChatService
  - File: app/src/test/java/me/rerere/rikkahub/service/ChatServiceWorldBookTest.kt (create new)
  - Write unit tests for world book matching and injection
  - Test with various assistant configurations
  - Test error handling and graceful degradation
  - Purpose: Ensure ChatService integration works correctly
  - _Leverage: existing test patterns, ChatService, WorldBookMatcher, WorldBookInjector
  - _Requirements: 2.1, 2.2
  - _Prompt: Role: QA Engineer with unit testing expertise | Task: Create comprehensive unit tests for ChatService world book integration covering requirements 2.1 and 2.2, testing various scenarios and configurations | Restrictions: Must mock dependencies appropriately, test both success and failure cases, maintain test isolation | Success: All integration scenarios are tested, tests pass reliably, test coverage is adequate

- [ ] 19. Test MemoryTableQueryService
  - File: app/src/test/java/me/rerere/rikkahub/service/MemoryTableQueryServiceTest.kt (create new)
  - Write tests for search, formatting, import, export methods
  - Test with various table sizes and data types
  - Test validation and error handling
  - Purpose: Ensure MemoryTableQueryService works correctly
  - _Leverage: existing test patterns, MemoryTableQueryService
  - _Requirements: 7.1, 7.2, 7.3
  - _Prompt: Role: QA Engineer with service testing expertise | Task: Create comprehensive unit tests for MemoryTableQueryService covering requirements 7.1, 7.2, and 7.3, testing search, formatting, import, and export functionality | Restrictions: Must test with realistic data, verify output formats are correct, test error scenarios | Success: All methods are thoroughly tested, output formats are validated, error handling works correctly

- [ ] 20. Run full application build and tests
  - Execute: ./gradlew build
  - Execute: ./gradlew test
  - Fix any compilation errors or test failures
  - Purpose: Ensure all changes integrate correctly
  - _Leverage: existing build system, test infrastructure
  - _Requirements: All
  - _Prompt: Role: Build Engineer with CI/CD expertise | Task: Run full application build and test suite, fixing any issues that arise | Restrictions: Must not break existing functionality, maintain test pass rates, ensure build stability | Success: Build completes successfully, all tests pass, no regressions introduced

- [ ] 21. Create usage documentation
  - File: docs/world-book-usage-guide.md
  - Document how to use world book features
  - Document how to use memory table features
  - Include examples and best practices
  - Purpose: Provide user documentation for new features
  - _Leverage: existing documentation patterns, feature specifications
  - _Requirements: User Documentation
  - _Prompt: Role: Technical Writer with Android app documentation expertise | Task: Create comprehensive usage documentation for world book and memory table features, including examples and best practices | Restrictions: Must be clear and concise, include screenshots/examples, follow existing doc patterns | Success: Documentation is complete and clear, covers all user scenarios, follows style guide

- [ ] 22. Final code review and cleanup
  - Review all implemented code for quality and consistency
  - Ensure all files follow project conventions
  - Remove any debug code or TODO comments
  - Verify all features work end-to-end
  - Purpose: Ensure high code quality and maintainability
  - _Leverage: existing code review standards, project conventions
  - _Requirements: Code Quality
  - _Prompt: Role: Senior Android Developer with code review and quality assurance expertise | Task: Perform comprehensive code review and cleanup, ensuring all code meets quality standards and follows conventions | Restrictions: Must maintain functionality, ensure performance, verify security considerations | Success: Code is clean and maintainable, all features work end-to-end, meets quality standards

## Implementation Notes

### Task Dependencies
- Task 1 must complete before Task 2
- Task 2 must complete before Task 5
- Tasks 3-4 can be done in parallel
- Tasks 6-7 can be done in parallel
- Tasks 8-11 require Tasks 6-7 to be complete
- Task 12 requires Tasks 8-11 to be complete
- Task 13 can be done in parallel with Tasks 8-11
- Task 14 requires Tasks 8-11 to be complete
- Tasks 15-22 can be done after core implementation

### File Organization
All new files should be created in the appropriate package structure following existing conventions:
- Services: `app/src/main/java/me/rerere/rikkahub/service/`
- ViewModels: `app/src/main/java/me/rerere/rikkahub/ui/viewmodel/`
- UI Pages: `app/src/main/java/me/rerere/rikkahub/ui/pages/worldbook/` and `app/src/main/java/me/rerere/rikkahub/ui/pages/memorytable/`
- Tests: `app/src/test/java/me/rerere/rikkahub/service/`

### Code Style
- Follow existing Kotlin code style
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Maintain consistent formatting
- Follow Material 3 design guidelines for UI
