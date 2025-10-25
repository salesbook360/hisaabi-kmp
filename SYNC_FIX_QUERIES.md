# Sync Query Fixes

## Issue
The DAO queries use `sync_status != 0`, but:
- Default `sync_status` in entities is `0`
- SyncStatus enum: NONE(1), SYNCED(2), UPDATED(3)
- Query `sync_status != 0` gets records with status 1, 2, 3 but NOT 0

## Problem
- Default records (sync_status = 0) are NOT being synced
- Synced records (sync_status = 2) ARE being synced again (wrong!)

## Solution
Change all `getUnsynced*` queries to: `sync_status != 2`

This will sync:
- sync_status = 0 (default/initial) ✓
- sync_status = 1 (NONE) ✓
- sync_status = 3 (UPDATED) ✓
- sync_status = 2 (SYNCED) ✗ (correctly excluded)

## Files to Update
All DAO files with getUnsynced queries.


