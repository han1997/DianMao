# UI Visual Enhancement - Hierarchy and Colors

## Goal

Improve visual hierarchy, color strategy, and overall polish of the main interface while maintaining the existing functionality and layout structure.

## Current State

**Strengths:**
- Clean Material3 design
- Functional quadrant layout
- Clear task organization
- Smooth animations

**Areas for improvement:**
- Visual hierarchy could be stronger (statistics blend in)
- Color strategy is purely functional (Material3 defaults)
- Spacing could create more breathing room
- Typography hierarchy could be more distinctive

## Design Improvements

### 1. Enhanced Statistics Display

**Current:** Plain text with counts
**Proposed:**
- Larger, bold numbers with lighter labels
- Color-coded icons for each stat type
- Subtle background containers for visual grouping
- Better use of spacing between stats

### 2. Improved Header Visual Hierarchy

**Current:** Title and date subtitle with equal weight
**Proposed:**
- Stronger title presence (larger, bolder)
- More refined subtitle styling
- Better vertical rhythm
- Archive/Settings buttons with subtle hover states

### 3. Quadrant Color Enhancement

**Current:** Colors from TaskPriority enum, borders at 0.55f alpha
**Proposed:**
- Slightly more saturated quadrant colors for better distinction
- Color-matched badges/indicators
- Refined border opacity for better depth
- Hover states for quadrant cards

### 4. Task Card Refinement

**Current:** Uniform padding, single border treatment
**Proposed:**
- Slightly increased padding for breathing room
- Refined completed state (less opacity drop, smoother transition)
- Better visual separation between task elements
- Priority color dot more prominent

### 5. Typography Hierarchy

**Current:** Material3 defaults throughout
**Proposed:**
- Header: Bolder, more distinctive
- Task titles: Slightly larger, better weight contrast
- Metadata: Smaller, lighter for clear hierarchy
- Numbers in stats: Tabular/featured treatment

## Implementation Approach

**Non-breaking changes:**
- All changes are visual polish, no functionality changes
- Maintain existing component structure
- Preserve Material3 theming system
- Keep accessibility (contrast, touch targets)

**Files to modify:**
- `MainActivity.kt`: Update composables for visual refinements
  - AppHeader: Enhanced stats, better hierarchy
  - QuadrantBoard: Color adjustments
  - TaskRow: Padding, typography, color refinements
  - CompletionMark: Potentially enhanced

## Specific Changes

### AppHeader Statistics
```kotlin
// Before: Plain text
Text("待办 $openTasks · 定时 $timedTasks · 已完成 $completedTasks")

// After: Structured with emphasis
Row {
    StatItem(icon = CheckCircle, count = openTasks, label = "待办", color = primary)
    StatItem(icon = Schedule, count = timedTasks, label = "定时", color = tertiary)
    StatItem(icon = Done, count = completedTasks, label = "已完成", color = secondary)
}
```

### Quadrant Colors
- ImportantUrgent: More saturated red (#D32F2F → #E53935)
- ImportantNotUrgent: More saturated blue (#1976D2 → #1E88E5)
- UrgentNotImportant: More saturated amber (#F57C00 → #FB8C00)
- NotImportantNotUrgent: Refined gray (keep current)

### Task Card Padding
```kotlin
// Before: horizontal = 12.dp, vertical = 10.dp
// After: horizontal = 16.dp, vertical = 14.dp
```

### Typography Weights
- App title: SemiBold → Bold
- Task title: Medium (current) + 1.2x size increase
- Metadata: Regular → Light

## Acceptance Criteria

* [ ] Statistics display is more visually prominent
* [ ] Header has stronger visual hierarchy
* [ ] Quadrant colors are more distinctive
* [ ] Task cards have better spacing and readability
* [ ] Typography creates clear information hierarchy
* [ ] Completed state transition is smooth
* [ ] All changes maintain accessibility standards
* [ ] No functionality regressions

## Out of Scope

- Animation changes (keep existing)
- Layout restructuring (keep quadrant grid)
- New features or interactions
- Icon changes
- Dark mode specific changes (will adapt automatically)

## Design Principles

**Subtlety over drama:** Refinements, not redesign
**Readability first:** Hierarchy serves information scanning
**Material3 aligned:** Work with the system, not against it
**Accessible:** Maintain contrast ratios and touch targets
