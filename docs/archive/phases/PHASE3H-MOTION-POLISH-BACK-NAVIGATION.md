# Phase 3H — Motion Polish & Back Navigation

## Goal
Close the Dashboard Spatial Motion phase by making navigation stack behavior predictable and keeping shared-motion surfaces intact.

## Changes
- Normalize Alarm, World Clock, Timer, and Stopwatch navigation to the Dashboard root.
- Preserve World Clock detail/search, Settings, and Legal as stack destinations.
- Keep the existing navigation-level enter/exit/pop motion contract.
- Avoid introducing experimental predictive-back APIs.

## Back behavior
- Primary time tool → system/app back returns to Dashboard.
- World Clock → City Detail → back returns to World Clock.
- World Clock → Search → back remains within the World Clock flow.
- Settings and Legal remain ordinary stack destinations.

## Phase 3 closure
This completes the planned Phase 3 spatial-motion foundation: stable motion keys, destination shared bounds, detail motion, unified navigation transitions, and predictable back-stack behavior.

Visual tuning on physical devices is considered validation/refinement, not a new architectural phase.
