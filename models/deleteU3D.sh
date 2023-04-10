#To run this code open cmd and make sure you are in the same directory
#In the cmd run >bash deleteU3D.sh
#It's done!

#!/bin/sh
find . -name "*.u3d" -type f -delete
find . -name "*.u3d.info" -type f -delete
