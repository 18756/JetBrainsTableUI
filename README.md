# JetBrainsTableUI

## Main features:
* Choose table size
* Row header
* Save/open table files
* Basic math formulas with doubles
* Some functions with cell diapasons as parameters
* Parser/calculator error messages
* Dependency graph for recalculating dependent cells
* Check cyclic dependency
* Copy/paste formulas with cell updating
* Ctrl + z
* Row resizing
* Text field synchronized with selected cell for long formulas or error messages
* Table resize


## Formula operations:
* \+
* \-
* \*
* \/

## Formula functions:
* sin(x)
* cos(x)
* tan(x)
* ln(x)
* exp(x)
* abs(x)
* pow(a, b)
* min(x1, [x2, x3, x4,...])
* max(x1, [x2, x3, x4,...])
* sum(x1, [x2, x3, x4,...])
* mean(x1, [x2, x3, x4,...])
* std(x1, [x2, x3, x4,...])
* cor(cellDiapason1, cellDiapason2)

## Formula examples:
* =2+2*2
* =-(1.5 + A2)/5.1
* =-(-(-1))
* =sin(A1)/cos(B2)
* =-pow(3, C3)+36
* =min(A1, A2, A3) or =min(A1:A3)
* =sum(A1:D10)
* =cor(A1:A10, B1:B10)
