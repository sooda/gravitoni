import sys
import random

fmt = \
"""body {
	name %(name)s
	mass 1e30
	radius 1e9
	position %(position)s
	gfx {
		texture sun.png
		color %(color)s
	}
}
"""

print "dt 86400"
print "collisiontype 2"

def rpos():
	return random.random() * 2e12 - 2e12/2
	
def rclr():
	return 0.5 + 0.5 * random.random()

n = int(sys.argv[1])
for i in xrange(n):
	pos = "%d, %d, %d" % (rpos(), rpos(), rpos())
	clr = "%f, %f, %f" % (rclr(), rclr(), rclr())
	print fmt % {"name":i, "position":pos, "color":clr}