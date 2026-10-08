# TaCZ Ad Astra Arm Fix (Unofficial Patch)

With an Ad Astra space suit, netherite space suit or jet suit on, Ad Astra 1.15.21 draws the first-person right arm as a second left arm. The arm sits off to the side and its texture is mirrored. It's easy to miss with an empty hand, but TaCZ places both hands exactly on a gun, so there the right arm visibly misses the grip. This small companion mod draws the correct suit arm for each hand.

Nothing else changes. Other chestplates, third person and everything outside first-person arms are untouched.

Client-only. It does nothing on a server, and clients and servers don't need to match.

## Reason For Existing

Temporarily exists until if/when Ad Astra fixes the issue.

### Credit

This is an unofficial patch, not affiliated with Ad Astra or TaCZ. [Ad Astra](https://github.com/terrarium-earth/Ad-Astra) is made by Terrarium (Alex Nijjar and contributors). [TaCZ (Timeless and Classics Zero)](https://github.com/MCModderAnchor/TACZ) is made by the TaCZ team.

## How It Works

Every first-person arm goes through vanilla's `PlayerRenderer.renderRightHand` or `renderLeftHand`: the empty hand, TaCZ's gun hands and Ad Astra's zip gun. Both call the private `renderHand`, passing the player model's arm. Ad Astra's `PlayerRendererMixin` cancels `renderHand` when the chestplate is a space suit and draws the suit's arm instead. To pick which suit arm, it compares the player model's arm with the right arm of a suit model it has just created. Those are never the same object, so it always draws the suit's left arm, placed like the arm that was asked for.

This mod injects at the start of `renderRightHand` and `renderLeftHand`. When the chestplate is an Ad Astra suit, it poses the player model like vanilla does and draws the matching suit arm with the same model, render type, light and dye colour as Ad Astra, so shaders treat it the same. It then skips `renderHand`. With any other chestplate it does nothing.

At startup the log says `Fix applied`, or `Fix not applied` if a later Ad Astra changed the parts this uses. In that case the mod does nothing and Ad Astra draws its arm as before.

## Building

Put these jars in `libs/` (it's gitignored), then run `gradlew build`:

- `ad_astra-forge-1.20.1-1.15.21.jar`
- `botarium-forge-1.20.1-2.3.4.jar`
- `resourcefullib-forge-1.20.1-2.1.29.jar`
- `resourcefulconfig-forge-1.20.1-2.1.3.jar`
- `tacz-1.20.1-1.1.8-hotfix2.jar` (only loaded in the dev client for testing, never compiled against)

## License

MIT. The suit-arm drawing is adapted from Ad Astra's code, which is MIT under the Terrarium License v1; see [NOTICE](NOTICE). No Ad Astra or TaCZ assets are included, and the icon is original.
