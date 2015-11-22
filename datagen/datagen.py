import pyqtgraph as pg
import csv

f = open('../phase3/arealm_small.csv', 'r')
reader = csv.reader(f)
a = list(reader)


print(a)

# Set up a window with plot
import pyqtgraph as pg
win = pg.GraphicsWindow()
plt = win.addPlot()
plt.plot(x=[0, 0.1, 0.2, 0.3, 0.4], y=[1, 7, 2, 4, 3])

# Add a ViewBox below with two rectangles
vb = win.addViewBox(col=0, row=1)
r1 = pg.QtGui.QGraphicsRectItem(0, 0, 0.4, 1)
r1.setPen(pg.mkPen(None))
r1.setBrush(pg.mkBrush('r'))
vb.addItem(r1)
r2 = pg.QtGui.QGraphicsRectItem(0.2, -5, 0.1, 10)
r2.setPen(pg.mkPen((0, 0, 0, 100)))
r2.setBrush(pg.mkBrush((50, 50, 200)))
vb.addItem(r2)