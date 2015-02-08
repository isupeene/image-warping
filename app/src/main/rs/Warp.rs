#pragma version(1)
#pragma rs java_package_name(ca.isupeene.imagewarp.Warp)

int xCenter;
int yCenter;
int xMax;
int yMax;
rs_allocation inputAllocation;

uchar4 __attribute__((kernel)) fisheye(uint32_t x, uint32_t y) {
  // Here, we normalize x and y to values between 0 and 1,
  // and compute the original radius from the center.
  // The new radius is 2*r^2.5.  We then get the corresponding
  // x and y values for the new radius, and then denormalize
  // to get our final x and y.

  float xNorm = (float)x / xMax;
  float yNorm = (float)y / yMax;

  float r = sqrt(pow(xNorm - .5, (float)2) + pow(yNorm - .5, (float)2));
  float theta = atan2((float)(yNorm - .5), (float)(xNorm - .5));
  float rn = 2*pow(r, 2.5);

  float newXNorm = rn*cos(theta) + .5;
  float newYNorm = rn*sin(theta) + .5;

  int newX = max(0, min(xMax, (int)(newXNorm * xMax)));
  int newY = max(0, min(yMax, (int)(newYNorm * yMax)));

  const uchar4* element = rsGetElementAt(inputAllocation, newX, newY);
  return *element;
}

uchar4 __attribute__((kernel)) narrow(uint32_t x, uint32_t y) {
  // Similar to the fisheye, except we only modify x,
  // and we put it to a power less than 1, to create the effect
  // of narrowing the center of the image.

  float xNorm = (float)x / xCenter - 1;

  float newXNorm;
  if (xNorm < 0) {
    newXNorm = -pow(-xNorm, 0.7);
  }
  else {
    newXNorm = pow(xNorm, 0.7);
  }

  int newX = max(0, min(xMax, (int)((newXNorm + 1) * xCenter)));

  const uchar4* element = rsGetElementAt(inputAllocation, newX, y);
  return *element;
}

uchar4 __attribute__((kernel)) swirl(uint32_t x, uint32_t y) {
  // Here, we get use x and y to get r and theta, rotate theta,
  // and then use r and the new theta to get the new x and y.
  // The amount by which we change theta is proportional to r,
  // which gives us a swirl effect instead of just a rotation.

  int xFromCenter = x - xCenter;
  int yFromCenter = y - yCenter;

  float r = sqrt((float)(xFromCenter*xFromCenter + yFromCenter*yFromCenter));

  float theta = atan2((float)yFromCenter, (float)xFromCenter);
  float newTheta = theta + (r * M_PI)/(yMax); // Rotation proportional to image height.

  int newXFromCenter = r * cos(newTheta);
  int newYFromCenter = r * sin(newTheta);

  int newX = max(0, min(xMax, newXFromCenter + xCenter));
  int newY = max(0, min(yMax, newYFromCenter + yCenter));

  const uchar4* element = rsGetElementAt(inputAllocation, newX, newY);
  return *element;
}
