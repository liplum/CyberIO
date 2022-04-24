## Mechanics

**Note:This has been suspended indefinitely.**

### Skeleton

1. A skeleton can't be circular but linear or tree.

### Bone

1. A bone has an angle, angular velocity and angular acceleration.
2. It is a rigid body, so it's of a fixed length and mass

### Acceleration

1. The acceleration of angular velocity is only of value and rotational direction, so it's a scalar.
2. It can increase the angular velocity.
   ```
   let accleration is a
   w += a * t
   ```

### Force

1. Force isn't real in skeleton, however, it can change the acceleration.
   ```
   let the force is F and the the unit vector with angle is D.
   let N = normalize(D)
   a += ( N ⋅ F ) / m
   direction of a is decided by the angle bettween proximate bone's direction and F.
   ```
2. [Optional] A force is only applied on a bone and can spread bone by bone.
   ```
   let the distance between two bone is d.
   let R = (D + F) / d ^ 2
   proximity.applyForce(R)
   ```
3. Force is disposable between two updates unless there is a constant force.

### Velocity

1. Velocity is a scalar.
2. A force can increase the velocity.
    ```
    let the velocity is v and the force is F.  
    v += F * t
    ```
3. Velocity will increase the angle
    ```
    let the position is angle.
    angle += v * t
    ```

### Position

1. It represents the rotation of a bone relative to its parent.

### Friction

1. It's always the opposite of velocity.
2. The greater its velocity, the greater the friction it receives.
   ```
   let the friction value is f and its maxinum value is fmax.
   f = u * w
   ```

### Update

Once update should do the following things in order.

1. Apply the acceleration on angular velocity.
2. Clear acceleration.
3. Apply the angular velocity on position.
