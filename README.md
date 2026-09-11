# Dynamic Survival HUD

Forge 1.20.1 client mod that fades inactive survival HUD elements while retaining danger and interaction cues.

## Verification

Run `./gradlew verifyFull stageRuntimeJar` before committing or pushing. `verifyFull`
runs the deterministic JVM/resource checks and validates the runtime JAR's production
mixin refmap. `verifyFast` runs just the deterministic checks.

This client mod has no server GameTests. Starting the optional `headlessGameTest`
server therefore provides no behavioral coverage and is not part of `verifyFull`.
HUD fading, danger/interaction cues, layout, and compatibility rendering require
manual review in a client; automated verification does not claim to check them.
