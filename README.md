# TaCZ Ad Astra Arm Fix (Unofficial Patch)

With an Ad Astra space suit, netherite space suit or jet suit on, Ad Astra replaces your first-person arms with the suit's arms, and they sit in the wrong place. TaCZ places both hands exactly on a gun, so there the arms visibly miss the gun. This small companion mod draws your normal arms in first person instead, exactly as without a suit, so TaCZ guns are held properly.

Nothing else changes. The suit still shows in third person and to other players, and other chestplates are untouched.

Client-only. It does nothing on a server, and clients and servers don't need to match.

## Reason For Existing

Temporarily exists until if/when Ad Astra fixes the issue.

### Credit

This is an unofficial patch, not affiliated with Ad Astra or TaCZ. [Ad Astra](https://github.com/terrarium-earth/Ad-Astra) is made by Terrarium (Alex Nijjar and contributors). [TaCZ (Timeless and Classics Zero)](https://github.com/MCModderAnchor/TACZ) is made by the TaCZ team.

## How It Works

Every first-person arm goes through vanilla's `PlayerRenderer.renderRightHand` or `renderLeftHand`: the empty hand, TaCZ's gun hands and Ad Astra's zip gun. Each fires Forge's `RenderArmEvent`, then calls the private `renderHand`. Ad Astra's `PlayerRendererMixin` cancels `renderHand` whenever the chestplate is a space suit and draws the suit's arm instead.

This mod injects just before that `renderHand` call. When the chestplate is an Ad Astra suit, it draws the arm and sleeve the way vanilla's `renderHand` does and skips the call, so Ad Astra's replacement never runs. Forge's event still fires first. With any other chestplate it does nothing.

At startup the log says `Fix applied`, or `Fix not applied` if a later Ad Astra no longer has its `SpaceSuitItem` class. In that case the mod does nothing and Ad Astra draws its suit arm as before.

## Building

Put these jars in `libs/` (it's gitignored), then run `gradlew build`:

- `ad_astra-forge-1.20.1-1.15.21.jar`
- `botarium-forge-1.20.1-2.3.4.jar`
- `resourcefullib-forge-1.20.1-2.1.29.jar`
- `resourcefulconfig-forge-1.20.1-2.1.3.jar`
- `tacz-1.20.1-1.1.8-hotfix2.jar` (only loaded in the dev client for testing, never compiled against)

## License

MIT. No Ad Astra or TaCZ code or assets are included, and the icon is original.
