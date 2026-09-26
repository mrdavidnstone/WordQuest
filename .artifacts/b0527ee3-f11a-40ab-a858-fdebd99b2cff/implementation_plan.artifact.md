# Word Defender Monster Movement Fix

Fix the monster movement system to ensure monsters move continuously and smoothly towards the castle, independent of the spelling progress.

## User Review Required

> [!NOTE]
> The monsters are currently cleared at the start of each word. I will modify this so that existing monsters (distractors or previous word remnants) continue their journey while new monsters are added, preventing the "reset" look.

## Proposed Changes

### [app]

#### [MODIFY] [WordDefenderViewModel.kt](file:///C:/Android/SpellingGames/app/src/main/java/com/spellinggames/app/ui/screens/games/WordDefenderViewModel.kt)
- Add `baseMonsterSpeed` to the state/repository for configurability.
- Update `updatePhysics` to run in more phases (like `WORD_COMPLETE`) to prevent freezing.
- Modify `startWord` to avoid clearing all monsters immediately, allowing smooth transitions.
- Implement the "target" logic where monsters move specifically towards the castle base.
- Add debug information to the UI state (monster count, average distance, etc.).

#### [MODIFY] [WordDefenderScreen.kt](file:///C:/Android/SpellingGames/app/src/main/java/com/spellinggames/app/ui/screens/games/WordDefenderScreen.kt)
- Add a debug overlay toggle/display showing monster speed and distance.
- Ensure the rendering logic supports smooth frame-based updates (which it mostly does, but I'll double-check the `SnapshotStateList` observation).

## Verification Plan

### Manual Verification
- Deploy the app and play "Word Defender".
- Observe monsters spawning and moving continuously.
- Verify that monsters don't stop moving when a word is completed or an incorrect answer is given.
- Check that monsters hit the castle if not defeated, dealing damage.
- Verify the debug info display.
