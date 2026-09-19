#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:-.}"
cd "$ROOT"

TARGET="app/src/main/java/com/febricahyaa/clockapp/ui/screens/ChronaBentoHomeScreen.kt"

if [[ ! -f "$TARGET" ]]; then
  echo "ERROR: $TARGET not found. Run this script from the Chrona repository or pass the repo path as the first argument." >&2
  exit 1
fi

python3 - "$TARGET" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")

start_anchor = "@Composable\nprivate fun ClockHero("
end_anchor = "@Composable\nprivate fun TimeActionCard("
start = text.find(start_anchor)
end = text.find(end_anchor, start)

if start < 0 or end < 0:
    raise SystemExit("ERROR: ClockHero/TimeActionCard anchors were not found; repository layout may have changed.")

if "private fun ClockModeSwitcher(" in text[start:end]:
    raise SystemExit("INFO: Phase 2 clock experience revamp is already applied to this file.")

replacement = r'''@Composable
private fun ClockHero(
    now: java.time.ZonedDateTime,
    dateText: String,
    displayMode: ClockDisplayMode,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
    onDisplayModeChange: (ClockDisplayMode) -> Unit,
) {
    val daytime = now.hour in 7..17
    val utcOffset = formatUtcOffset(now.offset.totalSeconds)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(
                        Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (daytime) 0.92f else 0.65f,
                                ),
                            ),
                    )
                    Text(
                        if (daytime) {
                            stringResource(R.string.home_daytime)
                        } else {
                            stringResource(R.string.home_nighttime)
                        },
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    now.zone.id.replace('_', ' '),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            BentoIconButton(
                icon = if (displayMode == ClockDisplayMode.DIGITAL) {
                    Icons.Filled.AccessTime
                } else {
                    Icons.Filled.GridView
                },
                onClick = {
                    onDisplayModeChange(displayMode.toggle())
                },
                contentDescription = if (displayMode == ClockDisplayMode.DIGITAL) {
                    stringResource(R.string.home_switch_to_analog)
                } else {
                    stringResource(R.string.home_switch_to_digital)
                },
                active = true,
                modifier = Modifier.size(42.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 190.dp)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = displayMode,
                transitionSpec = {
                    fadeIn(tween(180)).togetherWith(fadeOut(tween(120)))
                },
                label = "clock-style-transition",
            ) { mode ->
                when (mode) {
                    ClockDisplayMode.DIGITAL -> DigitalClockUI(
                        hour24 = now.hour,
                        minute = now.minute,
                        second = now.second,
                        use24HourFormat = use24HourFormat,
                        showSeconds = showSeconds,
                    )

                    ClockDisplayMode.ANALOG -> SmoothAnalogClockUI(now.zone)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.86f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.09f),
            ),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        dateText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.home_local_time),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.35.sp,
                    )
                }
                Text(
                    utcOffset,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        ClockModeSwitcher(displayMode, onDisplayModeChange)
    }
}

@Composable
private fun ClockModeSwitcher(
    selected: ClockDisplayMode,
    onSelected: (ClockDisplayMode) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ClockModeChoice(
                mode = ClockDisplayMode.DIGITAL,
                selected = selected == ClockDisplayMode.DIGITAL,
                modifier = Modifier.weight(1f),
                onClick = onSelected,
            )
            ClockModeChoice(
                mode = ClockDisplayMode.ANALOG,
                selected = selected == ClockDisplayMode.ANALOG,
                modifier = Modifier.weight(1f),
                onClick = onSelected,
            )
        }
    }
}

@Composable
private fun ClockModeChoice(
    mode: ClockDisplayMode,
    selected: Boolean,
    modifier: Modifier,
    onClick: (ClockDisplayMode) -> Unit,
) {
    Surface(
        onClick = { onClick(mode) },
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (mode == ClockDisplayMode.DIGITAL) {
                    Icons.Filled.AccessTime
                } else {
                    Icons.Filled.GridView
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(Modifier.size(6.dp))
            Text(
                text = stringResource(
                    if (mode == ClockDisplayMode.DIGITAL) {
                        R.string.settings_clock_style_digital
                    } else {
                        R.string.settings_clock_style_analog
                    },
                ),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun SmoothAnalogClockUI(zoneId: java.time.ZoneId) {
    val smoothNow = rememberSmoothZonedNow(zoneId)
    AnalogClockUI(
        hour = smoothNow.hour,
        minute = smoothNow.minute,
        second = smoothNow.second + smoothNow.nano / 1_000_000_000f,
    )
}

@Composable
private fun DigitalClockUI(
    hour24: Int,
    minute: Int,
    second: Int,
    use24HourFormat: Boolean,
    showSeconds: Boolean,
) {
    val hour = if (use24HourFormat) hour24 else ((hour24 + 11) % 12) + 1
    val secondsProgress by animateFloatAsState(
        second / 59f,
        tween(850),
        label = "seconds-progress",
    )
    val heroClockStyle = MaterialTheme.typography.displayLarge.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeight = 0.96.em,
        fontSize = 80.sp,
        letterSpacing = (-4.8).sp,
        fontFeatureSettings = "tnum",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = hour.toString().padStart(2, '0'),
                style = heroClockStyle,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                text = ":${minute.toString().padStart(2, '0')}",
                style = heroClockStyle,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false,
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!use24HourFormat) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        stringResource(if (hour24 < 12) R.string.time_am else R.string.time_pm),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            if (showSeconds) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(
                                R.string.home_seconds_suffix,
                                second.toString().padStart(2, '0'),
                            ),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(
                                        alpha = secondsProgress.coerceIn(0.35f, 1f),
                                    ),
                                ),
                        )
                    }
                }
            }
        }
    }
}

private fun formatUtcOffset(totalSeconds: Int): String {
    val totalMinutes = totalSeconds / 60
    val sign = if (totalMinutes >= 0) "+" else "-"
    val absoluteMinutes = kotlin.math.abs(totalMinutes)
    val hours = absoluteMinutes / 60
    val minutes = absoluteMinutes % 60
    return if (minutes == 0) {
        "UTC$sign$hours"
    } else {
        "UTC$sign$hours:${minutes.toString().padStart(2, '0')}"
    }
}

'''

new_text = text[:start] + replacement + text[end:]
path.write_text(new_text, encoding="utf-8")
PY

