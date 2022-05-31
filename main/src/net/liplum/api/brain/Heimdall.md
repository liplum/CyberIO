## Concept


|     | 1.0  | 1.1  |     |
| ----- | ------ | ------ | ----- |
| 2.0 | Self | Self | 0.0 |
| 2.1 | Self | Self | 0.1 |
|     | 3.0  | 3.1  |     |

For Side[0] and Side[3], the proximate component is x.1

For Side[1] and Side[2], the proximate component is x.0

## Rotation

### Clockwise


|     | 2.1  | 2.0  |     |
| ----- | ------ | ------ | ----- |
| 3.0 | Self | Self | 1.0 |
| 3.1 | Self | Self | 1.1 |
|     | 0.1  | 0.0  |     |

At the [1] or [3], the side should swap its componets.

### Anti-clockwise


|     | 0.0  | 0.1  |     |
| ----- | ------ | ------ | ----- |
| 1.1 | Self | Self | 3.1 |
| 1.0 | Self | Self | 3.0 |
|     | 2.0  | 2.1  |     |

At the [0] or [2], the side should swap its componets.
