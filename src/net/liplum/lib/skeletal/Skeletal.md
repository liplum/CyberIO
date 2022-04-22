## Mechanics

### Joint

1. A joint is between two bones, in other words, joins them.
2. It has two bones, one for previous bone and another for next bone, but if it's a leaf joint, there isn't any next
   bone.
3. It has a rotation only for rendering.
4. It can be applied a force.

`
*For perspective of joint*
pre? J1 next ---B1--- pre J2 next ---B2--- pre J3 next?
*For perspective of bone*
j1a ---B1--- jb2 --- B2 --- ja3 So that:
J1.next == B1.ja J2.pre == B1.jb
`

### Bone

1. A bone is connected with two joints.
2. It is a rigid body so has a fixed length.
3. Its rotation is computed by the positions of its two joints.
4. It has mass.

### Force

1. A force is only applied on a joint and doesn't spread joint by joint.
2. If a joint will become longer/shorter, it will apply an attenuating/pulling force to its proximate joints on the
   direction of bone.
    ```
    let the distance will be stretched is D, the mass of bone is m and the force to be applied is F.
    F += sign * k * sqrt(D) / m 
    F += sign * k * sqrt(D)       // If no mass in the implemetation
    P.S.: Whether it's an attenuating force or pulling force decides the sign -- negative or positive.
   ```
3. Force is disposable between update unless there is a constant force.

### Velocity

1. Only joint has velocity.
2. A force can increase the velocity.
    ```
    let the velocity is v and the force is F.  
    v += F * delta
    ```
3. Velocity will increase the position
    ```
    let the position is p.
    p += v * delta
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

1. Apply the force on velocity.
2. Clear the force.
3. Apply the velocity on position.
4. Calculate the distance between two joints after/before the *step 3*
5. Calculate how much more/less distance there is between two joints.
6. Apply the attenuating/pulling force on proximate joints.