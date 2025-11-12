# Implementation Plan

- [x] 1. Fix TestEmailGenerator to ensure unique ID generation





  - Update `generateEmail()` to use atomic counter and timestamp combination
  - Add `generateUniqueId()` private method with timestamp + counter + random
  - Update `generateEmails()` to use the new unique ID generation
  - Update `generateEmailThread()` to ensure unique IDs within thread
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 2. Add deduplication and cleanup to TestDataInserter



  - Add `deleteTestEmails()` method to clear old test data
  - Update `insertTestEmails()` to clear before inserting
  - Update `insertTestSuite()` to clear before inserting
  - Add logging for inserted email IDs
  - _Requirements: 3.4, 5.3_

- [x] 3. Add deduplication to EmailDao

  - Update `insertEmails()` to use `OnConflictStrategy.REPLACE`
  - Add `deleteTestEmails()` query method
  - Add `findDuplicateIds()` diagnostic query method
  - _Requirements: 2.2, 2.3_

- [x] 4. Add deduplication to EmailRepository


  - Update email retrieval methods to use `distinctBy { it.id }`
  - Add warning logs when duplicates are detected
  - Add diagnostic logging for email list size
  - _Requirements: 2.4, 5.2_

- [x] 5. Add validation to InboxViewModel


  - Update `loadEmails()` to deduplicate before updating state
  - Add warning logs when duplicates are found
  - Update all state update methods to ensure uniqueness
  - _Requirements: 4.4, 5.2_

- [x] 6. Add safety check to EmailListView


  - Add `remember` block to deduplicate emails before rendering
  - Add error logging when UI layer finds duplicates
  - Ensure LazyColumn uses deduplicated list
  - _Requirements: 1.1, 1.2, 4.1, 4.2, 4.3_

- [x] 7. Add diagnostic logging utility



  - Create `EmailListLogger` object with logging methods
  - Add `logDuplicates()` method for deduplication logging
  - Add `logEmailList()` method for detailed email list logging
  - Integrate logging into all layers
  - _Requirements: 5.1, 5.2, 5.3, 5.4_
