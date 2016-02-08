@echo off
set /p pathFrom=Path from: 
set /p pathTo=Path to: 
mklink /j %pathTo% %pathFrom%
pause