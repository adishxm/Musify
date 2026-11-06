# Phase 32 — High-Performance Compiled Zero-Copy Filter Engine

**Status:** PLANNED  
**Planning duration:** 1–2 days  
**Goal:** Compile Musify's filter lists and Brave rule catalogs into an in-memory prefix/suffix Trie and Bloom filter data structure inspired by Brave's `engine0.dat` FlatBuffers format. This achieves sub-millisecond URL rule evaluation on Android with less than 15 MB of heap allocation.

---

## 1. Context & Reverse-Engineered Mechanism

Parsing text filter lists upon app launch consumes 100MB+ of RAM and introduces noticeable UI frame drops on mobile devices.

Brave uses `adblock-rust` to pre-compile rules into a memory-mapped binary FlatBuffer (`engine0.dat`). By indexing tokens into a fast hash table and verifying candidates against a Bloom filter, matching occurs in $O(1)$ to $O(k)$ time without string allocations.

---

## 2. Scope and Tasks

1. **Compact Rule Trie & Token Matcher (`CompiledFilterIndex.kt`):**
   - Parse filter rules into domain trie, exact path tokens, and regex indices.
   - Implement fast candidate selection via domain hash map.

2. **Zero-Allocation Evaluation Pipeline:**
   - Optimize `evaluateResource` to execute without object allocations in the hot path.

3. **Performance Benchmarking (`Phase32FilterPerformanceTest.kt`):**
   - Benchmark rule evaluation speed against a batch of 1,000 URLs, targeting $< 1.0\text{ ms}$ per evaluation.
   - Run complete `./gradlew.bat testDebugUnitTest` (33 test suites).

4. **Phase Report & Multi-Variant Release Assembly:**
   - Document `.brain/.report/PHASE-32-REPORT.md`.
   - Compile signed release APKs and push to Git.

---

## 3. Verification Gate (Run at Phase End)
- [ ] Sub-millisecond rule matching verified across benchmark tests.
- [ ] Heap memory overhead $< 15\text{ MB}$.
- [ ] Complete unit test suite passes 100%.
- [ ] `.brain/.report/PHASE-32-REPORT.md` documented and committed.

