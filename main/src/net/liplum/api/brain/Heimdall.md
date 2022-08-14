## Concept

|     | 1.1  | 1.0  |     |
|-----|------|------|-----|
| 2.0 | Self | Self | 0.1 |
| 2.1 | Self | Self | 0.0 |
|     | 3.0  | 3.1  |     |

Clockwise
`Right` -> `Bottom` : `0` -> `3`

`Side.Pos`

|     | 3    | 2    |     |
|-----|------|------|-----|
| 4   | Self | Self | 1   |
| 5   | Self | Self | 0   |
|     | 6    | 7    |     |

`Number` - `Side` * 2 + `Pos`

## Rotation

### Clockwise

|     | 2.1  | 2.0  |     |
|-----|------|------|-----|
| 3.0 | Self | Self | 1.1 |
| 3.1 | Self | Self | 1.0 |
|     | 0.0  | 0.1  |     |

### Anti-clockwise

|     | 0.1  | 0.0  |     |
|-----|------|------|-----|
| 1.0 | Self | Self | 1.1 |
| 1.1 | Self | Self | 1.0 |
|     | 2.0  | 2.1  |     |
