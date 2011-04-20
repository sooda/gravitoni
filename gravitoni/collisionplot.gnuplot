# 3d plot
# jos wxt ei toimi niin vaihda vaikka x11:ksi
set term wxt 0
splot 'collision_a.log' using 3:4:5 with lines title 'a', 'collision_b.log' using 3:4:5 with lines title 'b'

# distance plot
set term wxt 1
dist(ax, ay, az, bx, by, bz) = sqrt((ax-bx)**2 + (ay-by)**2 + (az-bz)**2)
# pasten voi korvata tekemällä sen käsin jos systeemi ei tue tämmösiä putkia
plot '< paste collision_a.log collision_b.log' using 1:(dist($3,$4,$5,$11,$12,$13)) with lines title 'distance'
