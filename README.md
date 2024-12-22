# jcontour

jcontour is a Java-based project about plotting meteorological contours. It integrates inverse distance weighting (IDW) and ordinary kriging interpolation algorithms, providing tools for visualizing and analyzing meteorological data.

## Features

- **Inverse Distance Weighting (IDW) Interpolation**: This algorithm estimates unknown data values based on known points, using inverse distance weighting.
- **Ordinary Kriging Interpolation**: A geostatistical method that considers the spatial correlation between known points to estimate unknown data values. The kriging algorithm in this project references the open-source library [oeo4b/kriging](https://github.com/oeo4b/kriging).
- **Geometric Drawing Calculations**: For contour plotting and related display calculations, I utilize code borrowed from [meteoinfo/wContour](https://github.com/meteoinfo/wContour), specifically dealing with KD-TREE and POLYGON functionalities.

