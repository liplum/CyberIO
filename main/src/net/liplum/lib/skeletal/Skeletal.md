## Mechanics
**Note:This has been suspended indefinitely.**
### Skeleton

1. A skeleton can't be circular but linear or tree.

### Joint

1. A joint is between two bones, in other words, joins them.
2. It has two bones, one for previous bone and another for next bone, but if it's a leaf joint, there isn't any next
   bone.
3. It has a rotation only for rendering.
4. It can be applied a force.
5. It has a position relative to its previous joint.
```
For perspective of joint:
pre? J1 next ---B1--- pre J2 next ---B2--- pre J3 next?
For perspective of bone:
j1a ---B1--- jb2 --- B2 --- ja3 So that:
J1.next == B1.ja J2.pre == B1.jb
```

### Bone

1. A bone is connected with two joints.
2. It is a rigid body, so it's of a fixed length and density, which can decides its mass.
   ```
   let density is p, length is l and mass is m 
   m = p * l
   ```
3. Its rotation decides the next joint


### Acceleration
1. The acceleration of angular velocity is only of value and rotational direction.
2. It can increase the angular velocity.
   ```
   let accleration is a
   w += a * t
   ```
### Force

1. Force isn't real in skeleton, however, it can change the acceleration.
   ```
   let the force is F.
   |a| = |F| / m
   direction of a is decided by the angle bettween proximate bone's direction and F.
   ```
2. [Optional] A force is only applied on a joint and can spread joint by joint.
   ```
   let the accleartion of proximate joint is a' and the mass of it is m' 
   |a'| = |a| / m'
   ```
3. Force is disposable between two updates unless there is a constant force.


### Velocity

1. Only joint has velocity.
2. A force can increase the velocity.
    ```
    let the velocity is v and the force is F.  
    v += F * t
    ```
3. Velocity will increase the position
    ```
    let the position is p.
    p += v * t
    ```

### Position

1. It represents the location of a joint.

### Friction

1. It's always the opposite of velocity.
2. The greater its velocity, the greater the friction it receives.
   ```
   let the friction value is f and its maxinum value is fmax.
   f = min(u * |v|,fmax)
   ```

### Update

Once update should do the following things in order.

1. Apply the acceleration on angular velocity.
2. Clear acceleration.
3. Apply the angular velocity on position.
