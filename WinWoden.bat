@echo off
cl /I"include" woden.c /link /NOD /INCREMENTAL:NO /DLL kernel32.lib /OUT:"woden.dll"