# Changelog

## 1.0.0 (2026-10-08)

First version, for Minecraft 1.20.1, Forge 47 and Ad Astra 1.15.21 or newer.

- With an Ad Astra space suit, netherite space suit or jet suit on, the first-person right arm is now the suit's right arm instead of a second left arm. This fixes the arm missing the grip when holding a TaCZ gun, and its placement with an empty hand.
- Same suit model, texture, dye colour and render type as Ad Astra, so shaders treat the arm as before.
- Logs one line at startup that says whether the fix applied.
- Client-only. A server with the mod starts normally, and clients and servers don't need to match.
